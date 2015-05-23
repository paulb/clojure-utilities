(ns unknown-unit.repl.ns
  (:require [clojure.string :as str]
            [ns-tracker.core :refer :all]
            [unknown-unit.config :as config])
  (:use [clojure.java.io]))

;; This describes future functionality. Built in namespaces
;; will be aliased according to internal rules.
;; Namespaces are defined in require format:
;; [unladen.swallow]
;; [unladen.swallow :as swallow]
;; [laden.swallow :refer [coconuts]]
;; [laden.swallow :as swallow :refer [coconuts]]
;; [vorpal.rabbit :refer :all] ; this may be dangerous; beware naming conflicts.
;; [vorpal.rabbit] implies the default u-/

(def referrals '[ns- ns+ reload-ns])

(def ^:private default-ns "u-")
(def ^:private imports '[ns capture macro system])
(def ^:private local-ns (symbol (str *ns*)))
;; Define this if you want your core functions aliased to a particular namespace.
;; If nil, most functions will be namespaced to the default u-/
;; Exceptions are the namespace control functions, ns-, ns+, reload-ns.
;; Since we can't control loading order this is likely the safest approach.
(def ^:private util-ns (or (config/get :util-ns) default-ns))
(def ^:private ns-prefix (str/replace (str *ns*) #"\.[^\.]*$" ""))
(def ^:private ns-directives #{:as :refer})

(defn- aliased
  [namespace]
  (let [aliased (symbol (str util-ns "." suffix))]
    (conj namespace :as aliased)))

(defn- referred
  [namespace]
  (let [ns-name (first namespace)]
    (when-not (= ns-name local-ns) (require ns-name))
    (->> (var-get (intern ns-name 'referrals))
         (conj namespace :refer))))

(defmacro intern-user-functions
  [qualified-name]
  ;; This may not be necessary. Or should be changed to only do non 'repl.ns
  (when-not (= qualified-name local-ns)
    (require qualified-name))
  (let [referrals (->> (var-get (intern qualified-name 'referrals))
                       (map #(vector (symbol (str qualified-name "/" %) %))))]
    `~(
       ; doseq [[name var] referrals]
       ;  (println :name (class name))
       ;  (println :var (class var))
        #_(intern 'u- name var))))

       (intern ~'u- fn# #'~qualified-name/fn#))))

(defn- core-namespaces*
  []
  (map (fn [name]
         (let [qualified-name (symbol (str ns-prefix "." name))]
           (if (= name 'ns)
             (referred [qualified-name])
             (intern-user-functions qualified-name))))
       imports))

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
  ; (println :loading-mod (modified-namespaces))
  ; (println :loading-traveling (traveling-namespaces))
  (load-namespaces (modified-namespaces))
  (config/reload)
  (load-namespaces (traveling-namespaces)))

; (defmacro override-ns
;   []
;   (if (config/get :override-ns)
;   ;   ; (do
;       (and (not (println :override-ns))
;            `(defmacro ~'fweep [~'name] (println :fweep!) `(ns- ~'dave)))))

(defn init
  []
  (println :innit?)
  (create-ns util-ns)
  (load-namespaces (traveling-namespaces)))
  ; (override-ns))

(defmacro follow-ns
  "Changes to a new namespace and requires specified namespaces.
  Namespaces are aliased or referred as declared.
  Not intended for direct use. Use ns- or ns+ instead."
  [namespace & {:keys [refer-all?]}]
  `(ns ~namespace
     (:require ~@(if refer-all?
                   (into (core-namespaces) (map refer-all (user-namespaces)))
                   (traveling-namespaces)))))

(defmacro ns-
  "Changes to a new namespace and requires specified namespaces.
  Namespaces are aliased or referred as declared."
  [namespace]
  `(follow-ns ~namespace))

(defmacro ns+
  "Changes to a new namespace and requires specified namespaces.
  All functions are referred locally."
  [namespace]
  `(follow-ns ~namespace :refer-all? true))
;     (defmacro ns
;       [name]
;       `(ns- ~'dave)))
; )
  ;   [namespace]
  ;   `(ns- ~namespace))
  ; (require '[clojure.core :only [ns]]))

; (defmacro ns
;   [name]
;   `(ns- ~name))

