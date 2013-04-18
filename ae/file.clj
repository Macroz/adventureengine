(ns ae.file
  (:import [java.io File FileInputStream BufferedInputStream]))

(defn load-stream
  "Load resources from classpath."
  [name]
  (let [ldr (.getContextClassLoader (Thread/currentThread))
        is (.getResourceAsStream ldr name)]
    is))

(defn current-directory []
  (. (File. ".") getCanonicalPath))
