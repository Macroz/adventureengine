(ns ae.svg.core
  (:require [clojure.xml :as xml])
  (:require [clojure.string :as string])
  (:use [clojure.test])
  (:require [ae.file :as file])
  (:require [ae.vec2 :as v]))


(defn svg-translate [s]
  (let [m (re-matches #"\s*translate\(([-\d.]+),([-\d.]+)\)\s*" s)]
    (if m
      (vec (map read-string (take-last 2 m)))
      [0 0])))

(deftest svg-translate-test
  (is (= [39.031125 219.34909] (svg-translate "translate(39.031125,219.34909)"))))

(defn extract-coordinates [match]
  (vec (map read-string (take-last 2 match))))

(defn svg-d-coordinates [d]
  (let [matches (re-seq #"([-\d.]+),([-\d.]+)" d)]
    (if matches
      (map extract-coordinates matches))))

(defn svg-d-parts [d]
  (string/split #"\s" d))

(deftest svg-d-coordinates-test
  (is (= (list [94.015495 414.41899] [3.10984 5.38639] [6.21967 0]) (svg-d-coordinates "m 94.015495,414.41899 3.10984,5.38639 -6.21967,0 z"))))

(defn translate-svg-d-part [translation part]
  (let [match (re-matches #"([-\d.]+),([-\d.]+)" part)]
    (if match
      (apply str (interpose "," (v/sum translation (extract-coordinates match))))
      part)))

(defn transform-path [path translation]
  (let [d (get-in path [:attrs :d])]
    (if d
      (assoc-in path [:attrs :d] (apply str (interpose " " (map (partial translate-svg-d-part translation) (svg-d-parts d)))))
      path)))

(deftest transform-path-test
  (is (= "m -5,-5 5,-5 5,5 z" (transform-path {:tag :path :attrs {:translation "translate(10,10)" :d "m 0,0 10,0 10,10 z"}} [-5 -5]))))

(defn first-svg-path-point [d]
  (let [m (re-matches #"\w+ ([-\d.]+),([-\d.]+).*" d)]
    (when m
      (vec (map read-string (take-last 2 m))))))

(deftest first-svg-path-point-test
  (is (= [70.054589 59.634775] (first-svg-path-point "m 70.054589,59.634775 52.622471,0.505076 6.62247,0 0,5.303301 -6.62247,0 -52.622471,0.505076 z"))))


(defn add [object v]
  {:tag :g :attrs {:transform (str "translate(" (first v) "," (second v) ")")} :content [object]})

(defn sub [object v]
  (add object (map - v)))

(defn transform-to-origo [objects]
  (map (fn [o] (dissoc-in o [:attrs :transform])) objects))

(defn rotate [object angle location]
  {:tag :g :attrs {:transform (str "rotate(" angle " " (first location) " " (second location) ")")} :content [object]})

(defn scale [object sx sy]
  {:tag :g :attrs {:transform (str "scale(" sx " " sy ")")} :content [object]})
