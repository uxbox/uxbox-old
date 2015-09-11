(ns uxbox.workspace.canvas.views
  (:require
   rum
   [uxbox.streams :as s]
   [uxbox.mouse :as mouse]
   [uxbox.data.log :refer [record]]
   [uxbox.workspace.tools :as tools]
   [uxbox.workspace.signals :as wsigs]
   [cljs.core.async :as async]
   [uxbox.workspace.canvas.actions :as actions]
   [uxbox.workspace.canvas.signals :as signals]
   [uxbox.geometry :as geo]
   [cuerdas.core :as str]
   [uxbox.shapes.protocols :as shapes]
   [uxbox.shapes.line :refer [new-line]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(rum/defc grid < rum/static
  [width height start-width start-height zoom]
  (let [padding (* 20 zoom)
        ticks-mod (/ 100 zoom)
        step-size (/ 10 zoom)

        vertical-ticks (range (- padding start-height) (- height start-height padding) step-size)
        horizontal-ticks (range (- padding start-width) (- width start-width padding) step-size)

        vertical-lines (fn
          [position value padding]
          (if (< (mod value ticks-mod) step-size)
             [:line {:key position
                     :y1 padding
                     :y2 width
                     :x1 position
                     :x2 position
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.75}]
             [:line {:key position
                     :y1 padding
                     :y2 width
                     :x1 position
                     :x2 position
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.25}]))

        horizontal-lines (fn
          [position value padding]
          (if (< (mod value ticks-mod) step-size)
             [:line {:key position
                     :y1 position
                     :y2 position
                     :x1 padding
                     :x2 height
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.75}]
             [:line {:key position
                     :y1 position
                     :y2 position
                     :x1 padding
                     :x2 height
                     :stroke "blue"
                     :stroke-width (/ 1 zoom)
                     :opacity 0.25}]))]
    [:g.grid
     (map #(vertical-lines (+ %1 start-width) %1 padding) vertical-ticks)
     (map #(horizontal-lines (+ %1 start-height) %1 padding) horizontal-ticks)]))

(rum/defc debug-coordinates < rum/reactive
  []
  (let [[x y] (rum/react signals/canvas-coordinates)]
    [:div
     {:style #js {:position "absolute"
                  :left "80px"
                  :top "20px"}}
     [:table
      [:tr
       [:td "X:"]
       [:td x]]
      [:tr
       [:td "Y:"]
       [:td y]]]]))

;; mouse down -> selected-tool? - yes -> start drawing! (implies deselect)

;;(mlet [tool wsigs/selected-tool-signal
;;       coords signals/mouse-down]
;;  [tool coords])

(def start-drawing-signal
  (s/flat-map-latest wsigs/selected-tool-signal
                     (fn [tool]
                       (s/combine vector
                                  (if (= tool :none)
                                    (s/never)
                                    (s/constant tool))
                                  signals/mouse-down-signal))))

;; todo: stop on mouse up
(def drawing-signal
  (s/map
   (fn [[tool coords]]
     (tools/start-drawing tool coords))
   start-drawing-signal))

(def zd (s/flat-map drawing-signal
                    (fn [shape]
                      (s/map (fn [[dx dy]] (shapes/move-delta shape dx dy))
                             mouse/delta))))

(def drawing (s/pipe-to-atom
              (s/map second
               (s/filter first
                         (s/combine-with vector
                                         signals/mouse-drag-signal
                                         zd)))))

(def drawn-signal
  (s/dedupe (s/sampled-by drawing-signal
                          signals/mouse-up-signal)))


;; mouse down -> selcted-tool? - no -> intersection? - yes -> select
;;                                                    - no -> deselect
;; toggle-selection-signal

;; mouse drag -> drawing? - yes -> update drawing!
;;
;;                        - no -> selected stuff? - yes -> move selections
;;                                                - no ->

;; mouse up -> drawing? - yes -> end drawing (implies selection of just drawn?)

;; shape-select :: mouse click -> (intersects with shape) -> (shape is not selected) -> selection shape
;; shape-deselect :: mouse click -> (doesn't intersect with shape) -> (shapes are selected) -> deselect all shapes

(rum/defc background < rum/static
  []
  [:rect
   {:x 0
    :y 0
    :width "100%"
    :height "100%"
    :fill "white"}])

(defn draw-shape
  [page shape]
  (record :uxbox/create-shape {:shape/data shape
                               :shape/uuid (random-uuid)
                               :shape/page (:page/uuid page)
                               :shape/locked? false
                               :shape/visible? true}))

(def canvas-signals
  {:will-mount (fn [state]
                 (let [args (:rum/args state)
                       page (second args)
                       unsub (s/on-value drawn-signal #(draw-shape page %))]
                   (assoc state ::unsub unsub)))
   :transfer-state (fn [old-state state]
                     (let [oldargs (:rum/args state)
                           oldpage (second oldargs)
                           args (:rum/args state)
                           page (second args)]
                      (if-not (= (:page/uuid oldpage) (:page/uuid page))
                        (let [unsub (::unsub old-state)
                              nunsub (s/on-value drawn-signal #(draw-shape page %))]
                          (unsub)
                          (assoc state ::unsub nunsub))
                        state)))
   :will-unmount (fn [state]
                   (let [unsub (::unsub state)]
                     (unsub)
                     (dissoc state ::unsub)))})

(rum/defc canvas < rum/reactive canvas-signals
  [conn
   page
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:page/width page)
        page-height (:page/height page)
        raw-shapes (map :shape/data shapes)]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height
      :on-mouse-down signals/on-mouse-down
      :on-mouse-up signals/on-mouse-up}
     (background)
     (apply vector :svg#page-layout (map shapes/shape->svg raw-shapes))
     (when-let [shape (rum/react drawing)]
       (shapes/shape->drawing-svg shape))
     #_(when-let [selected-shapes (get page :selected)]
       (map shapes/selected-svg selected-shapes)
       (shapes/shape->selected-svg (get shapes selected-uuid)))]))
