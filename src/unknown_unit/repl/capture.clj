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
  (cond
    (not (contains? valid-operations op)) :op-not-supported
    (system/existing-expression name) :expression-already-exists
    :else (store op name body)))

(defmacro modify
  "Modify a captured expression."
  [op name & body]
  (cond
    (not (contains? valid-operations op)) :op-not-supported
    (not (system/existing-expression name)) :expression-not-found
    :else (store op name body)))

(defmacro local-multi
  "Imports the specified expressions into the local namespace."
  [& names]
  (println :names names)
  `[~@(keep system/existing-expression names)])

(defmacro local
  "Imports the specified expression into the local namespace."
  [name]
  `(local-multi ~name))
