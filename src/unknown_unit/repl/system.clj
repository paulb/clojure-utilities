(ns unknown-unit.repl.system
  (:require [unknown-unit.config :as config]
            [unknown-unit.repl.ns :as ns])
  (:refer-clojure :exclude [ns]))

(def ^:private auto-refresh? (boolean (config/get :auto-refresh)))
(def ^:private initialized (atom false))

(def ^:private system*
  (atom {:running false
         :stop []}))

(defn system
  []
  @system*)

(defn running
  [system]
  (assoc system :running true))

(defn stopped
  [system]
  (assoc system :running false))

(defn- reloader
  []
  (let [refresh-interval (config/get [:refresh :interval] 500)]
    (future (while (not (Thread/interrupted))
              (Thread/sleep refresh-interval)
              (let [state {:initialized @initialized
                           :system @system*}]
                (ns/reload-ns)
                (reset! initialized (:initialized state))
                (reset! system* (:system state)))))))

(defn start
  ([] (start system*))
  ([system]
   (if-not (:running @system)
     (if-let [auto? (config/get [:refresh :auto])]
       (let [cancel-loader (partial future-cancel (reloader))]
         (swap! system update-in [:stop] conj cancel-loader)
         (swap! system running))
       (swap! system running))
    system)))

(defn ok [] :corral)

(defn stop
  ([] (stop system*))
  ([system]
   (if (:running @system)
     (let [operations (for [fn (:stop @system)] (fn))]
       (doall operations)
       (swap! system assoc :stop [])
       (swap! system stopped))
     system)))

(defn init
  []
  (when-not @initialized
    ;; This still needs work
    ; (ns/load-namespaces (traveling-namespaces))
    (start)
    (reset! initialized true)))

(defn reset
  ([] (reset system*))
  ([system]
   (stop system)
   (start system)))

(defn -start
  [this]
  (start))

(defn -stop
  [this]
  (stop))

(defn -destroy
  [this]
  ;; no-op
  )

(defn -main
  [& args]
  (.addShutdownHook (Runtime/getRuntime) (do (stop) (Thread. stop)))
  (start))

(def atom-test (atom {}))

(defn moo-cow
  [thing]
  (assoc thing :moooooo! "hello cow!"))

(def fuck (atom ()))
