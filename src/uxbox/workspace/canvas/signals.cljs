(ns uxbox.workspace.canvas.signals
  (:require
   [goog.dom :as dom]
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]
   [uxbox.geometry :refer [client-coords->canvas-coords]]))

(def canvas-coordinates-signal
  (s/map client-coords->canvas-coords mouse/client-position))

(defonce canvas-coordinates
  (s/pipe-to-atom canvas-coordinates-signal))

(def mouse-down-signal
  (s/bus))

(def mouse-up-signal
  (s/bus))

;; TODO
(def mouse-drag-signal
  (s/bus))

(defn on-mouse-down
  [e]
  (s/push! mouse-down-signal
           (client-coords->canvas-coords [(.-clientX e) (.-clientY e)])))

(defn on-mouse-up
  [e]
  (s/push! mouse-up-signal
           (client-coords->canvas-coords [(.-clientX e) (.-clientY e)])))

(defn on-drag-start
  [e]
  (s/push! mouse-drag-signal true))

(defn on-drag-end
  [e]
  (s/push! mouse-drag-signal false))
