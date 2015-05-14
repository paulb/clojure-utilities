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

;; TODO Don't need to backup/restore state if nothing is reloaded.
(defn- reloader
  "Returns a future running a task to check for and reload
  changed namespaces. System state is backed up, and restored
  after reloading."
  []
  (let [refresh-interval (config/get [:refresh :interval] 500)]
    (future (while (not (Thread/interrupted))
              (Thread/sleep refresh-interval)
              (let [state {:initialized @initialized
                           :system @system*}]
                (ns/reload-ns)
                (reset! initialized (:initialized state))
                (reset! system* (:system state)))))))

;; TODO Ability to inject external dependencies into the system.
(defn start
  "An existing system can be externally supplied, or the
  current system will be used. When first starting a session,
  this will use the default empty system.
  If the configuration declares the system should auto-refresh namespaces,
  a thread is spun off to manage this task and the system is given a
  cancellation function."
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
  "An existing system can be externally supplied, or the
  current system will be used. Stopping a system will end
  the namespace reloading task if one was running."
  ([] (stop system*))
  ([system]
   (if (:running @system)
     (let [operations (for [fn (:stop @system)] (fn))]
       (doall operations)
       (swap! system assoc :stop [])
       (swap! system stopped))
     system)))

(defn init
  "Will only work once. A previous session which has been
  (stop)ped should be re(start)ed."
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
