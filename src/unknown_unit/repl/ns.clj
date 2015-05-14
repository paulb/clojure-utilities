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

