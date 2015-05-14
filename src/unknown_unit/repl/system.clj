(ns unknown-unit.repl.system
  (:require [unknown-unit.config :as config]
            [unknown-unit.repl.ns :as ns])
  (:refer-clojure :exclude [ns]))

(def ^:private auto-refresh? (boolean (config/get :auto-refresh)))

(declare start)

(defn running
  [system]
  (assoc system :running true))

(defn stopped
  [system]
  (assoc system :running false))

(def ^:private system*
  (atom {:running false
         :stop []}))

(defn system
  []
  @system*)

(defn- reloader
  []
  (println "IT FUCKING HATES ME")
  (let [refresh-interval (config/get [:refresh :interval] 500)]
    (future (while (not (Thread/interrupted))
              (println :checking...)
              (Thread/sleep refresh-interval)
              (ns/reload-ns)))))

(defn cu [] [:HEY "O" :I :thInK :it :MIGHt :WOrK!])

(defn start
  ([] (start system*))
  ([system]
   (if-not (:running @system)
     (do
       (println :AAAAAAAAAAAAAAAAAAAAAAAAFUUUUUUUUUUCK!)
       (if-let [auto? (config/get [:refresh :auto])]
         (let [cancel-loader (partial future-cancel (reloader))
               _ (println :cancel-loader cancel-loader)]
           (println "L OR WHATEVER THE FUCK!")
           (swap! system update-in [:stop] conj cancel-loader)
           (swap! system running))
             ; (-> (update-in system [:stop] conj (partial future-cancel (reloader)))
             ;     running)))
         (swap! system running)))
    system)))

(defn stop
  ([] (stop system*))
  ([system]
   (if (:running @system)
     (let [functions (for [fn (:stop @system)] (fn))]
       (println :stopping)
       (doall functions)
       (swap! system assoc :stop [])
       (swap! system stopped))
     system)))

(def ^:private initialized (atom false))

(defn init
  []
  (when-not @initialized
    (println :initializing :system)
    (start)
    ; (ns/load-namespaces (traveling-namespaces))
    ; (let [fuckcunt (start)]
    ;   (println :fuckcunt fuckcunt)
    ;   (stop fuckcunt)
    ;   (swap! system* assoc :started true)
    ;   (swap! system* assoc :jesus :fuck!))
    (reset! initialized true)))

(defn reset
  [system]
  (stop system)
  (start system))

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
