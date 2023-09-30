(ns picoautomator.api
  (:gen-class)
  (:require [clojure.core :as core]
            [clojure.stacktrace :refer [print-stack-trace]])
  (:import [kotlin.jvm.functions Function1]
           [com.github.aivanovski.picoautomator PicoAutomatorDsl PicoAutomatorApi]
           [com.github.aivanovski.picoautomator.extensions UiNodeExtensionsKt]
           [com.github.aivanovski.picoautomator.domain.entity Duration UiTreeNode ElementReference]
           [com.github.aivanovski.picoautomator.presentation StandardOutputFlowReporter OutputWriter]
           [com.github.aivanovski.picoautomator.domain.runner FlowRunner]))

(defn map-to-element
  [data]
  (cond
    (:id data) (ElementReference/id (:id data))
    (:text data) (ElementReference/text (:text data))
    (:contains-text data) (ElementReference/containsText (:contains-text data))
    :else (throw (IllegalArgumentException. "Invalid reference to element"))))

(defn seconds
  [sec]
  (Duration/seconds sec))

(defn millis
  [milliseconds]
  (Duration/millis milliseconds))

(defn has-element
  [tree element]
  (UiNodeExtensionsKt/hasElement tree (map-to-element element)))

(defprotocol ApiWrapper
  "A wrapper for PicoAutomatorApi that makes it easier to use from Clojure"
  (launch ^ApiWrapper [this package-name])
  (assert-visible ^ApiWrapper [this element])
  (tap-on ^ApiWrapper [this element])
  (input-text ^ApiWrapper
    [this text]
    [this text element])
  (visible? [this element])
  (wait-for ^ApiWrapper [this element timeout step])
  (ui-tree ^UiTreeNode [this])
  (sleep ^ApiWrapper [this duration])
  (fail [this message])
  (complete [this message]))

(deftype ApiWrapperImpl [api]
  ApiWrapper
  (launch [this package-name] (.launch api package-name) this)
  (assert-visible [this element] (.assertVisible api (map-to-element element)) this)
  (tap-on [this element] (.tapOn api (map-to-element element)) this)
  (input-text [this text] (.inputText api text) this)
  (input-text [this text element] (.inputText api (map-to-element element) text) this)
  (visible? [this element] (.isVisible api (map-to-element element)))
  (wait-for [this element timeout step] (.waitFor api (map-to-element element) timeout step) this)
  (ui-tree [this] (.getUiTree api))
  (sleep [this duration] (.delay api duration) this)
  (fail [this message] (.fail api message))
  (complete [this message] (.complete api message)))

(defn wrap-with-clj-api
  [f]
  (reify Function1
    (invoke
      [this api]
      (f (ApiWrapperImpl. api)))))

(defn create-printer
  []
  (reify OutputWriter
    (print [this text] (do
                         (core/print text)
                         (core/flush)))
    (println [this line] (core/println line))
    (printStackTrace [this exception] (print-stack-trace exception))))

(defn start-flow
  [flow-name f]
  (let [printer (create-printer)
        reporter (StandardOutputFlowReporter. printer)
        runner (FlowRunner. 3)
        flow (.newFlow PicoAutomatorDsl/INSTANCE flow-name (wrap-with-clj-api f))]
    (.addLifecycleListener runner reporter)
    (.run runner flow)))

(defn -main
  [& args]

  (start-flow
    "Wikipedia Search Flow"
    (fn [^PicoAutomatorApi automator]
      (-> automator
          (launch "org.wikipedia")
          (wait-for {:text "Search"} (seconds 10) (millis 500))
          (tap-on {:text "Search"})
          (tap-on {:text "Search Wikipedia"})
          (input-text "Dunning" {:text "Search Wikipedia"})
          (tap-on {:text "Dunning–Kruger effect"})
          (assert-visible {:id "page_web_view"})))
    )
  )