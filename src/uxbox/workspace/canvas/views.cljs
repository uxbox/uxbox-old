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

(rum/defc canvas < rum/static
  [page
   groups
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:width page)
        page-height (:height page)
        ;; Get a group of ids and retrieves the list of shapes
        id->shape-xform (comp
                         (map #(get shapes %))
                         (filter #(not (nil? %))))
        ids->shapes #(sequence id->shape-xform %)

        ;; Retrieve the <g> element grouped if applied
        group-svg (fn [shapes]
                    (if (= (count shapes) 1)
                      (->> shapes first shapes/shape->svg)
                      (apply vector
                             :g
                             (->> shapes
                                  (map shapes/shape->svg)))))

        ;; Retrieve the list of shapes grouped if applies
        shape-svg-xform (comp
                         (filter :visible)
                         (map #(update-in % [:shapes] ids->shapes))
                         (map :shapes)
                         (map group-svg))

        shapes-svg (->> groups
                        (vals)
                        (sort-by :order)
                        (sequence shape-svg-xform))]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height}
     [:rect {:x 0 :y 0 :width "100%" :height "100%" :fill "white"}]
     (apply vector :svg#page-layout shapes-svg)
     (when-let [shape (get page :drawing)]
       (shapes/shape->drawing-svg shape))
     (when-let [selected-uuid (get page :selected)]
       (shapes/shape->selected-svg (get shapes selected-uuid)))]))
