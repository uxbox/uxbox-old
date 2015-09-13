(ns uxbox.workspace.signals
  (:require
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]))

(def workspace-scroll-signal
  (s/bus))

(def workspace-top-scroll-signal
  (s/dedupe (s/map :top workspace-scroll-signal)))

(def workspace-left-scroll-signal
  (s/dedupe (s/map :left workspace-scroll-signal)))

(defn- scroll-event
  [e]
  (let [t (.-target e)]
    {:top (.-scrollTop t)
     :left (.-scrollLeft t)}))

(defn on-workspace-scroll
  [e]
  (s/push! workspace-scroll-signal
           (scroll-event e)))

(defonce scroll-top (s/pipe-to-atom workspace-top-scroll-signal))
(defonce scroll-left (s/pipe-to-atom workspace-left-scroll-signal))

(def selected-tool-signal
  (s/bus))

(defonce selected-tool (s/pipe-to-atom selected-tool-signal))

(defn select-tool!
  [tool]
  (if (= @selected-tool tool)
    (s/push! selected-tool-signal :none)
    (s/push! selected-tool-signal tool)))
