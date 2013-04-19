(ns ae.util)

(defmacro up! [& forms]
  `(swap! data (fn [data]
                 ~@forms)))