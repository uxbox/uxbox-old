(ns uxbox.workspace.canvas.views
  (:require
   rum
   [jamesmacaulay.zelkova.signal :as z]
   [uxbox.pubsub :as pubsub]
   [uxbox.workspace.canvas.actions :as actions]
   [uxbox.workspace.canvas.signals :as signals]
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


(defonce canvas-coordinates (z/pipe-to-atom signals/canvas-coordinates))

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

(rum/defc canvas
  [db]
  (let [viewport-height 3000
        viewport-width 3000
        page (:page @db)
        page-groups (:groups @db)
        page-shapes (:shapes @db)

        page-width (:width page)
        page-height (:height page)

        document-start-x 50
        document-start-y 50

        zoom (get-in @db [:workspace :zoom])

        ;; Get a group of ids and retrieves the list of shapes
        ids->shapes (fn [shape-ids]
                      (->> shape-ids
                           (map #(get page-shapes %))
                           (filter #(not (nil? %)))
                           ))

        ;; Retrieve the <g> element grouped if applied
        group-svg (fn [shapes]
                    (if (= (count shapes) 1)
                      (->> shapes first shapes/shape->svg)
                      (apply vector :g
                             (->> shapes
                                  (map shapes/shape->svg)))))

        ;; Retrieve the list of shapes grouped if applies
        shapes-svg (->> page-groups
                        (vals)
                        (sort-by :order)
                        (filter :visible)
                        (map #(update-in % [:shapes] ids->shapes))
                        (map :shapes)
                        (map group-svg))

        on-event (fn [event-type]
                   (fn [e]
                     (let [coords (geo/client-coords->canvas-coords (.-clientX e) (.-clientY e))]
                       (pubsub/publish! [event-type coords])
                       (.preventDefault e))))

        on-wheel-event (fn [event-type]
                   (fn [e]
                     (when (.-altKey e)
                       (do (if (> (.-deltaY e) 0)
                             (pubsub/publish! [event-type 5])
                             (pubsub/publish! [event-type -5]))
                           (.preventDefault e)))))]

    [:div {:on-mouse-move (on-event :viewport-mouse-move)
           :on-click (on-event :viewport-mouse-click)
           :on-mouse-down (on-event :viewport-mouse-down)
           :on-mouse-up (on-event :viewport-mouse-up)
           :on-wheel (on-wheel-event :zoom-wheel)}
     (debug-coordinates)
     [:svg#viewport {:width viewport-height :height viewport-width}
      [:g.zoom {:transform (str "scale(" zoom " " zoom ")")}
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
           (shapes/shape->selected-svg (get page-shapes selected-uuid)))]
        (if (:grid? (:workspace @db))
          (grid viewport-width viewport-height document-start-x document-start-y zoom))]]]))
