(ns unknown-unit.repl.core
  (:require [unknown-unit.repl.ns :as ns]
            [unknown-unit.repl.system :as system])
  (:refer-clojure :exclude [ns]))

(defn init
  []
  (ns/init)
  (system/configure :ns-refresh ns/reload-ns)
  (system/start))
