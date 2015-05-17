(ns unknown-unit.repl.capture
  (require [unknown-unit.repl.system :as system]))

(def ^:private valid-operations #{'def 'defn})

(def referrals '[capture modify local local-multi])

(defn- store
  [op name body]
  (let [expression `(~op ~name ~@body)]
    (system/capture name expression)
    expression))

(defmacro capture
  "Facilitates capture of `def` and `defn` expressions.
  The expressions can then be added to the output of
  clojure.repl/source (not yet implemented.)
  They can optionally be saved for future sessions (not yet implemented.)
  Expressions will not be overwritten unless forced (not yet implemented.)
  The expression is then returned for evaluation in the current namespace."
  [op name & body]
  (if (contains? valid-operations op)
    (if-let [existing-expression (system/existing-expression name)]
      (do
        (println name "is already defined. Use `modify` to overwrite.")
        existing-expression)
      (store op name body))
    (println op "not supported")))

(defmacro modify
  "Modify a captured expression."
  [op name & body]
  (store op name body))

(defmacro local
  "Imports the specified expression into the local namespace."
  [name]
  (if-let [expression (system/existing-expression name)]
    expression
    (println name "not found")))

(defmacro local-multi
  "Imports the specified expressions into the local namespace."
  [& names]
  (println :.....)
  (println :.....)
  (println :.....)
  `~(keep system/existing-expression names))
