(ns uxbox.workspace.signals
  (:require
   [jamesmacaulay.zelkova.signal :as z]
   [cljs.core.async :as async]
   [uxbox.mouse :as mouse]))

(def workspace-scroll-signal
  (z/write-port {:top 0
                 :left 0}))

(def workspace-top-scroll-signal
  (z/drop-repeats (z/map :top workspace-scroll-signal)))

(def workspace-left-scroll-signal
  (z/drop-repeats (z/map :left workspace-scroll-signal)))

(defonce scroll-top (z/pipe-to-atom workspace-top-scroll-signal))
(defonce scroll-left (z/pipe-to-atom workspace-left-scroll-signal))

(defn on-workspace-scroll
  [e]
  (let [t (.-target e)]
    (async/put! workspace-scroll-signal
                {:top (.-scrollTop t)
                 :left (.-scrollLeft t)})
    e))
