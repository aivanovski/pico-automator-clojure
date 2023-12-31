(ns picoautomator.utils

  (:require [clojure.core :as core]
            [clojure.stacktrace :refer [print-stack-trace]])

  (:import [com.github.aivanovski.picoautomator.domain.entity Duration ElementReference]
           [com.github.aivanovski.picoautomator.presentation OutputWriter StandardOutputFlowReporter]))

(defn to-element
  [data]
  (cond
    (:id data) (ElementReference/id (:id data))
    (:text data) (ElementReference/text (:text data))
    (:contains-text data) (ElementReference/containsText (:contains-text data))
    (:content-desc data) (ElementReference/contentDesc (:content-desc data))
    :else (throw
            (let [message (format "Invalid reference to element: %s" data)]
              (IllegalArgumentException. message)))))

(defn to-duration
  [data]
  (cond
    (:seconds data) (Duration/seconds (:seconds data))
    (:milliseconds data) (Duration/millis (:milliseconds data))
    (:millis data) (Duration/millis (:millis data))
    :else (throw
            (let [message (format "Invalid duration: %s" data)]
              (IllegalArgumentException. message)))))

(defn create-writer
  []
  (reify OutputWriter
    (print [this text] (do
                         (core/print text)
                         (core/flush)))
    (println [this line] (core/println line))
    (printStackTrace [this exception] (print-stack-trace exception))))

(defn setup-flow-reporter
  []
  (if (nil? (StandardOutputFlowReporter/getDefaultReporter))
       (StandardOutputFlowReporter/newReplReporter (create-writer))
       (StandardOutputFlowReporter/getDefaultReporter)))
