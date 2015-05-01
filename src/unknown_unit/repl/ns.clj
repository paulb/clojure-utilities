(ns unknown-unit.repl.ns
  (:require [ns-tracker.core :refer :all]))

;; Define this if you want your core functionality namespaces instead of locally referred.
;; Requires a symbol, e.g., 'core
(def ^:private ns-alias nil)

;; If you want to alias the main namespace to something,
;; modify like this (assuming my-util is the desired name):
;; [~(symbol (str *ns*)) :as 'my-util]
(def ^:private core-namespace
  (let [ns-symbol [(symbol (str *ns*))]]
    (if ns-alias
      (conj ns-symbol :as ns-alias)
      ns-symbol)))

;; Always includes the current namespace, so that
;; ns+, ns- and reload-ns are always available.
;; Add namespaces you want included, e.g.,
;; [:clojure.set :as set].
(def ^:private user-namespaces
  '[])

(def ^:private follow-namespaces
  (cons core-namespace user-namespaces))

(def ^:private modified-namespaces
  (ns-tracker ["src" "test"]))

;; Namespace reloading tools.

(declare refer-some)

(defn bootstrap
  []
  (refer (first core-namespace)))

(defn reload-ns
  []
  (doseq [ns-sym (modified-namespaces)]
    (require ns-sym :reload))
  (require (refer-some core-namespace) :reload))

;; Namespace traversal tools.

(defn- refer-all
  [namespace]
  [(first namespace) :refer :all])

(defn- refer-some
  [namespace]
  (if (some #{:as :refer} namespace)
    namespace
    (refer-all namespace)))

(defmacro follow-ns
  [namespace & {:keys [refer-all?]}]
  (let [ref-fn (if refer-all? refer-all refer-some)
        follow-namespaces (map ref-fn follow-namespaces)]
    `(do
       (reload-ns)
       (ns ~namespace
         (:require ~@follow-namespaces)))))

(defmacro ns+
  [namespace]
  `(follow-ns ~namespace :refer-all? true))

(defmacro ns-
  [namespace]
  `(follow-ns ~namespace))
