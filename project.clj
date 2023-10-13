(defproject com.github.aivanovski/pico-automator-clojure "0.0.11"
  :description "Functional API for pico-automator UI automation library"
  :url "https://github.com/aivanovski/pico-automator-clojure"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/"}

  :repositories [["jitpack" "https://jitpack.io"]]

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.jetbrains.kotlin/kotlin-stdlib-jdk8 "1.8.22"]
                 [com.github.aivanovski/pico-automator "0.0.11"]]

  :repl-options {:init-ns picoautomator.core})
