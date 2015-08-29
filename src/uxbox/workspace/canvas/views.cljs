(ns uxbox.workspace.canvas.views
  (:require
   rum
   [jamesmacaulay.zelkova.signal :as z]
   [uxbox.pubsub :as pubsub]
   [uxbox.workspace.canvas.actions :as actions]
   [uxbox.workspace.canvas.signals :refer [canvas-coordinates]]
   [uxbox.geometry :as geo]
   [cuerdas.core :as str]
   [uxbox.shapes.core :as shapes]))

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
  (let [[x y] (rum/react canvas-coordinates)]
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

(defn on-canvas-click
  [e]
  (.preventDefault e)
  (pubsub/publish! [:canvas-mouse-click (geo/client-coords->canvas-coords [(.-clientX e)
                                                                           (.-clientY e)])]))

(defn on-canvas-mouse-move
  [e]
  (.preventDefault e)
  (pubsub/publish! [:canvas-mouse-move (geo/client-coords->canvas-coords [(.-clientX e)
                                                                          (.-clientY e)])]))
(defn on-canvas-mouse-up
  [e]
  (.preventDefault e)
  (pubsub/publish! [:canvas-mouse-up (geo/client-coords->canvas-coords [(.-clientX e)
                                                                        (.-clientY e)])]))
(defn on-canvas-mouse-down
  [e]
  (.preventDefault e)
  (pubsub/publish! [:canvas-mouse-down (geo/client-coords->canvas-coords [(.-clientX e)
                                                                          (.-clientY e)])]))

(defn on-canvas-wheel
  [e]
  (when (.-altKey e)
      (do (if (> (.-deltaY e) 0)
            (pubsub/publish! [:canvas-mouse-wheel 5])
            (pubsub/publish! [:canvas-mouse-wheel -5]))
          (.preventDefault e))))

(rum/defc canvas < rum/static
  [page
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:width page)
        page-height (:height page)
        shapes-to-draw (map #(get shapes %) (reverse (:root page)))]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height
      :on-click on-canvas-click
      :on-mouse-move on-canvas-mouse-move
      :on-mouse-down on-canvas-mouse-down
      :on-mouse-up on-canvas-mouse-up
      :on-wheel on-canvas-wheel}
     [:rect {:x 0 :y 0 :width "100%" :height "100%" :fill "white"}]
     (apply vector :svg#page-layout (map shapes/shape->svg shapes-to-draw))
     (when-let [shape (get page :drawing)]
       (shapes/shape->drawing-svg shape))
     (when-let [selected-uuid (get page :selected)]
       (shapes/shape->selected-svg (get shapes selected-uuid)))]))
