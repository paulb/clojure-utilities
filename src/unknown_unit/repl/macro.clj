(ns unknown-unit.repl.macro
  (:require [clojure.pprint :refer [pprint]]))

(def referrals ['expand])

(def ^:private expansions
  {:0 'macroexpand
   :1 'macroexpand-1
   :all 'clojure.walk/macroexpand-all})

(defmacro expand
  "Pretty print macro expansion.
  Defaults to macroexpand-1 if given no level or an invalid level.
  Valid levels:
  :0 macroexpand
  :1 macroexpand-1
  :all macroexpand-all"
  ([body] `(expand :1 ~body))
  ([level & body]
   (let [expansion-fn (get expansions level 'macroexpand-1)]
     `(pprint (~expansion-fn '~@body)))))
