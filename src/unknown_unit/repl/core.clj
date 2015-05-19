(ns unknown-unit.repl.core
  (:require [unknown-unit.repl.ns :as ns]
            [unknown-unit.repl.system :as system])
  (:refer-clojure :exclude [ns]))

(def ^:private initialized (atom false))

(defn init
  []
  (when-not @initialized
    (ns/init)
    (system/configure :ns-refresh ns/reload-ns)
    (system/start)
    (reset! initialized true)))

(defn re-init
  []
  (system/stop)
  (reset! initialized false)
  (init))
