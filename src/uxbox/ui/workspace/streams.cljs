(ns uxbox.ui.workspace.streams
  (:require
   [uxbox.streams :as s]
   [uxbox.ui.streams.mouse :as mouse]))

(def workspace-scroll-bus
  (s/bus))

(def workspace-top-scroll-stream
  (s/dedupe (s/map :top workspace-scroll-bus)))

(def workspace-left-scroll-stream
  (s/dedupe (s/map :left workspace-scroll-bus)))

(defn- scroll-event
  [e]
  (let [t (.-target e)]
    {:top (.-scrollTop t)
     :left (.-scrollLeft t)}))

(defn on-workspace-scroll
  [e]
  (s/push! workspace-scroll-bus
           (scroll-event e)))

(defonce scroll-top (s/pipe-to-atom workspace-top-scroll-stream))
(defonce scroll-left (s/pipe-to-atom workspace-left-scroll-stream))

(def selected-tool-bus
  (s/bus))

(def selected-tool-stream
  (s/to-event-stream selected-tool-bus))

(defonce selected-tool (s/pipe-to-atom selected-tool-stream))

(def tool-selected?
  (s/to-property (s/map #(not= % :none)
                        selected-tool-stream)))

(defn toggle-tool!
  [tool]
  (if (= @selected-tool tool)
    (s/push! selected-tool-bus :none)
    (s/push! selected-tool-bus tool)))

(defn deselect-tool!
  []
  (s/push! selected-tool-bus :none))
