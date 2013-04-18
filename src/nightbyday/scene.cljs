(ns nightbyday.scene
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em])
  (:use [nightbyday.util :only [log clj->js]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn- rename-to-old! []
  (em/at js/document
         [".scene"] (em/chain (em/add-class "scene-old")
                              (em/remove-class "scene")
                              )))

(defn- insert-scene! [scene]
  (em/at js/document
         [".scene-old"] (em/after scene)))

(defn- transition-to-new! [delay]
  (em/at js/document
         [".scene-old"] (em/chain (em/fade-out delay)
                                  (em/substitute ""))
         [".scene"] (em/chain (em/fade-in delay))))

(defn show-scene! [scene delay]
  (log "Showing scene " (scene :id))
  (rename-to-old!)
  (insert-scene! (scene :content))
  (transition-to-new! delay))
