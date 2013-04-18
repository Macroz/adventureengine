(ns nightbyday.server
  (:use compojure.core)
  (:use clojure.test)
  (:use [clojure.walk :only [keywordize-keys]])
  (:use [ring.middleware.params :only [wrap-params]])
  (:require [compojure.route :as route])
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:use [hiccup core element page]))

(defn link-image [src]
  [:a {:href src}
   [:img {:src src
          :width 300
          :height 200}]])

(defn welcome-page []
  (html5 [:html
          [:head
           [:title "Night By Day"]
           (include-css "css/welcome.css")]
          [:body
           [:div.center
            [:div.content
             [:h1 "Night By Day"]
             [:p "A brutal murder mystery game entry to " [:a {:href "http://www.ludumdare.com"} "Ludum Dare #25"] ". Not for the faint of heart!"]
             [:p "By Markku Rontu / markku.rontu@iki.fi / @zorcam"]
             [:div.center
              [:p.play [:a {:href "/game"} "Play"]]]
             [:div.center
              [:p.screenshots (link-image "img/screenshot1.png")
               (link-image "img/screenshot2.png")]]
             [:p "I enjoyed this second time of participation."]
             [:p "I wasn't as well prepared as last time, but I had played with the right technology recently."
              [:ul [:li "ClojureScript was definitely a good choice. There isn't much code. I can make it even less by some refactoring, but there is never time in compos."]
               [:li "Raphael.js is pretty easy to work with. I could've used some more examples, but that was not a big problem."]
               [:li "I didn't have time for sounds or music. I was just too lazy :-)"]
               [:li "Saturday evening with champaign was fun ;-)"]
               [:li "Concentrating on a short intensive sprint seems like a good way to get things done!"]
               [:li "I use a bit of Inkscape like last time, but also decided to re-learn some Blender."]
               [:li "I more or less managed to finish the first day of the game (of four days). Game is winnable!"]]]
             [:p "Thanks, have fun, more to come!"]]]]]))

(defn game-page [params]
  (html5 [:html
          [:head
           [:title "Night By Day"]
           [:meta {:name "viewport"
                   :content "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"}]
           [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
           [:meta {:name "apple-mobile-web-app-status-bar-style" :content "black-translucent"}]
           (include-css "css/main.css")
           (include-js "js/raphael-min.js")
           (include-js "js/raphael-zpd.js")
           (include-js "js/hammer.js")
           (include-js "js/cljs.js")]
          [:body {:onload (str "nightbyday.main.startup(" (params :delay) ");")}
           [:div.content]]]))
;; [:div.game
;;  [:div#paper]
;;  [:div.tasks.block]
;;  [:div.info.block]
;;  [:div.results.block]]
;; [:div.demo]]]))

(defroutes handler
  (GET "/" [] (welcome-page))
  (GET "/game" {params :params} (game-page (keywordize-keys params)))
  (route/resources "/")
  (route/not-found "Page not found!"))

(def app (-> handler
             wrap-params
             ))

(def server (atom nil))

(defn- run []
  (when @server
    (.stop @server))
  (swap! server (fn [old]
                  (run-jetty #'app {:port 8080 :join? false}))))

(defn -main [port]
  (run-jetty #'app {:port (Integer. port)}))


(def kirjaimet (char-array (for [n (range 26)] (char (+ (int (.charValue \a)) n)))))
(def uppercase (char-array (for [n (range 26)] (char (+ (int (.charValue \A)) n)))))
(def numerot (char-array (for [n (range 10)] (char (+ (int (.charValue \0)) n)))))

(defn salasana []
  (let [ks (inc (rand-int 6))
        us (inc (rand-int (- 6 ks)))
        ns (- 8 ks us)]
    (apply str (shuffle (concat (repeatedly ks (fn [] (aget kirjaimet (rand-int 26))))
                                (repeatedly us (fn [] (aget uppercase (rand-int 26))))
                                (repeatedly ns (fn [] (aget numerot (rand-int 10)))))))))

(def luokat ["abcdefghijklmnopqrstuvwxyz"
             "ABCDEFGHIJKLMNOPQRSTYVWXYZ"
             "1234567890"])

(defn salasana2 []
  (apply str (for [c (range 8)]
               (rand-nth (rand-nth luokat)))))

(defn calc-dist []
  (apply concat (for [k (range 1 8)
                      u (range 1 8)
                      n (range 1 8)
                      :when (= (+ k u n) 8)]
                  [k u n])))

(def dist (vec (calc-dist)))

(defn salasana3 []
  (let [merkit (vec "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTYVWXYZ1234567890")
        dist [1 1 6 1 2 5 1 3 4 1 4 3 1 5 2 1 6 1 2 1 5 2 2 4 2 3 3 2 4 2 2 5 1 3 1 4 3 2 3 3 3 2 3 4 1 4 1 3 4 2 2 4 3 1 5 1 2 5 2 1 6 1 1]
        [k u n] (take 3 (drop (* (rand-int 21) 3) dist))
        rand-merkit (fn [x xs] (repeatedly x (partial rand-nth xs)))]
    (apply str (shuffle (concat (rand-merkit k (subvec merkit 0 26))
                                (rand-merkit u (subvec merkit 26 52))
                                (rand-merkit n (subvec merkit 52 62)))))))

(defn salasana4 [] (apply str (repeatedly 8 (partial rand-nth "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTYVWXYZ1234567890"))))

(defn salasana5 []
  (let [merkit ["abcdefghijklmnopqrstuvwxyz" "ABCDEFGHIJKLMNOPQRSTUVWXYZ" "1234567890"]
        n1 (inc (rand-int 6))
        n2 (inc (rand-int (- 6 n1)))
        n3 (- 8 n1 n2)]
    (apply str (shuffle (concat (repeatedly n1 #(rand-nth (merkit 0)))
                                (repeatedly n2 #(rand-nth (merkit 1)))
                                (repeatedly n3 #(rand-nth (merkit 2))))))))

(defn salasana6 []
  (let [n1 (inc (rand-int 6))
        n2 (inc (rand-int (- 6 n1)))
        n3 (- 8 n1 n2)
        rpt (fn [n xs] (repeatedly n #(rand-nth xs)))]
    (apply str (shuffle (concat (rpt n1 "abcdefghijklmnopqrstuvwxyz")
                                (rpt n2 "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                                (rpt n3 "1234567890"))))))

(defn salasana7 {:static true} []
  (let [n1 (inc (rand-int 6))
        n2 (inc (rand-int (- 6 n1)))
        n3 (- 8 n1 n2)
        rpt (fn [n xs] (repeatedly n #(rand-nth xs)))]
    (apply str (shuffle (apply concat (map rpt [n1 n2 n3] ["abcdefghijklmnopqrstuvwxyz"
                                                           "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                                           "1234567890"]))))))

(defn salasana8 []
  (let [n1 (inc (rand-int 6))
        n2 (inc (rand-int (- 7 n1)))
        n3 (- 8 n1 n2)]
    (apply str (shuffle (concat (repeatedly n1 #(rand-nth "abcdefghijklmnopqrstuvwxyz"))
                                (repeatedly n2 #(rand-nth "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                                (repeatedly n3 #(rand-nth "1234567890")))))))

(defn draw-set [max & mins]
  (cond (empty? mins) nil
        (= 1 (count mins)) [max]
        :else (let [n (+ (rand-int (- max (apply + (rest mins)))) (first mins))]
                (cons n (apply draw-set (- max n) (rest mins))))))

(defn salasana9 []
  (let [[n1 n2 n3 n4] (draw-set 8 1 1 1 1)]
    (apply str (shuffle (concat (repeatedly n1 #(rand-nth "abcdefghijklmnopqrstuvwxyz"))
                                (repeatedly n2 #(rand-nth "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
                                (repeatedly n3 #(rand-nth "1234567890"))
                                (repeatedly n4 #(rand-nth "!#$%&/(),.-_<>?+-/|\\")))))))

(defn salasana [max & sets]
  (let [sets (partition 2 sets)
        mins (map first sets)
        chars (map second sets)
        ns (apply draw-set max mins)]
    (apply str (shuffle (loop [results []
                               ns ns
                               chars chars]
                          (if (empty? ns) results
                              (recur (concat (repeatedly (first ns) #(rand-nth (first chars))) results)
                                     (rest ns) (rest chars))))))))

(defn salasana10 []
  (salasana 10
            1 "abcdefghijklmnopqrstuvwxyz"
            1 "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            1 "1234567890"
            1 "!#$%&/(),.-_<>?+-/|\\"))

(defn salasana11 []
  (salasana 8
            1 "abcdefghijklmnopqrstuvwxyz"
            1 "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            1 "1234567890"))

(defn sortti [coll]
  (cond (not coll) []
        (< (count coll) 2) coll
        :else (let [pivot (first coll)
                    small (filter #(< % pivot) (rest coll))
                    big (filter #(<= pivot %) (rest coll))]
                (concat (sortti small) [pivot] (sortti big)))))

(is (= [] (sortti [])))
(is (= [1] (sortti [1])))
(is (= [1 2] (sortti [2 1])))
(is (= [1 1 1 2 2 2 3 3 3] (sortti [3 2 2 1 1 1 2 3 3])))

(defn qsort [coll]
  (if (< (count coll) 2) coll
      (let [pivot (first coll)
            {small true big false} (group-by #(<= % pivot) (rest coll))]
        (concat (qsort small) [pivot] (qsort big)))))

