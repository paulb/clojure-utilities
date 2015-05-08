(ns unknown-unit.repl.util)

(defn map-all [f & colls]
  "Pads any collection which is shorter than the others.
  Taken from http://stackoverflow.com/a/15771713
  Credit to http://stackoverflow.com/users/625403/amalloy"
  (lazy-seq
    (when (some seq colls)
      (cons (apply f (map first colls))
            (apply map-all f (map rest colls))))))
