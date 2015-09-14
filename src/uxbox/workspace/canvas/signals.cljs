(ns uxbox.workspace.canvas.signals
  (:require
   [goog.dom :as dom]
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]
   [uxbox.workspace.signals :as wsigs]
   [uxbox.workspace.tools :as tools]
   [uxbox.shapes.protocols :as shapes]
   [uxbox.geometry :refer [client-coords->canvas-coords]]))

(def canvas-coordinates-signal
  (s/map client-coords->canvas-coords mouse/client-position))

(defonce canvas-coordinates
  (s/to-property canvas-coordinates-signal))

(def mouse-down-signal
  (s/bus))

(def mouse-up-signal
  (s/bus))

(def mouse-down?
  (s/to-property (s/merge (s/map (constantly true) mouse-down-signal)
                          (s/map (constantly false) mouse-up-signal))))

(def mouse-up? (s/not mouse-down?))

;; TODO
(def mouse-drag-signal
  (s/bus))

(def dragging?
  (s/to-property mouse-drag-signal))

(defn on-mouse-down
  [e]
  (s/push! mouse-down-signal
           (client-coords->canvas-coords [(.-clientX e) (.-clientY e)]))
  (s/push! mouse-drag-signal true))

(defn on-mouse-up
  [e]
  (s/push! mouse-up-signal
           (client-coords->canvas-coords [(.-clientX e) (.-clientY e)]))
  (s/push! mouse-drag-signal false))

(defn clamp-coords
  [obs]
  (s/dedupe (s/map (fn [[x y]]
                     (let [cx (max 0 x)
                           cy (max 0 y)]
                       [cx cy]))
                   obs)))

(def start-drawing? (s/and wsigs/tool-selected?
                           mouse-down?))

(def start-drawing-signal
  (s/sampled-by
   (s/combine
    (fn [tool coords]
      (tools/start-drawing tool coords))
    wsigs/selected-tool-signal
    canvas-coordinates-signal)
   (s/true? start-drawing?)))

(def stroke-signal (s/flat-map-latest
                    (s/true? dragging?)
                    (clamp-coords canvas-coordinates)))

(def drawing-signal (s/flat-map start-drawing-signal
                                (fn [shape]
                                  (let [drag (s/take-while stroke-signal dragging?)]
                                    (s/scan (fn [s [x y]]
                                              (shapes/draw s x y))
                                            shape
                                            drag)))))

(def draw-signal (s/sampled-by drawing-signal
                               (clamp-coords mouse-up-signal)
                               (fn [s [x y]]
                                 (shapes/draw s x y))))

(def draw-in-progress (s/merge drawing-signal
                               (s/map (constantly nil)
                                      mouse-up-signal)))
