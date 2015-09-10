(ns uxbox.workspace.canvas.signals
  (:require
   [jamesmacaulay.zelkova.signal :as z]
   [jamesmacaulay.zelkova.mouse :as zm]
   [cljs.core.async :as async]
   [uxbox.geometry :refer [client-coords->canvas-coords]]
   [uxbox.mouse :as mouse]))

(def canvas-coordinates-signal
  (z/map client-coords->canvas-coords mouse/client-position))

(defonce canvas-coordinates
  (z/pipe-to-atom canvas-coordinates-signal))

(def mouse-down-signal
  (z/write-port false))

(def mouse-drag-signal
  (z/write-port false))

(defn on-mouse-down
  [e]
  (async/put! mouse-down-signal true))

(defn on-mouse-up
  [e]
  (async/put! mouse-down-signal false))

(defn on-mouse-drag
  [e]
  (async/put! mouse-drag-signal false))

(def mouse-down
  (z/sample-on (z/keep-if identity mouse-down-signal)
                canvas-coordinates-signal))

(def mouse-up
  (z/sample-on (z/drop-if identity mouse-down-signal)
                canvas-coordinates-signal))

(def mouse-drag
  (z/keep-when mouse-down-signal mouse/delta))
