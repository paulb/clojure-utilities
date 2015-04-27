(defproject unknown-unit/clojure-utils "0.1.0-SNAPSHOT"
  :description "A set of miscellaneous utilities for Clojure development"
  :url "https://bitbucket.org/unknown-unit/clojure-utilities"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [ns-tracker "0.2.2"]]
  :main unknown-unit.repl.core
  :min-lein-version "2.0.0"
  :resource-paths ["config"])
