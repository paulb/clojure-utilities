(ns unknown-unit.repl.util)

(defn map-all [f & colls]
  "Pads any collection which is shorter than the others.
  Taken from http://stackoverflow.com/a/15771713
  Credit to http://stackoverflow.com/users/625403/amalloy"
  (lazy-seq
    (when (some seq colls)
      (cons (apply f (map first colls))
            (apply map-all f (map rest colls))))))

(defn map-vals
  [f m]
  (->> (map (fn [[k v]] [k (f v)]) m)
       (into {})))

(defn print-passthrough
  "Utility function useful for inserting into threaded code.
  Prints the current value with a prefix
  As threaded values are in uncertain positions in the argument list,
  prefixes must be specified as a keyword beginning with a '-',
  e.g., :-my-var-prefix.
  Returns the input value."
  [& args]
  (let [prefix? (fn [arg] (boolean (and (keyword? arg)
                                        (= (subs (name arg) 0 1) "-"))))
        {prefix true value false} (map-vals first (group-by prefix? args))
        prefix (when prefix (keyword (subs (name prefix) 1)))
        print-me (remove nil? [:threaded-val prefix value])]
    (apply println print-me)
    value))
