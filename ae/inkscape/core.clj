(ns ae.inkscape.core
  (:require [clojure.string :as string])
  (:use [clojure.test])
  )

(defn layer?
  "Is an XML element is an Inkscape layer?"
  [element]
  (and (= (:tag element) :g)
       (let [attrs (:attrs element)]
         (= (:inkscape:groupmode attrs) "layer"))))

(deftest layer?-test
  (is (not (layer? nil)) "nil is not a layer")
  (is (not (layer? {})) "empty map is not a layer")
  (is (not (layer? {:foo 1 :bar "2"})) "any map is not a layer")
  (is (not (layer? {:tag :svg :attrs {} :contents nil})) "any XML element is not a layer")
  (is (not (layer? {:tag :g :attrs {:inkscape:groupmode nil}})) "invalid layer is not a layer")
  (is (layer? {:tag :g :attrs {:inkscape:groupmode "layer"}}) "valid layer is a layer"))

(defn element?
  "Is it an Inkscape element?"
  [x]
  (get-in x [:attrs :inkscape:label] false))

(deftest element?-test
  (is (not (element? nil)) "nil is not an element")
  (is (not (element? {})) "empty map is not an element")
  (is (not (element? {:foo 1 :bar "2"})) "any map is not an element")
  (is (not (element? {:tag :svg :attrs {} :contents nil})) "any XML element is not an Inkscape element")
  (is (element? {:tag :g :attrs {:inkscape:label "valid element"}}) "valid element is an element"))

(defn element-types
  "Returns the types of an Inkscape element"
  [element]
  (let [attrs (:attrs element)
        label (:inkscape:label attrs)]
    (if (empty? label)
      []
      (string/split label #"\s+"))))

;;   (if (not (element? element)) []

(deftest element-types-test
  (is (= [] (element-types nil)) "element types of an invalid element is empty")
  (is (= [] (element-types {:attrs {:inkscape:label ""}})) "element types of unlabeled object is empty")
  (is (= ["foo"] (element-types {:attrs {:inkscape:label "foo"}})) "element types of single labeled element is a single type")
  (is (= ["a" "b" "c"] (element-types {:attrs {:inkscape:label "a b c"}})) "multiple element types are split"))

(defn element-type?
  "Is the element of type?"
  [element type]
  (let [type-set (apply hash-set (element-types element))]
    (type-set type)))

(deftest element-type?-test
  (is (not (element-type? nil "foo")))
  (is (not (element-type? {:attrs {:inkscape:label "bar"}} "foo")))
  (is (element-type? {:attrs {:inkscape:label "foo"}} "foo") "element types are detected for single typed elements")
  (is (element-type? {:attrs {:inkscape:label "a b c"}} "b") "element types are detected for multiple typed elements"))

(defn inkscape-attribute?
  "Is the key an Inkscape attribute?"
  [key]
  (when key
    (let [k (name key)]
      (or (re-matches #"inkscape:.*" k)
          (re-matches #"sodipodi:.*" k)
          (re-matches #"cc:.*" k)
          (re-matches #"dc:.*" k)))))

(deftest inkscape-attribute?-test
  (is (not (inkscape-attribute? nil)))
  (is (not (inkscape-attribute? "id")))
  (is (not (inkscape-attribute? :id)))
  (is (inkscape-attribute? :inkscape:foo:bar))
  (is (inkscape-attribute? :sodipodi:foo:bar))
  (is (inkscape-attribute? :cc:foo:bar))
  (is (inkscape-attribute? :dc:foo:bar)))

(defn clean-attributes
  "Cleans the element of all Inkscape specific attributes."
  [element]
  (let [new-attrs (select-keys (:attrs element) (remove inkscape? (keys (:attrs element))))
        new-content (map clean-attributes (:content element))]
    (-> element
        (#(if-not (empty? new-attrs) (assoc % :attrs new-attrs) %))
        (#(if-not (empty? new-content) (assoc % :content new-content) %)))))

(deftest clean-attributes-test
  (is (= nil (clean-attributes nil)))
  (is (= {} (clean-attributes {})))
  (is (= {:attrs {:foo 1}} (clean-attributes {:attrs {:foo 1 :inkscape:label "foo"}})) "elements are cleaned up")
  (is (= {:content [{:attrs {:foo 1}}]} (clean-attributes {:content [{:attrs {:foo 1 :inkscape:label "foo"}}]})) "nested elements are processed"))

(defn printable?
  "Is the attribute something that should be printed to users?"
  [key]
  (or (inkscape-attribute? key)
      (let [name (name key)]
        (re-matches #"style" name))))

(deftest printable?-test
  (= (printable? :foo))
  (= (not (printable? :inkscape:label)))
  (= (not (printable? :style))))



;; (defn- clean-object
;;   "Cleans the object to displayable SVG without extraneous elements such as mounts."
;;   [object]
;;   (if (or (nil? object) (mount? object))
;;     nil
;;     (clean-attributes object)
;;     (assoc object
;;       :attrs (select-keys (:attrs object) (remove inkscape? (keys (:attrs object))))
;;       :content (remove nil? (map clean-object (:content object))))))

;; (defn- game-object? [element]
;;   (let [attrs (element :attrs)
;;         label (attrs :inkscape:label)]
;;     (and attrs label
;;          (let [types (types-of element)]
;;            (some object-type? types)))))

;; (declare mount?)

;; (defn- potential-game-object? [element]
;;   (let [attrs (element :attrs)
;;         label (attrs :inkscape:label)]
;;     (and attrs label
;;          (not (re-matches #"#.+" label))
;;          (not (game-object? element))
;;          (not (mount? element)))))


;; (defn- mount? [element]
;;   (let [attrs (element :attrs)
;;         label (attrs :inkscape:label)]
;;     (and attrs label
;;          (let [types (types-of element)]
;;            (some (partial subtype? :mount) types)))))

;; (defn svg-translate [s]
;;   (let [m (re-matches #"\s*translate\(([-\d.]+),([-\d.]+)\)\s*" s)]
;;     (if m
;;       (vec (map read-string (take-last 2 m)))
;;       [0 0])))

;; (deftest svg-translate-test
;;   (is (= [39.031125 219.34909] (svg-translate "translate(39.031125,219.34909)"))))

;; (defn- extract-coordinates [match]
;;   (vec (map read-string (take-last 2 match))))

;; (defn- svg-d-coordinates [d]
;;   (let [matches (re-seq #"([-\d.]+),([-\d.]+)" d)]
;;     (if matches
;;       (map extract-coordinates matches))))

;; (defn- svg-d-parts [d]
;;   (string/split #"\s" d))

;; (deftest svg-d-coordinates-test
;;   (is (= (list [94.015495 414.41899] [3.10984 5.38639] [6.21967 0]) (svg-d-coordinates "m 94.015495,414.41899 3.10984,5.38639 -6.21967,0 z"))))

;; (defn- translate-svg-d-part [translation part]
;;   (let [match (re-matches #"([-\d.]+),([-\d.]+)" part)]
;;     (if match
;;       (apply str (interpose "," (v/sum translation (extract-coordinates match))))
;;       part)))

;; (defn- transform-path [path translation]
;;   (let [d (get-in path [:attrs :d])]
;;     (if d
;;       (assoc-in path [:attrs :d] (apply str (interpose " " (map (partial translate-svg-d-part translation) (svg-d-parts d)))))
;;       path)))

;; (deftest transform-path-test
;;   (is (= "m -5,-5 5,-5 5,5 z" (transform-path {:tag :path :attrs {:translation "translate(10,10)" :d "m 0,0 10,0 10,10 z"}} [-5 -5]))))

;; (defn transform-object [object translation]
;;   (let [t (get-in object [:attrs :transform])
;;         tt (if t (v/sum translation (svg-translate t)) translation)
;;         to (if t (dissoc-in object [:attrs :transform]) object)
;;         to (case (:tag to)
;;              ;;:path (transform-path to tt)
;;              ;;:g (transform-g to tt)
;;              to)]
;;     (assoc to :content (map #(transform-object % tt) (:content to)))))

;; (defn gather-content [root]
;;   (if (nil? root)
;;     []
;;     (let [direct-content (root :content)]
;;       (concat direct-content
;;               (mapcat gather-content direct-content)))))

;; (defn walk-fn [fn root]
;;   (if (nil? root)
;;     []
;;     (let [rooted (fn root)]
;;       (assoc rooted :content (map (partial walk-fn fn) (:content rooted))))))

;; (defn load-graphics [filename]
;;   (let [xml (xml/parse (file/load-stream filename))
;;         content (mapcat gather-content
;;                         (filter inkscape-layer? (xml :content)))]
;;     [(filter game-object? content)
;;      (filter potential-game-object? content)
;;      (filter mount? content)]))

;; (defn- print-list [objects]
;;   (println (apply str (interpose ", " objects))))

;; (defn adjust-right [n pad s]
;;   (let [l (count s)]
;;     (str s (apply str (repeat (- n l) pad)))))

;; (defn- print-header [text]
;;   (println (adjust-right 60 "-" text)))

;; (defn- print-footer []
;;   (println (adjust-right 60 "-" "")))

;; (defn list-objects [filename]
;;   (let [[objects potentials mounts] (load-graphics filename)]
;;     (print-header (str "- Objects (" (count objects) ") "))
;;     (let [objects (sort (map :inkscape:label (map :attrs objects)))]
;;       (print-list objects))
;;     (print-header (str "- Potential objects (" (count potentials) ") "))
;;     (let [objects (sort (map :inkscape:label (map :attrs potentials)))]
;;       (print-list objects))
;;     (print-header (str "- Mounts (" (count mounts) ") "))
;;     (let [objects (sort (map :inkscape:label (map :attrs mounts)))]
;;       (print-list objects))
;;     (print-footer)))

;; (defn show-object [filename id]
;;   (let [[objects potentials] (load-graphics filename)]
;;     (first (filter (fn [x] (= (get-in x [:attrs :id]) id)) objects))))

;; (defn list-ids [filename]
;;   (let [[objects potentials] (load-graphics filename)]
;;     (println "Found" (count objects) "objects and" (count potentials) "potentials.")
;;     (println "Objects:")
;;     (print-list (sort (map :id (map :attrs objects))))
;;     (println "Potential objects:")
;;     (print-list (sort (map :id (map :attrs potentials))))))

;; (defn- mounts-of [object]
;;   (let [content (gather-content object)
;;         mounts (filter mount? content)]
;;     mounts))

;; (defn show-mounts [filename id]
;;   (let [[objects potentials] (load-graphics filename)
;;         object (first (filter (fn [x] (= (get-in x [:attrs :id]) id)) objects))
;;         mounts (mounts-of object)]
;;     (println "Found" (count mounts) "mounts.")
;;     (println "Mounts:")
;;     (print-list (map :inkscape:label (map :attrs mounts)))))

;; (defn- printup [object]
;;   (if (mount? object)
;;     nil
;;     (assoc object
;;       :attrs (select-keys (:attrs object) (remove printable? (keys (:attrs object))))
;;       :content (when (:content object) (remove nil? (map printup (:content object)))))))

;; (defn- find-mount [object mount-type]
;;   (let [mounts (mounts-of object)
;;         mounts (filter (partial has-type? mount-type) mounts)]
;;     (when-not (or (nil? mounts) (empty? mounts)) (first mounts))))

;; (defn- first-svg-path-point [d]
;;   (let [m (re-matches #"\w+ ([-\d.]+),([-\d.]+).*" d)]
;;     (when m
;;       (vec (map read-string (take-last 2 m))))))

;; (deftest first-svg-path-point-test
;;   (is (= [70.054589 59.634775] (first-svg-path-point "m 70.054589,59.634775 52.622471,0.505076 6.62247,0 0,5.303301 -6.62247,0 -52.622471,0.505076 z"))))

;; (defn- location-of [mount]
;;   (when mount (first-svg-path-point (get-in mount [:attrs :d]))))

;; (defn- add [object v]
;;   {:tag :g :attrs {:transform (str "translate(" (first v) "," (second v) ")")} :content [object]})

;; (defn- sub [object v]
;;   (add object (map - v)))

;; (defn- place [object x y]
;;   (let [origo (location-of (find-mount object "bodymount"))
;;         origo (or origo [0 0])]
;;     (if origo
;;       (add object [(- x (first origo)) (- y (second origo))])
;;       object)))

;; (defn- find-object [id objects]
;;   (let [o (filter (fn [x] (= (get-in x [:attrs :id]) id)) objects)]
;;     (when o (first o))))

;; (defn- transform-to-origo [objects]
;;   (map (fn [o] (dissoc-in o [:attrs :transform])) objects))

;; (defn- wo []
;;   (let [[objects1 potentials1] (load-graphics "people.svg")
;;         [objects2 potentials2] (load-graphics "objects.svg")
;;         objects (transform-to-origo (concat objects1 objects2))]
;;     ;;(println (count objects1) (count objects2) (count objects))
;;     (reduce (fn [os o] (assoc os (get-in o [:attrs :id]) o)) {} objects)))

;; (defn- wear [object on-object on-mount]
;;   (let [l (location-of (find-mount on-object on-mount))
;;         m (svg-translate (get-in on-object [:attrs :transform]))]
;;     (apply place object (v/sum l m))))

;; (defn rotate [object angle location]
;;   {:tag :g :attrs {:transform (str "rotate(" angle " " (first location) " " (second location) ")")} :content [object]})

;; (defn scale [object sx sy]
;;   {:tag :g :attrs {:transform (str "scale(" sx " " sy ")")} :content [object]})

;; (defn- hold [object on-object on-mount]
;;   (let [l (location-of (find-mount on-object on-mount))
;;         m (svg-translate (get-in on-object [:attrs :transform]))
;;         angle (case on-mount
;;                 "righthand" (case (rand-int 5)
;;                               0 -25
;;                               1 -15
;;                               2 25
;;                               3 45
;;                               4 -150)
;;                 "lefthand" (case (rand-int 4)
;;                              0 -45
;;                              1 -25
;;                              2 15
;;                              3 25)
;;                 :else 0)]
;;     (rotate (apply place object (v/sum l m)) angle (v/sum m l))))

;; (defn make-man [db]
;;   (let [{man "man0"
;;          leftboot0 "leftboot0"
;;          rightboot0 "rightboot0"
;;          leftboot1 "leftboot1"
;;          rightboot1 "rightboot1"
;;          leftshoe0 "leftshoe0"
;;          rightshoe0 "rightshoe0"
;;          leftvambrace0 "leftvambrace0"
;;          rightvambrace0 "rightvambrace0"
;;          jacket0 "jacket0"
;;          jacket1 "jacket1"
;;          jacket3 "jacket3"
;;          spear0 "spear0"
;;          sword0 "sword0"
;;          battleaxe0 "battleaxe0"
;;          mace0 "mace0"
;;          morningstar0 "morningstar0"
;;          shield0 "shield0"
;;          shield1 "shield1"
;;          vest0 "vest0"
;;          shirt2 "shirt2"
;;          shirt3 "shirt3"
;;          pants0 "pants0"
;;          beard0 "beard0"
;;          beard1 "beard1"
;;          helmet0 "helmet0"
;;          helmet1 "helmet1"
;;          helmet2 "helmet2"
;;          hair1 "hair1"
;;          hair4 "hair4"} db
;;          pman (place man 0 0)
;;          content (remove nil?
;;                          (map cleanup
;;                               (concat [pman]
;;                                       (case (rand-int 4)
;;                                         0 []
;;                                         1 [(wear vest0 pman "chest")]
;;                                         2 [(wear shirt2 pman "chest")]
;;                                         3 [(wear shirt3 pman "chest")])

;;                                       [(wear pants0 pman "groin")]

;;                                       (case (rand-int 3)
;;                                         0 []
;;                                         1 [(wear jacket1 pman "chest")]
;;                                         2 [(wear jacket3 pman "chest")])

;;                                       (case (rand-int 4)
;;                                         0 []
;;                                         1 [(wear leftboot0 pman "leftfoot")
;;                                            (wear rightboot0 pman "rightfoot")]
;;                                         2 [(wear leftboot1 pman "leftfoot")
;;                                            (wear rightboot1 pman "rightfoot")]
;;                                         3 [(wear leftshoe0 pman "leftfoot")
;;                                            (wear rightshoe0 pman "rightfoot")])

;;                                       (case (rand-int 3)
;;                                         0 []
;;                                         1 [(wear beard0 pman "mouth")]
;;                                         2 [(wear beard1 pman "mouth")])
;;                                       (case (rand-int 3)
;;                                         0 []
;;                                         1 [(wear hair1 pman "head")]
;;                                         2 [(wear hair4 pman "head")])
;;                                       (case (rand-int 4)
;;                                         0 []
;;                                         1 [(wear helmet0 pman "head")]
;;                                         2 [(wear helmet1 pman "head")]
;;                                         3 [(wear helmet2 pman "head")])
;;                                       (case (rand-int 2)
;;                                         0 []
;;                                         1 [(wear leftvambrace0 pman "leftwrist")
;;                                            (wear rightvambrace0 pman "rightwrist")])
;;                                       (case (rand-int 5)
;;                                         0 [(hold spear0 pman "righthand")]
;;                                         1 [(hold sword0 pman "righthand")]
;;                                         2 [(hold battleaxe0 pman "righthand")]
;;                                         3 [(hold mace0 pman "righthand")]
;;                                         4 [(hold morningstar0 pman "righthand")])
;;                                       (case (rand-int 3)
;;                                         0 []
;;                                         1 [(hold shield0 pman "lefthand")]
;;                                         2 [(hold shield1 pman "lefthand")]))))]
;;     {:tag :g :content content}))

;; (defn make []
;;   (let [db (wo)
;;         content [(place (make-man db) 50 200)
;;                  (place (make-man db) 130 200)
;;                  (place (make-man db) 210 200)
;;                  (place (make-man db) 290 200)
;;                  (place (make-man db) 370 200)]
;;         svg (with-out-str (xml/emit {:tag :svg
;;                                      :attrs {:xmlns:svg "http://www.w3.org/2000/svg"
;;                                              :xmlns "http://www.w3.org/2000/svg"
;;                                              :width "420"
;;                                              :height "200"
;;                                              :version "1.1"
;;                                              :style "border: 1px solid black"}
;;                                      :content content}))]
;;     svg))

;; (defn write []
;;   (spit "test.svg" (make)))

(run-tests)
