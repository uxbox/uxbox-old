(ns uxbox.ui.workspace.streams
  (:require
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]))

(def workspace-scroll-bus
  (s/bus))

(def workspace-top-scroll-signal
  (s/dedupe (s/map :top workspace-scroll-bus)))

(def workspace-left-scroll-signal
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

(defonce scroll-top (s/pipe-to-atom workspace-top-scroll-signal))
(defonce scroll-left (s/pipe-to-atom workspace-left-scroll-signal))

(def selected-tool-bus
  (s/bus))

(def selected-tool-signal
  (s/to-event-stream selected-tool-bus))

(defonce selected-tool (s/pipe-to-atom selected-tool-signal))

(def tool-selected?
  (s/to-property (s/map #(not= % :none)
                        selected-tool-signal)))

(defn toggle-tool!
  [tool]
  (if (= @selected-tool tool)
    (s/push! selected-tool-bus :none)
    (s/push! selected-tool-bus tool)))

(defn deselect-tool!
  []
  (s/push! selected-tool-bus :none))
