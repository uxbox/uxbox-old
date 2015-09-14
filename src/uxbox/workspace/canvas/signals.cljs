(ns uxbox.workspace.canvas.signals
  (:require
   [goog.dom :as dom]
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]
   [uxbox.workspace.signals :as wsigs]
   [uxbox.workspace.tools :as tools]
   [uxbox.shapes.protocols :as shapes]
   [uxbox.geometry :refer [client-coords->canvas-coords clamp]]))

(defn clamp-coords
  [obs]
  (s/dedupe (s/map clamp obs)))

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

(def mouse-drag-signal
  (s/flat-map-latest (s/true? mouse-down?)
                     (fn [_]
                       (s/take-until
                        (clamp-coords canvas-coordinates-signal)
                        mouse-up-signal))))

(defn on-mouse-down
  [e]
  (let [coords (client-coords->canvas-coords [(.-clientX e) (.-clientY e)])]
    (s/push! mouse-down-signal coords)))

(defn on-mouse-up
  [e]
  (s/push! mouse-up-signal
           (client-coords->canvas-coords [(.-clientX e) (.-clientY e)])))

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
                    (s/true? mouse-down?)
                    (clamp-coords canvas-coordinates)))

(def drawing-signal (s/flat-map start-drawing-signal
                                (fn [shape]
                                  (let [drag (s/take-while stroke-signal mouse-down?)]
                                    (s/scan (fn [s [x y]]
                                              (shapes/draw s x y))
                                            shape
                                            drag)))))

(def draw-signal (s/dedupe (s/sampled-by drawing-signal
                                         (s/true? mouse-up?))))

(def draw-in-progress (s/merge drawing-signal
                               (s/map (constantly nil)
                                      mouse-up-signal)))

(def shapes-bus
  (s/bus))

(def shapes-signal
  (s/dedupe shapes-bus))

(defn set-current-shapes!
  [shapes]
  (s/push! shapes-bus shapes))

(def start-selection
  (s/and mouse-down?
         (s/not wsigs/tool-selected?)))

(def intersections (s/sampled-by
                    (s/combine
                       (fn [[x y] shapes]
                         (into {}
                               (comp
                                 (map (juxt :shape/uuid :shape/data))
                                 (filter #(shapes/intersect (second %) x y)))
                               shapes))
                       (clamp-coords canvas-coordinates-signal)
                       shapes-signal)
                    (s/true? start-selection)))

(def selections
  (s/scan
   (fn [selected [shapes intersections]]
     (if (empty? intersections)
         {} ;; deselect everything
         (let [selected-ids (set (keys selected))
               selected-shapes (->> shapes
                                    (filter #(contains? selected-ids (:shape/uuid %)))
                                    (map (fn [s]
                                           [(:shape/uuid s) (:shape/data s)])))]
           (merge intersections selected selected-shapes))))
   {}
   (s/combine vector
              shapes-signal
              intersections)))

(defn move-selections
  [sels [dx dy]]
  (into {} (map (fn [[k s]]
                  [k (shapes/move-delta s dx dy)])
                sels)))

(def selected (s/flat-map
               selections
               (fn [sels]
                 (let [drag (s/take-while mouse/delta mouse-down?)]
                   (s/scan move-selections sels drag)))))

(def move-signal (s/sampled-by
                  selected
                  (s/true? mouse-up?)))
