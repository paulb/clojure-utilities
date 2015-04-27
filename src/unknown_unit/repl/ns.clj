(ns unknown-unit.repl.ns
  (:require [unknown-unit.config :as config]
            [ns-tracker.core :refer :all]))

;; Define this if you want your core functions aliased
;; instead of locally referred.
(def ^:private ns-alias (config/get :ns-alias))

(def ^:private core-namespace
  (cond-> [(symbol (str *ns*))]
          ns-alias (conj :as ns-alias)))

;; Add namespaces you want included, e.g.,
;; [:clojure.set :as set].
(def ^:private user-namespaces
  (let [namespaces (config/get :namespaces)]
    (into (:default namespaces) (:user namespaces))))

;; Always includes the current namespace, so that
;; ns+, ns- and reload-ns are always available.
(def ^:private traveling-namespaces
  (cons core-namespace user-namespaces))

(def ^:private watched-namespaces
  (ns-tracker (into ["src" "test"] (config/get :watch))))

;; Namespace reloading

(declare refer-some)

(defn- load-traveling-namespaces
  []
  (doseq [namespace traveling-namespaces]
    (require (refer-some namespace) :reload)))

(defn init
  []
  (load-traveling-namespaces))

;; NOTE Config reloading is only useful in a dev environment.
;; TODO Add config-dir to config, which allows reloading config
;;      to add new user libraries at any time.
(defn reload-ns
  []
  (doseq [namespace (watched-namespaces)]
    (require namespace :reload))
  (config/reload)
  (load-traveling-namespaces))

;; Namespace traversal

(def ^:private ns-directives #{:as :refer})

(defn- refer-all
  [namespace]
  [(first namespace) :refer :all])

(defn- refer-some
  [namespace]
  (if (some ns-directives namespace)
    namespace
    (refer-all namespace)))

(defmacro follow-ns
  [namespace & {:keys [refer-all?]}]
  (let [ref-fn (if refer-all? refer-all refer-some)]
    `(do
       (ns ~namespace
         (:require ~@(map ref-fn traveling-namespaces))))))

(defmacro ns+
  [namespace]
  `(follow-ns ~namespace :refer-all? true))

(defmacro ns-
  [namespace]
  `(follow-ns ~namespace))
