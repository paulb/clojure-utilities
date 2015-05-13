(ns unknown-unit.repl.ns
  (:require [clojure.string :as str]
            [ns-tracker.core :refer :all]
            [unknown-unit.config :as config])
  (:use [clojure.java.io])
  (:refer-clojure :exclude [namespace ns-imports ns-name]))

;; Namespaces are defined in require format:
;; [unladen.swallow]
;; [unladen.swallow :as swallow]
;; [laden.swallow :refer [coconuts]]
;; [laden.swallow :as swallow :refer [coconuts]]

(def ^:private auto-refresh? (boolean (config/get :auto-refresh)))
(def ^:private local-ns (symbol (str *ns*)))
;; Define this if you want your core functions aliased
;; instead of locally referred.
(def ^:private ns-alias (config/get :ns-alias))
(def ^:private ns-prefix (str/replace (str *ns*) #"\.[^\.]*$" ""))
(def ^:private ns-imports '[ns macro])
(def ^:private ns-directives #{:as :refer})

(def referrals '[ns- ns+ reload-ns])

(defn- aliased
  [namespace suffix]
  (let [aliased (symbol (str ns-alias "." suffix))]
    (conj namespace :as aliased)))

(defn- referred
  [namespace]
  (let [ns-name (first namespace)]
    (when-not (= ns-name local-ns) (require `[~ns-name]))
    (let [referrals (var-get (intern ns-name 'referrals))]
      (conj namespace :refer referrals))))

(defn- core-namespaces*
  []
  (let [namespaces (->> (map #(str ns-prefix "." %) ns-imports)
                        (map (comp vector symbol)))]
    (if ns-alias
      (mapv aliased namespaces ns-imports)
      (mapv referred namespaces))))

(def ^:private core-namespaces (memoize core-namespaces*))

(defn- refer-all
  [namespace]
  [(first namespace) :refer :all])

(defn- refer-some
  [namespace]
  (if (some ns-directives namespace)
    namespace
    (refer-all namespace)))

;; Add namespaces you want included, e.g.,
;; [:clojure.set :as set].
(defn- user-namespaces*
  []
  (->> (vals (config/get :namespaces))
       (apply concat)
       (map refer-some)
       (into [])))

(def ^:private user-namespaces (memoize user-namespaces*))

(defn- traveling-namespaces*
  []
  (into (core-namespaces) (user-namespaces)))

(def ^:private traveling-namespaces (memoize traveling-namespaces*))
(def ^:private modified-namespaces (ns-tracker (into ["src" "test"] (config/get :watch))))

(defn- load-namespaces
  [namespaces]
  (doseq [namespace namespaces]
    (require namespace :reload)))

;; NOTE Config reloading is only useful in a dev environment.
;; TODO Don't do it unless in dev environment
;;      (requires environment awareness, this can go in config)
;; TODO Add config-dir to config, which allows reloading config
;;      to add new user libraries at any time.
(defn reload-ns
  []
  ;; TODO Don't need to load a modified namespace if
  ;;      it's a traveling namespace.
  (load-namespaces (modified-namespaces))
  (config/reload)
  (load-namespaces (traveling-namespaces)))

(defmacro follow-ns
  [namespace & {:keys [refer-all?]}]
  `(ns ~namespace
     (:require ~@(if refer-all?
                   (into (core-namespaces) (map refer-all (user-namespaces)))
                   (traveling-namespaces)))))

(defmacro ns+
  [namespace]
  `(follow-ns ~namespace :refer-all? true))

(defmacro ns-
  [namespace]
  `(follow-ns ~namespace))

;; Initialize/Start/Stop

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

(defn start
  ([] (println :default-start) (start system*))
  ([system]
   (if-not (:running system)
     (do
       (println :repl-util :tools :start
                :with system)
       (let [{:keys [auto interval]} (config/get :refresh)]
         (if auto
           (let [motherfucker "English, do you speak it?"
                 loader (future (while true (println :hey) (Thread/sleep 1000)))]
         ;   (let [loader (future (while true
         ;                          (Thread/sleep interval)
         ;                          (reload-ns)))]
         ;     (-> (update-in system [:stop] conj `(future-cancel ~loader))
         ;         running)
             ; (running system)))
             (-> (update-in system [:stop] conj (fn [] (future-cancel loader)))
                 running))
           (running system))))
     system)))
   ; (if-not (:running system)
   ;   (do
   ;     (println :auto-refresh-cfg (config/get :auto-refresh))
   ;     (println :auto-refresh auto-refresh?)
   ;     (load-namespaces (traveling-namespaces))
   ;     (if auto-refresh?
   ;       (let [refresh-interval (config/get :refresh-interval 500)]
   ;         (println :refreshing--)
   ;         (let [refresh-ms (config/get :refresh-ms)
   ;               _ (println :and :then...)
   ;               loader (future (while true
   ;                                (Thread/sleep refresh-interval)
   ;                                (reload-ns)))
   ;               _ (println :and :after :that...)]
   ;           (-> (update-in system [:stop]
   ;                          conj `(future-cancel ~loader))
   ;               running
   ;               ((fn [x]
   ;                  (println)
   ;                  (println :current-system x)
   ;                  (println)
   ;                  x)))))
   ;       (running system)))
   ;   system)))

; (def ^:private initialized (atom false))

; (defn init
;   []
;   (when-not @initialized
;     (println :initialize :system)
;     (reset! initialized true)
;     (swap! system* start)
;     (println :siestem @system*)
;     (println :SYSTEM (system))))

(def ^:private initialized (atom false))

(defn init
  []
  (when-not @initialized
    (println :initializing :ns-controller)
    (load-namespaces (traveling-namespaces))
    (swap! system* start)
    (reset! initialized true)))

(defn stop
  ([] (stop system*))
  ([system]
   (when (:running @system)
     (println :stopping)
     (doall (for [fn (:stop @system)] (fn)))
     (swap! system assoc :stop [])
     (swap! system stopped))))

(defn reset
  [system]
  (stop system)
  (start system))

(defn -start
  [this](println :this-one!!!!!!!)
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

