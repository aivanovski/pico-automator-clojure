(ns picoautomator.core

  (:require [picoautomator.utils :refer :all])

  (:import [kotlin.jvm.functions Function1]
           [com.github.aivanovski.picoautomator FlowFactory PicoAutomatorApi]
           [com.github.aivanovski.picoautomator.extensions UiNodeExtensionsKt]
           [com.github.aivanovski.picoautomator.domain.entity UiTreeNode]
           [com.github.aivanovski.picoautomator.presentation StandardOutputFlowReporter]
           [com.github.aivanovski.picoautomator.domain.runner FlowRunner]))

(defn has-element
  [^UiTreeNode tree element]
  (UiNodeExtensionsKt/hasElement tree (to-element element)))

(defprotocol PicoAutomator
  "A wrapper for PicoAutomatorApi that makes it easier to use from Clojure"
  (launch ^PicoAutomator [this package-name])
  (assert-visible ^PicoAutomator [this element])
  (tap-on ^PicoAutomator [this element])
  (input-text ^PicoAutomator
    [this text]
    [this text element])
  (visible? [this element])
  (wait-until ^PicoAutomator [this element timeout step])
  (ui-tree ^UiTreeNode [this])
  (sleep ^PicoAutomator [this duration])
  (fail [this message])
  (complete [this message]))

(deftype PicoAutomatorImpl [^PicoAutomatorApi api]
  PicoAutomator
  (launch [this package-name] (.launch api package-name) this)
  (assert-visible [this element] (.assertVisible api (to-element element)) this)
  (tap-on [this element] (.tapOn api (to-element element)) this)
  (input-text [this text] (.inputText api text) this)
  (input-text [this text element] (.inputText api (to-element element) text) this)
  (visible? [this element] (.isVisible api (to-element element)))
  (wait-until [this element timeout step]
    (.waitUntil api (to-element element) (to-duration timeout) (to-duration step)) this)
  (ui-tree [this] (.getUiTree api))
  (sleep [this duration] (.sleep api (to-duration duration)) this)
  (fail [this message] (.fail api message))
  (complete [this message] (.complete api message)))

(defn wrap-with-clj-api
  [f]
  (reify Function1
    (invoke
      [this api]
      (f (PicoAutomatorImpl. api)))))

(defn start-flow
  [flow-name f]
  (let [reporter (StandardOutputFlowReporter. (create-printer))
        runner (FlowRunner. 3)
        flow (FlowFactory/newFlow flow-name (wrap-with-clj-api f))]
    (.addLifecycleListener runner reporter)
    (.run runner flow)))
