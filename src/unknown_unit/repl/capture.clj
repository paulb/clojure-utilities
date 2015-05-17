(ns unknown-unit.repl.capture
  (require [unknown-unit.repl.system :as system]))

(def ^:private valid-operations #{'def 'defn})

(def referrals '[capture])

(defmacro capture
  "Facilitates capture of `def` and `defn` expressions.
  The expressions can then be added to the output of
  clojure.repl/source (not yet implemented.)
  They can optionally be saved for future sessions (not yet implemented.)
  Expressions will not be overwritten unless forced (not yet implemented.)"
  [op name & body]
  (println :capturing op name body)
  (if (contains? valid-operations op)
    (if-let [existing-definition (system/existing-definition name)]
      (do
        (println name "is already defined. Apply :force to overwrite.")
        existing-definition) ;; probably don't need to return it.
      (let [definition `(~op ~name ~@body)]
        (system/capture name definition)
        definition))
    (println op "not supported")))
