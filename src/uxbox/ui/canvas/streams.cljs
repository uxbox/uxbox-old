(ns uxbox.ui.canvas.streams
  (:require
   [uxbox.streams :refer [main-bus on-event]]
   [beicon.core :as b]
   [uxbox.ui.workspace.streams :as ws]
   [uxbox.ui.tools :as tools]
   [uxbox.shapes.protocols :as shapes]
   [uxbox.shapes.queries :as sq]
   [uxbox.geometry :as geo]
   [goog.events :as events])
  (:import [goog.events EventType]))

;; Transformers

(defn- client-coords->canvas-coords
  [[client-x client-y]]
  (if-let [canvas-element (.getElementById js/document "page-canvas")]
    (let [bounding-rect (.getBoundingClientRect canvas-element)
          offset-x (.-left bounding-rect)
          offset-y (.-top bounding-rect)
          new-x (- client-x offset-x)
          new-y (- client-y offset-y)]
      [new-x new-y])
    [client-x client-y]))

;; Streams

;;;; Main workspace stream
(def canvas-stream
  (b/filter #(= (.-ns %) :canvas) main-bus))

;;;; Mouse privitives streams

(def mouse-up-stream
  (b/map #(client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
         (b/filter #(= (.-type %) "mouseup") canvas-stream)))

(def mouse-down-stream
  (b/map #(client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
         (b/filter #(= (.-type %) "mousedown") canvas-stream)))

(def mouse-move-stream
  (b/map #(client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
         (b/filter #(= (.-type %) "mousemove") canvas-stream)))

(def mouse-click-stream
  (b/map #(assoc {} :coords (client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
                    :shapes (.-data %)
                    :ctrl (.-ctrlKey %)
                    :alt (.-altKey %)
                    :shift (.-shiftKey %))
         (b/filter #(= (.-type %) "click") canvas-stream)))

(def mouse-ctrl-click-stream
  (b/filter #(and (:ctrl %) (not (:alt %)) (not (:shift %))) mouse-click-stream))

(def mouse-alt-click-stream
  (b/filter #(and (not (:ctrl %)) (:alt %) (not (:shift %))) mouse-click-stream))

(def mouse-shift-click-stream
  (b/filter #(and (not (:ctrl %)) (not (:alt %)) (:shift %)) mouse-click-stream))

(def mouse-normal-click-stream
  (b/filter #(and (not (:ctrl %)) (not (:alt %)) (not (:shift %))) mouse-click-stream))

;;;; Mouse privitive properties

(def mouse-pressed?-stream
  (b/merge (b/map (constantly true) mouse-down-stream)
           (b/map (constantly false) mouse-up-stream)))

(def client-position-stream mouse-move-stream)

(defn coords-delta
  [[old new]]
  (let [[oldx oldy] old
        [newx newy] new]
    [(* 2 (- newx oldx))
     (* 2 (- newy oldy))]))

(def ^{:doc "A stream of mouse coordinate deltas as `[dx dy]` vectors."}
  client-position-delta
  (b/map coords-delta (b/buffer 2 mouse-move-stream)))

(def drawing-stream
  (b/filter #(and @mouse-pressed? (not= @ws/selected-tool nil) (empty? @selected-shapes))
            mouse-move-stream))

(def selecting-stream
  (b/filter #(and @mouse-pressed? (= @ws/selected-tool nil) (empty? @selected-shapes))
            mouse-move-stream))

(def moving-shapes-stream
  (b/filter #(and @mouse-pressed? (= @ws/selected-tool nil) (not (empty? @selected-shapes)))
            client-position-delta))


;;;; Mouse streams
(def select-one-shape-stream
  (b/map #(take 1 %)
         (b/map #(filter (fn [shape] (shapes/intersect (:shape/data shape) (first (:coords %)) (second (:coords %)))) (reverse (:shapes %)))
                mouse-normal-click-stream)))

(def add-one-shape-to-selected-stream
  (b/map #(conj @selected-shapes %)
         (b/map first
                (b/map #(filter (fn [shape] (shapes/intersect (:shape/data shape) (first (:coords %)) (second (:coords %)))) (reverse (:shapes %)))
                       mouse-ctrl-click-stream))))

(def selected-shapes-stream
  (b/merge select-one-shape-stream
           add-one-shape-to-selected-stream))

(def selected-shapes-data-stream
  (b/map #(map :shape/data %) selected-shapes-stream))

(def selected-shape-ids-stream
  (b/map #(map :shape/uuid %) selected-shapes-stream))

(defn clamp-coords
  [obs]
  (b/dedupe (b/map geo/clamp obs)))

(def canvas-coordinates-stream
  (b/map client-coords->canvas-coords client-position-stream))

;; Mixins

(def ^{:doc "A mixin for capture mouse move events."}
  mouse-move-mixin
  {:will-mount (fn [state]
                 (events/listen js/document
                                EventType.MOUSEMOVE
                                (on-event :canvas))
                 state)
   :will-unmount (fn [state]
                 (events/unlisten js/document
                                  EventType.MOUSEMOVE
                                  (on-event :canvas))
                   state)})


;; Atoms

;; (def draw! (b/to-atom draw-stream))
;; (def move! (b/to-atom move-stream))
;; (def drawing (b/to-atom draw-in-progress))

(defonce draw! (atom nil))
(defonce move! (atom nil))
(defonce drawing (atom nil))
(defonce selected-shapes (b/to-atom selected-shapes-stream))
(defonce selected-shapes-data (b/to-atom selected-shapes-data-stream))
(defonce selected-ids (b/to-atom selected-shape-ids-stream))
(defonce mouse-pressed? (b/to-atom mouse-pressed?-stream))
(defonce canvas-coordinates (b/to-atom canvas-coordinates-stream))
(defonce client-position (b/to-atom client-position-stream))
