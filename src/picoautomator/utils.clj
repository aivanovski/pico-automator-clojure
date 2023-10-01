(ns picoautomator.utils

  (:require [clojure.core :as core]
            [clojure.stacktrace :refer [print-stack-trace]])

  (:import [com.github.aivanovski.picoautomator.domain.entity Duration ElementReference]
           [com.github.aivanovski.picoautomator.presentation OutputWriter]))

(defn transform-to-element
  [data]
  (cond
    (:id data) (ElementReference/id (:id data))
    (:text data) (ElementReference/text (:text data))
    (:contains-text data) (ElementReference/containsText (:contains-text data))
    :else (throw (IllegalArgumentException. "Invalid reference to element"))))

(defn transform-to-duration
  [data]
  (cond
    (:seconds data) (Duration/seconds (:seconds data))
    (:milliseconds data) (Duration/millis (:milliseconds data))
    (:millis data) (Duration/millis (:millis data))))

(defn create-printer
  []
  (reify OutputWriter
    (print [this text] (do
                         (core/print text)
                         (core/flush)))
    (println [this line] (core/println line))
    (printStackTrace [this exception] (print-stack-trace exception))))

