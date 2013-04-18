(ns nightbyday.main
  (:require [enfocus.core :as ef]
            [nightbyday.viewport :as viewport]
            [nightbyday.scene :as scene]
            [nightbyday.scenes :as scenes]
            )
  (:use [nightbyday.util :only [log clj->js]]
        [singult.core :only [render]])
  (:require-macros [enfocus.macros :as em])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def data (atom {}))
(def nodes (atom {}))

(defn delay-time [x]
  (int (Math/round (* x (@data :delay)))))

(defn zoom-html []
  (render [:div.scene "Parallax Scrolling SVG"]))

(defn svg-container-html []
  [:svg:svg#container {:version "1.1"
                       :width "2000" :height "1000"
                       :xmlns "http://www.w3.org/2000/svg"
                       :style (str "overflow: hidden; position: relative; "
                                   "-webkit-transform: translate3d(0, 0, 0);")
                       }])

(defn game-html []
  [:div.content
   [:div.scene]
   (svg-container-html)])

(defn prepare-game-page [root]
  (em/at js/document
         [root] (em/substitute (render (game-html)))))

(defn distance [[x0 y0] [x1 y1]]
  (let [dx (- x1 x0)
        dy (- y1 y0)]
    (Math/sqrt (+ (* dx dx) (* dy dy)))))

(defn layer-svg [layer]
  (render
   (let [id (layer :id)
         [x y] (layer :position [0 0])
         [w h] (layer :size)
         transform (str "translate(" (- x) "," (- y) ")")]
     [:svg:g {:id id
              :transform transform
              ;;:style (str "-webkit-transform: translate3d(" x "px, " y "px, 0px);")
              }])))

(defn image-svg [{:keys [position size image]}]
  (render
   (let [[x y] position
         [w h] size]
     ;; [:svg:rect {:x x :y y
     ;;             :width w :height h
     ;;             :fill (str "#" (rand-int 10) (rand-int 10) (rand-int 10))}]
     [:svg:image {:x x :y y
                  :width w :height h
                  :preserveAspectRatio "none"
                  :xlink:href image}]
     )))

(defn append-node! [parent node]
  (let [id (str "#" (parent :id))]
    (em/at js/document
           [id] (em/append node))))

(defn by-key [key-fn val]
  (fn [objects]
    (loop [objects objects]
      (if (empty? objects) nil
          (let [object (first objects)]
            (if (= (key-fn object) val)
              object
              (recur (rest objects))))))))

(defn update-by-keys [map path fn & args]
  (loop [map map
         path path]
    (if (empty? path)
      (apply fn map args)
      (let [k1 (first path)
            v1 (k1 map)]
        (recur v1 (rest path))))))

(defn zoomtest []
  (scene/show-scene! {:id :zoomscene :content (zoom-html)} (delay-time 1.5))
  (swap! data (fn [data] (-> data
                             (assoc-in [:scene :layers] [{:id "sky" :scale 0.5 :position [500 0] :size [3000 2000] :objects [{:image "img/sky.png" :position [0 -500] :size [3000 2000]}]}
                                                         {:id "clouds" :scale 0.6 :position [100 0] :size [3300 1650] :objects [{:image "img/clouds.png" :position [0 -100] :size [2000 500]}]}
                                                         {:id "landscape" :scale 0.7 :position [150 0] :size [2850 1425] :objects [{:image "img/landscape.png" :position [0 310] :size [2800 550]}]}
                                                         {:id "ground" :scale 0.8 :position [300 0] :size [2500 1250] :objects [{:image "img/ground.png" :position [0 700] :size [2600 800]}
                                                                                                                                ]}
                                                         {:id "main" :position [0 0] :size [2000 1500] :objects [{:type "tree1" :position [0 0] :size [10 10]}
                                                                                                                 {:image "img/tree.png" :position [1000 800] :size [100 200]}
                                                                                                                 {:image "img/tree.png" :position [900 810] :size [100 200]}
                                                                                                                 {:image "img/tree.png" :position [1050 790] :size [90 180]}
                                                                                                                 {:image "img/tree.png" :position [1300 880] :size [120 250]}]}])
                             (assoc-in [:scene :scale] 1.0))))

  (doseq [layer (get-in @data [:scene :layers])]
    (em/at js/document
           ["svg"] (em/append (layer-svg layer)))
    (let [layer-node (.getElementById js/document (layer :id))]
      ;;      (swap! data (fn [data] (update-by-keys data [:scene :layers (by-key :id (layer :id))])
      ;;                    (fn [layer]
      ;;                      (assoc layer :node layer-node)))))
      (swap! nodes (fn [nodes]
                     (assoc nodes (layer :id) layer-node))))
    (doseq [object (layer :objects)]
      (append-node! layer (image-svg object))))
  )

(defn refresh-layers []
  (doseq [layer (get-in @data [:scene :layers])]
    (let [layer-node (@nodes (layer :id))
          [x y] (layer :position [0 0])
          s (get-in @data [:scene :scale])
          transform (str "translate(" (- x) "," (- y) ") scale(" s ")")]
      (em/at layer-node (em/set-attr
                         ;;:style (str "-webkit-transform: translate3d(" x "px, " y "px, 0px);")
                         :transform transform
                         ))
      )))

(defn drag-start-layers [layers]
  (for [layer layers]
    (assoc layer :drag-start-position (layer :position))))

(defn update-layers [layers [dx dy] s]
  (for [layer layers]
    (let [[x y] (layer :drag-start-position)
          ;;s (layer :scale 1.0)
          [dx dy] [(* dx s) (* dy s)]
          new-position [(- x (/ dx s)) (- y (/ dy s))]]
      (-> layer
          (assoc :position new-position)))))

(defn update-scene [scene [ox oy] [dx dy] [cx cy] [os ns]]
  (let [main-layer ((by-key :id "main") (get-in scene [:layers]))
        [lw lh] (main-layer :size)
        [lx ly] (main-layer :position)
        [dw dh] [(.-clientWidth (.-documentElement js/document)) (.-clientHeight (.-documentElement js/document))]
        ns (viewport/between (Math/max (/ dw lw) (/ dh lh)) ns 2.0)

        nlw (* lw ns)
        nlh (* lh ns)

        nlx (+ (* cx (- 1 (/ nlw lw))) (* (/ nlw lw) lx))
        nly (+ (* cy (- 1 (/ nlh lh))) (* (/ nlh lh) ly))

        [dx dy] [(+ dx (/ (- nlx lx) ns)) (+ dy (/ (- nly ly) ns))]

        ;;_ (log "zooming to " cx ", " cy " @ " os " -> " ns)

        ;;[dx dy] [(+ dx (/ (- mx nx) s)) (+ dy (/ (- my ny) s))]
        ;; ox = location_.getX() + (x - getWidth()  * 0.5f) / zoom_;
        ;; float oy = location_.getY() + (y - getHeight() * 0.5f) / zoom_;

        ;; zoom_ *= zoom;

        ;; float nx = location_.getX() + (x - getWidth()  * 0.5f) / zoom_;
        ;; float ny = location_.getY() + (y - getHeight() * 0.5f) / zoom_;

        ;; location_.add(ox - nx, oy - ny, 0.0f);

        ;;[dx dy] [(+ dx (/ (* (- 1 s) cx) s))
        ;;         (+ dy (/ (* (- 1 s) cy) s))]
        ;;_ (log "s " s " " [dx dy])
        ;;[dx dy] [(+ dx (/ (- (+ lx (* cx s)) (+ lx (* 0.5 lw))) s))
        ;;         (+ dy (/ (- (+ ly (* cy s)) (+ ly (* 0.5 lh))) s))]

        viewport [(- ox (* ns dx)) (- oy (* dy ns)) (* dw ns) (* dh ns)]
        scaled-main-layer {:size [(* lw ns) (* lh ns)]}
        trans-limited-viewport (viewport/limit-viewport viewport scaled-main-layer)
        [lx ly lw lh] trans-limited-viewport
        [dx dy] [(/ (- ox lx) ns) (/ (- oy ly) ns)]]
    (-> scene
        (update-in [:layers] update-layers [dx dy] ns)
        (assoc-in [:scale] ns))))

(defn drag-start [event]
  (swap! data (fn [data]
                (-> data
                    (assoc-in [:transform-start-scale] (get-in data [:scene :scale]))
                    (update-in [:scene :layers] drag-start-layers)))))

(defn drag [event]
  (swap! data (fn [data]
                (let [main-layer ((by-key :id "main") (get-in data [:scene :layers]))
                      [ox oy] (main-layer :drag-start-position)
                      dx (.-distanceX event)
                      dy (.-distanceY event)
                      os (get-in data [:scene :scale])
                      cx (+ ox (* (.-x (.-position event)) os))
                      cy (+ oy (* (.-y (.-position event)) os))
                      ns os]                  
                  (log "scale=" os " xy=" ox "," oy)
                  (assoc data :scene (update-scene (data :scene) [ox oy] [dx dy] [cx cy] [os ns]))
                  )))
  (refresh-layers))

(defn double-tap [event]
  (swap! data (fn [data]
                (let [main-layer ((by-key :id "main") (get-in data [:scene :layers]))
                      [ox oy] (main-layer :position)
                      dx 0
                      dy 0
                      os (get-in data [:scene :scale])
                      cx (+ ox (* (.-x (aget (.-position event) 0)) os))
                      cy (+ oy (* (.-y (aget (.-position event) 0)) os))
                      ns (* 1.2 os)]
                  (-> data
                      (assoc-in [:transform-start-scale] os)
                      (update-in [:scene :layers] drag-start-layers)
                      (assoc :scene (update-scene (data :scene) [ox oy] [dx dy] [cx cy] [os ns]))))))
  (refresh-layers))

(defn transform-start [event]
  (swap! data (fn [data]
                (-> data
                    (assoc-in [:transform-start-scale] (get-in data [:scene :scale]))
                    (update-in [:scene :layers] drag-start-layers)))))

(defn transform [event]
  (swap! data (fn [data]
                (let [main-layer ((by-key :id "main") (get-in data [:scene :layers]))
                      [ox oy] (main-layer :drag-start-position)
                      dx 0
                      dy 0
                      cx (.-x (.-position event))
                      cy (.-y (.-position event))
                      os (get-in data [:scene :scale])
                      ns (* os (or (.-scale event) 1.0))]
                  (assoc data :scene (update-scene (data :scene) [ox oy] [dx dy] [cx cy] [os ns])))))
  (refresh-layers))

(defn replace-object [old-object new-object objects]
  (loop [done-objects []
         remaining-objects objects]
    (if (or (not remaining-objects) (empty? remaining-objects))
      done-objects
      (let [object (first remaining-objects)]
        (if (= object old-object)
          (recur (conj done-objects new-object) (rest remaining-objects))
          (recur (conj done-objects object) (rest remaining-objects)))))))

(defn startup [delay]
  (prepare-game-page ".content")
  (swap! data (fn [_] {:delay (or delay 1000)
                       :results #{}}))
  (let [hammer (js/Hammer. (.getElementById js/document "container")
                           (clj->js {:prevent_default true
                                     :drag true
                                     :swipe false
                                     :transform true
                                     :tap false
                                     :tap_double true
                                     :hold false
                                     ;;:allow_touch_and_mouse true
                                     }))]
    ;;(set! (.-ontap hammer) double-tap)
    (set! (.-ondoubletap hammer) double-tap)
    (set! (.-ondragstart hammer) drag-start)
    (set! (.-ondrag hammer) drag)
    (set! (.-ontransformstart hammer) transform-start)
    (set! (.-ontransform hammer) transform)
    )
  (zoomtest)
  (log "startup"))
