(ns unknown-unit.repl.ns
  (:require [clojure.string :as str]
            [ns-tracker.core :refer :all]
            [unknown-unit.config :as config])
  (:refer-clojure :exclude [ns-imports]))

;; Define this if you want your core functions aliased
;; instead of locally referred.
(def ^:private ns-alias (config/get :ns-alias))
(def ^:private ns-imports ['ns])
(def ^:private referrals ['ns- 'ns+ 'reload-ns])

(defn- aliased
  [namespace suffix]
  (let [aliased (symbol (str ns-alias "." suffix))]
    (conj namespace :as aliased)))

(defn- referred
  [namespace]
  (let [referrals (var-get (intern (first namespace) 'referrals))]
    (conj namespace :refer referrals)))

(def ^:private core-namespaces
  (let [ns-prefix (str/replace (str *ns*) #"\.[^\.]*$" "")
        namespaces (->> (map #(str ns-prefix "." %) ns-imports)
                        (map (comp vector symbol)))]
    (if ns-alias
      (mapv aliased namespaces ns-imports)
      (mapv referred namespaces))))

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
  (into core-namespaces user-namespaces))

(def ^:private watched-namespaces
  (ns-tracker (into ["src" "test"] (config/get :watch))))

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

(defmacro follow-ns
  [namespace & {:keys [refer-all?]}]
  `(ns ~namespace
     (:require ~@(if refer-all?
                   (into core-namespaces (map refer-all user-namespaces))
                   traveling-namespaces))))

(defmacro ns+
  [namespace]
  `(follow-ns ~namespace :refer-all? true))

(defmacro ns-
  [namespace]
  `(follow-ns ~namespace))
