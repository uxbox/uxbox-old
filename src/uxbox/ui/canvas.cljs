(ns uxbox.ui.canvas
  (:require
   rum
   [uxbox.streams :refer [on-event]]
   [beicon.core :as b]
   [uxbox.ui.mixins :as mx]
   [uxbox.shapes.actions :as actions]
   [uxbox.ui.canvas.streams :as cs]
   [uxbox.shapes.protocols :as shapes]))

(rum/defc debug-coordinates < rum/reactive
  []
  (let [[x y] (rum/react cs/canvas-coordinates)]
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

(rum/defc background < rum/static
  []
  [:rect
   {:x 0
    :y 0
    :width "100%"
    :height "100%"
    :fill "white"}])


(rum/defc canvas < rum/reactive
                   cs/mouse-move-mixin
                   (mx/cmds-mixin
                    [::draw cs/draw! (fn [[conn page] shape]
                                    (actions/draw-shape conn page shape))]

                    [::move cs/move! (fn [[conn] selections]
                                    (actions/update-shapes conn selections))])
  [conn
   page
   shapes
   {:keys [viewport-height
           viewport-width
           document-start-x
           document-start-y]}]
  (let [page-width (:page/width page)
        page-height (:page/height page)
        selection-uuids (rum/react cs/selected-ids)
        selected-shapes (rum/react cs/selected-shapes-data)
        visible-shapes (into []
                         (filter :shape/visible?)
                         shapes)]
    [:svg#page-canvas
     {:x document-start-x
      :y document-start-y
      :width page-width
      :height page-height
      :on-mouse-down (on-event :canvas shapes)
      :on-mouse-up (on-event :canvas shapes)
      :on-mouse-move (on-event :canvas shapes)}
     (background)
     (apply vector :svg#page-layout (map (fn [shape]
                                           (if (some #(= (:shape/uuid shape) %) selection-uuids)
                                             [:g
                                               (shapes/shape->svg (:shape/data shape))
                                               (shapes/shape->selected-svg (:shape/data shape))]
                                             (shapes/shape->svg (:shape/data shape))))
                                         visible-shapes))
     (when-let [shape (rum/react cs/drawing)]
       (shapes/shape->drawing-svg shape))
     (when-let [shape (rum/react cs/selecting)]
       (shapes/shape->drawing-svg shape))]))
