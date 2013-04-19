(ns ae.util.macros)

(defmacro up!-> [var & body]
  `(swap! ~var (fn [data#]
                 (-> data#
                     ~@body))))

(defmacro deftrace [name args & body]
  `(defn ~name ~args
     ;;(ae.util.core/log ~(str name) " begin")
     (let [result# (do ~@body)]
       ;;(ae.util.core/log ~(str name) " end")
       result#)))
