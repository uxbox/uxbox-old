(ns uxbox.workspace.canvas.signals
  (:require
   [jamesmacaulay.zelkova.signal :as z]
   [uxbox.geometry :refer [client-coords->canvas-coords]]
   [uxbox.mouse :as mouse]))

(def canvas-coordinates-signal
  (z/map client-coords->canvas-coords mouse/client-position))

(defonce canvas-coordinates
  (z/pipe-to-atom canvas-coordinates-signal))
