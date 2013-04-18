(ns ae.game.asset.svg
  (:require [clojure.xml :as xml])
  (:require [clojure.string :as string])
  (:use [clojure.test])
  (:require [ae.inkscape.core :as inkscape])
  (:require [ae.vec2 :as v]))

(defn mount? [element]
  (let [attrs (element :attrs)
        label (attrs :inkscape:label)]
    (and attrs label
         (let [types (inkscape/element-types element)]
           (some (partial subtype? :mount) types)))))

(defn game-object? [element]
  (let [attrs (element :attrs)
        label (attrs :inkscape:label)]
    (and attrs label
         (let [types (inkscape/element-types element)]
           (some object-type? types)))))

(defn potential-game-object? [element]
  (let [attrs (element :attrs)
        label (attrs :inkscape:label)]
    (and attrs label
         (not (re-matches #"#.+" label))
         (not (game-object? element))
         (not (mount? element)))))

(defn transform-object [object translation]
  (let [t (get-in object [:attrs :transform])
        tt (if t (v/sum translation (svg-translate t)) translation)
        to (if t (dissoc-in object [:attrs :transform]) object)
        to (case (:tag to)
             ;;:path (transform-path to tt)
             ;;:g (transform-g to tt)
             to)]
    (assoc to :content (map #(transform-object % tt) (:content to)))))

(defn gather-content [root]
  (if (nil? root)
    []
    (let [direct-content (root :content)]
      (concat direct-content
              (mapcat gather-content direct-content)))))

(defn- walk-fn [fn root]
  (if (nil? root)
    []
    (let [rooted (fn root)]
      (assoc rooted :content (map (partial walk-fn fn) (:content rooted))))))

(defn- mounts [object]
  (let [content (gather-content object)
        mounts (filter mount? content)]
    mounts))

(defn load-graphics [filename]
  (let [xml (xml/parse (file/load-stream filename))
        content (mapcat gather-content
                        (filter inkscape/layer? (xml :content)))]
    [(filter game-object? content)
     (filter potential-game-object? content)
     (filter mount? content)]))

(defn- print-list [objects]
  (println (apply str (interpose ", " objects))))

(defn- adjust-right [n pad s]
  (let [l (count s)]
    (str s (apply str (repeat (- n l) pad)))))

(defn- print-header [text]
  (println (adjust-right 60 "-" text)))

(defn- print-footer []
  (println (adjust-right 60 "-" "")))

(defn list-objects [filename]
  (let [[objects potentials mounts] (load-graphics filename)]
    (print-header (str "- Objects (" (count objects) ") "))
    (let [objects (sort (map :inkscape:label (map :attrs objects)))]
      (print-list objects))
    (print-header (str "- Potential objects (" (count potentials) ") "))
    (let [objects (sort (map :inkscape:label (map :attrs potentials)))]
      (print-list objects))
    (print-header (str "- Mounts (" (count mounts) ") "))
    (let [objects (sort (map :inkscape:label (map :attrs mounts)))]
      (print-list objects))
    (print-footer)))

(defn show-object [filename id]
  (let [[objects potentials] (load-graphics filename)]
    (first (filter (fn [x] (= (get-in x [:attrs :id]) id)) objects))))

(defn list-ids [filename]
  (let [[objects potentials] (load-graphics filename)]
    (println "Found" (count objects) "objects and" (count potentials) "potentials.")
    (println "Objects:")
    (print-list (sort (map :id (map :attrs objects))))
    (println "Potential objects:")
    (print-list (sort (map :id (map :attrs potentials))))))

(defn show-mounts [filename id]
  (let [[objects potentials] (load-graphics filename)
        object (first (filter (fn [x] (= (get-in x [:attrs :id]) id)) objects))
        mounts (mounts object)]
    (println "Found" (count mounts) "mounts.")
    (println "Mounts:")
    (print-list (map :inkscape:label (map :attrs mounts)))))

(defn- cleanup [object]
  (if (or (nil? object) (mount? object))
    nil
    (assoc object
      :attrs (select-keys (:attrs object) (remove inkscape? (keys (:attrs object))))
      :content (when (:content object) (remove nil? (map cleanup (:content object)))))))

(defn- printable? [key]
  (or (inkscape/inkscape-attribute? key)
      (let [k (name key)]
        (re-matches #"style" k))))

(defn- printup [object]
  (if (mount? object)
    nil
    (assoc object
      :attrs (select-keys (:attrs object) (remove printable? (keys (:attrs object))))
      :content (when (:content object) (remove nil? (map printup (:content object)))))))

(defn- find-mount [object mount-type]
  (let [mounts (mounts-of object)
        mounts (filter (partial has-type? mount-type) mounts)]
    (when-not (or (nil? mounts) (empty? mounts)) (first mounts))))


(defn- location-of [mount]
  (when mount (first-svg-path-point (get-in mount [:attrs :d]))))

(defn- add [object v]
  {:tag :g :attrs {:transform (str "translate(" (first v) "," (second v) ")")} :content [object]})

(defn- sub [object v]
  (add object (map - v)))

(defn- place [object x y]
  (let [origo (location-of (find-mount object "bodymount"))
        origo (or origo [0 0])]
    (if origo
      (add object [(- x (first origo)) (- y (second origo))])
      object)))

(defn- find-object [id objects]
  (let [o (filter (fn [x] (= (get-in x [:attrs :id]) id)) objects)]
    (when o (first o))))

(defn- transform-to-origo [objects]
  (map (fn [o] (dissoc-in o [:attrs :transform])) objects))

(defn- wo []
  (let [[objects1 potentials1] (load-graphics "people.svg")
        [objects2 potentials2] (load-graphics "objects.svg")
        objects (transform-to-origo (concat objects1 objects2))]
    ;;(println (count objects1) (count objects2) (count objects))
    (reduce (fn [os o] (assoc os (get-in o [:attrs :id]) o)) {} objects)))

(defn- wear [object on-object on-mount]
  (let [l (location-of (find-mount on-object on-mount))
        m (svg-translate (get-in on-object [:attrs :transform]))]
    (apply place object (v/sum l m))))

(defn rotate [object angle location]
  {:tag :g :attrs {:transform (str "rotate(" angle " " (first location) " " (second location) ")")} :content [object]})

(defn scale [object sx sy]
  {:tag :g :attrs {:transform (str "scale(" sx " " sy ")")} :content [object]})

(defn- hold [object on-object on-mount]
  (let [l (location-of (find-mount on-object on-mount))
        m (svg-translate (get-in on-object [:attrs :transform]))
        angle (case on-mount
                "righthand" (case (rand-int 5)
                              0 -25
                              1 -15
                              2 25
                              3 45
                              4 -150)
                "lefthand" (case (rand-int 4)
                             0 -45
                             1 -25
                             2 15
                             3 25)
                :else 0)]
    (rotate (apply place object (v/sum l m)) angle (v/sum m l))))

(defn make-man [db]
  (let [{man "man0"
         leftboot0 "leftboot0"
         rightboot0 "rightboot0"
         leftboot1 "leftboot1"
         rightboot1 "rightboot1"
         leftshoe0 "leftshoe0"
         rightshoe0 "rightshoe0"
         leftvambrace0 "leftvambrace0"
         rightvambrace0 "rightvambrace0"
         jacket0 "jacket0"
         jacket1 "jacket1"
         jacket3 "jacket3"
         spear0 "spear0"
         sword0 "sword0"
         battleaxe0 "battleaxe0"
         mace0 "mace0"
         morningstar0 "morningstar0"
         shield0 "shield0"
         shield1 "shield1"
         vest0 "vest0"
         shirt2 "shirt2"
         shirt3 "shirt3"
         pants0 "pants0"
         beard0 "beard0"
         beard1 "beard1"
         helmet0 "helmet0"
         helmet1 "helmet1"
         helmet2 "helmet2"
         hair1 "hair1"
         hair4 "hair4"} db
         pman (place man 0 0)
         content (remove nil?
                         (map cleanup
                              (concat [pman]
                                      (case (rand-int 4)
                                        0 []
                                        1 [(wear vest0 pman "chest")]
                                        2 [(wear shirt2 pman "chest")]
                                        3 [(wear shirt3 pman "chest")])

                                      [(wear pants0 pman "groin")]

                                      (case (rand-int 3)
                                        0 []
                                        1 [(wear jacket1 pman "chest")]
                                        2 [(wear jacket3 pman "chest")])

                                      (case (rand-int 4)
                                        0 []
                                        1 [(wear leftboot0 pman "leftfoot")
                                           (wear rightboot0 pman "rightfoot")]
                                        2 [(wear leftboot1 pman "leftfoot")
                                           (wear rightboot1 pman "rightfoot")]
                                        3 [(wear leftshoe0 pman "leftfoot")
                                           (wear rightshoe0 pman "rightfoot")])

                                      (case (rand-int 3)
                                        0 []
                                        1 [(wear beard0 pman "mouth")]
                                        2 [(wear beard1 pman "mouth")])
                                      (case (rand-int 3)
                                        0 []
                                        1 [(wear hair1 pman "head")]
                                        2 [(wear hair4 pman "head")])
                                      (case (rand-int 4)
                                        0 []
                                        1 [(wear helmet0 pman "head")]
                                        2 [(wear helmet1 pman "head")]
                                        3 [(wear helmet2 pman "head")])
                                      (case (rand-int 2)
                                        0 []
                                        1 [(wear leftvambrace0 pman "leftwrist")
                                           (wear rightvambrace0 pman "rightwrist")])
                                      (case (rand-int 5)
                                        0 [(hold spear0 pman "righthand")]
                                        1 [(hold sword0 pman "righthand")]
                                        2 [(hold battleaxe0 pman "righthand")]
                                        3 [(hold mace0 pman "righthand")]
                                        4 [(hold morningstar0 pman "righthand")])
                                      (case (rand-int 3)
                                        0 []
                                        1 [(hold shield0 pman "lefthand")]
                                        2 [(hold shield1 pman "lefthand")]))))]
    {:tag :g :content content}))

(defn make []
  (let [db (wo)
        content [(place (make-man db) 50 200)
                 (place (make-man db) 130 200)
                 (place (make-man db) 210 200)
                 (place (make-man db) 290 200)
                 (place (make-man db) 370 200)]
        svg (with-out-str (xml/emit {:tag :svg
                                     :attrs {:xmlns:svg "http://www.w3.org/2000/svg"
                                             :xmlns "http://www.w3.org/2000/svg"
                                             :width "420"
                                             :height "200"
                                             :version "1.1"
                                             :style "border: 1px solid black"}
                                     :content content}))]
    svg))

(defn write []
  (spit "test.svg" (make)))