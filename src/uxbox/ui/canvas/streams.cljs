(ns uxbox.ui.canvas.streams
  (:require
   [uxbox.streams :refer [main-bus on-event]]
   [beicon.core :as b]
   [uxbox.ui.workspace.streams :as ws]
   [uxbox.ui.tools :as tools]
   [uxbox.shapes.protocols :as shapes]
   [uxbox.shapes.circle :refer [new-circle]]
   [uxbox.shapes.rectangle :refer [new-rectangle]]
   [uxbox.shapes.queries :as sq]
   [uxbox.shapes.actions :as sa]
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
  (b/map #(assoc {} :coords (client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
                    :shapes (.-data %)
                    :ctrl (.-ctrlKey %)
                    :alt (.-altKey %)
                    :shift (.-shiftKey %))
         (b/filter #(= (.-type %) "mouseup") canvas-stream)))

(def mouse-down-stream
  (b/map #(assoc {} :coords (client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
                    :shapes (.-data %)
                    :ctrl (.-ctrlKey %)
                    :alt (.-altKey %)
                    :shift (.-shiftKey %))
         (b/filter #(= (.-type %) "mousedown") canvas-stream)))

(def mouse-move-stream
  (b/map #(assoc {} :coords (client-coords->canvas-coords [(.-clientX %) (.-clientY %)])
                    :shapes (.-data %)
                    :ctrl (.-ctrlKey %)
                    :alt (.-altKey %)
                    :shift (.-shiftKey %))
         (b/filter #(= (.-type %) "mousemove") canvas-stream)))

(def mouse-click-stream
  (->> (b/zip mouse-down-stream mouse-up-stream)
       (b/filter #(= (:coords (first %)) (:coords (second %))))
       (b/map first)))

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

(def client-position-stream (b/map :coords mouse-move-stream))

(defn coords-delta
  [[old new]]
  (let [[oldx oldy] old
        [newx newy] new]
    [(* 2 (- newx oldx))
     (* 2 (- newy oldy))]))

(def ^{:doc "A stream of mouse coordinate deltas as `[dx dy]` vectors."}
  client-position-delta
  (b/map coords-delta (b/buffer 2 client-position-stream)))

(def drawing-stream
  (b/filter #(and @mouse-pressed? (not= @ws/selected-tool nil))
            mouse-move-stream))

(def drawing-shape-stream
  (b/map (fn [event]
           (let [x1 (first @last-mouse-down-position)
                 y1 (second @last-mouse-down-position)
                 x2 (first (:coords event))
                 y2 (second (:coords event))
                 width (- (max x1 x2) (min x1 x2))
                 height (- (max y1 y2) (min y1 y2))
                 r (geo/distance x1 y1 x2 y2)]

             (case @ws/selected-tool
               [:circle] (new-circle x1 y1 r)
               [:rect] (new-rectangle (min x1 x2) (min y1 y2) width height)
               nil)))
         drawing-stream))

(def drawing-shape-finish-stream
  (b/map first
    (b/zip drawing-shape-stream
           mouse-up-stream)))

(def selecting-stream
  (b/filter #(and @mouse-pressed? (= @ws/selected-tool nil) (empty? @selected-shapes))
            mouse-move-stream))

(def selecting-shape-stream
  (b/filter #(not (nil? %))
            (b/map (fn [event]
                     (let [x1 (first @last-mouse-down-position)
                           y1 (second @last-mouse-down-position)
                           x2 (first (:coords event))
                           y2 (second (:coords event))
                           width (- (max x1 x2) (min x1 x2))
                           height (- (max y1 y2) (min y1 y2))]
                        (new-rectangle (min x1 x2) (min y1 y2) width height)))
                   drawing-stream)))

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

(defonce draw! (atom nil))
(defonce move! (atom nil))
(defonce last-mouse-down-position (b/to-atom (b/map :coords mouse-down-stream)))
(defonce drawing (b/to-atom drawing-shape-stream))
(defonce selecting (b/to-atom selecting-shape-stream))
(defonce selected-shapes (b/to-atom selected-shapes-stream))
(defonce selected-shapes-data (b/to-atom selected-shapes-data-stream))
(defonce selected-ids (b/to-atom selected-shape-ids-stream))
(defonce mouse-pressed? (b/to-atom mouse-pressed?-stream))
(defonce canvas-coordinates (b/to-atom canvas-coordinates-stream))
(defonce client-position (b/to-atom client-position-stream))

;; Effects

(b/on-value drawing-shape-finish-stream
            #(sa/draw_shape conn page %))
