(ns unknown-unit.repl.ns
  (:require [unknown-unit.config :as config]
            [ns-tracker.core :refer :all]))

;; Define this if you want your core functions aliased
;; instead of locally referred.
(def ^:private ns-alias (config/get :ns-alias))

(def ^:private core-namespace
  (let [modifier (if ns-alias
                   [:as ns-alias]
                   [:refer '[ns- ns+ reload-ns]])]
    (into [(symbol (str *ns*))] modifier)))

(def ^:private ns-directives #{:as :refer})

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
(def ^:private user-namespaces
  (let [namespaces (config/get :namespaces)]
    (->> (into (:default namespaces) (:user namespaces))
         (map refer-some))))

(def ^:private traveling-namespaces
  (into [core-namespace] user-namespaces))

(def ^:private watched-namespaces
  (ns-tracker (into ["src" "test"] (config/get :watch))))

;; Namespace reloading

;; Always includes the current namespace, so that
;; ns+, ns- and reload-ns are always available.
(defn- load-namespaces
  [namespaces]
  (doseq [namespace namespaces]
    (require namespace :reload)))

(defn init
  []
  (load-namespaces traveling-namespaces))

;; NOTE Config reloading is only useful in a dev environment.
;; TODO Add config-dir to config, which allows reloading config
;;      to add new user libraries at any time.
(defn reload-ns
  []
  (load-namespaces (watched-namespaces))
  (config/reload)
  (load-namespaces traveling-namespaces))

;; Namespace traversal

(defmacro follow-ns
  [namespace & {:keys [refer-all?]}]
  `(ns ~namespace
     (:require ~@(if refer-all?
                   (cons core-namespace (map refer-all user-namespaces))
                   traveling-namespaces))))

(defmacro ns+
  [namespace]
  `(follow-ns ~namespace :refer-all? true))

(defmacro ns-
  [namespace]
  `(follow-ns ~namespace))
