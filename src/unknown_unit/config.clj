(ns unknown-unit.config
  (:require [clojure.core.memoize :as core.memo]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [get]))

(def ^:private config-file "repl.edn")

(defn- config*
  []
  (if-let [file (io/resource config-file)]
    (with-open [reader (-> file io/reader java.io.PushbackReader.)]
      (edn/read reader))
    {}))

(def ^:private config (core.memo/memo config*))

(defn get*
  ([] (config))
  ([key] (get* key nil))
  ([key default]
   (get-in (config) (flatten [key]) default)))

(def get (core.memo/memo get*))

(defn clear
  []
  (core.memo/memo-clear! config)
  (core.memo/memo-clear! get))

(defn reload
  []
  (clear)
  (config))
