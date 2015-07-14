(ns uxbox.workspace.canvas.views
  (:require  [uxbox.pubsub :as pubsub]
             [uxbox.workspace.canvas.actions :as actions]
             [uxbox.geometry :as geo]
             [reagent.core :refer [atom]]
             [cuerdas.core :as str]))

(defn grid
  [width height start-width start-height zoom]
  (let [padding 20
        ticks-mod 100
        step-size 10

        vertical-ticks (range (- padding start-height) (- height start-height padding) step-size)
        horizontal-ticks (range (- padding start-width) (- width start-width padding) step-size)

        vertical-lines (fn
          [position value padding]
          (if (= (mod value ticks-mod) 0)
             [:line {:key position :y1 padding :y2 width :x1 position :x2 position :stroke "black" :opacity 0.75}]
             [:line {:key position :y1 padding :y2 width :x1 position :x2 position :stroke "black" :opacity 0.25}]))

        horizontal-lines (fn
          [position value padding]
          (if (= (mod value ticks-mod) 0)
             [:line {:key position :y1 position :y2 position :x1 padding :x2 height :stroke "black" :opacity 0.75}]
             [:line {:key position :y1 position :y2 position :x1 padding :x2 height :stroke "black" :opacity 0.25}]))
        ]
    [:g.grid
     (map #(vertical-lines (+ %1 start-width) %1 padding) vertical-ticks)
     (map #(horizontal-lines (+ %1 start-height) %1 padding) horizontal-ticks)]))

(defn vertical-rule
  [height start-height zoom]
  (let [padding 20
        big-ticks-mod 100
        mid-ticks-mod 50
        step-size 10
        ticks (range (- padding start-height) (- height start-height padding) step-size)

        lines (fn
                [position value padding]
                (cond
                  (= (mod value big-ticks-mod) 0)
                  [:g {:key position}
                   [:line {:y1 position :y2 position :x1 5 :x2 padding :stroke "black"}]
                   [:text {:y position :x 5 :transform (str/format "rotate(90 0 %s)" position)} value]]
                  (= (mod value mid-ticks-mod) 0)
                  [:line {:key position :y1 position :y2 position :x1 10 :x2 padding :stroke "black"}]
                  :else
                  [:line {:key position :y1 position :y2 position :x1 15 :x2 padding :stroke "black"}]))
        ]
    [:g.vertical-rule
     [:rect {:x 0 :y padding :height height :width padding :fill "gray"}]
     (map #(lines (+ %1 start-height) %1 padding) ticks)]))

(defn horizontal-rule
  [width start-width zoom]
  (let [padding 20
        big-ticks-mod 100
        mid-ticks-mod 50
        step-size 10
        ticks (range (- padding start-width) (- width start-width padding) step-size)
        lines (fn
                [position value padding]
                (cond
                  (= (mod value big-ticks-mod) 0)
                  [:g {:key position}
                   [:line {:x1 position :x2 position :y1 5 :y2 padding :stroke "black"}]
                   [:text {:x (+ position 2) :y 13} value]]
                  (= (mod value mid-ticks-mod) 0)
                  [:line {:key position :x1 position :x2 position :y1 10 :y2 padding :stroke "black"}]
                  :else
                  [:line {:key position :x1 position :x2 position :y1 15 :y2 padding :stroke "black"}]))
        ]
    [:g.horizontal-rule
     [:rect {:x padding :y 0 :width width :height padding :fill "gray"}]
     [:rect {:x 0 :y 0 :width padding :height padding :fill "gray"}]
     (map #(lines (+ %1 start-width) %1 padding) ticks)]))

;; Transform from the "shape" internal datastructure to SVG tags
(defmulti shape->svg :shape)

(defmethod shape->svg :rectangle [{:keys [x y width height rx ry fill stroke]}]
  [:rect {:x x :y y :width width :height height :fill fill :rx rx :ry ry :stroke stroke}])

(defmethod shape->svg :line [{:keys [x1 y1 x2 y2 color width]}]
  [:line {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :style #js {:stroke color :strokeWidth width}}])

;; Get the SVG elements for a "drawing" element
(defmulti shape->drawing-svg :shape)

(defmethod shape->drawing-svg :rectangle [{:keys [x y]}]
  (let [coordinates (atom [[x y]])
        viewport-move (fn [coord]
                        (reset! coordinates coord))]
    (pubsub/register-event :viewport-mouse-move viewport-move)
    (fn []
      (let [[mouseX mouseY] @coordinates
            [rect-x rect-y rect-width rect-height] (geo/coords->rect x y mouseX mouseY)]
        (if (and (> rect-width 0) (> rect-height 0))
          [:rect {:x rect-x :y rect-y :width rect-width :height rect-height
                  :style #js {:fill "transparent" :stroke "black" :strokeDasharray "5,5"}}])))))

(defn debug-coordinates [db]
  (let [coordinates (atom [])
        viewport-move (fn [coord]
                        (reset! coordinates coord))]
    (pubsub/register-event :viewport-mouse-move viewport-move)
    (fn []
      (let [[mouseX mouseY] @coordinates]
        [:div {:style #js {:position "absolute" :left "80px" :top "20px"}}
         [:table
          [:tr [:td "X:"] [:td mouseX]]
          [:tr [:td "Y:"] [:td mouseY]]]]))))

(defn canvas [db]
  (let [viewport-height 3000
        viewport-width 3000

        page-width (get-in @db [:page :width])
        page-height (get-in @db [:page :height])

        document-start-x (- 500 (/ page-width 2))
        document-start-y (- 750 (/ page-height 2))

        ;; Get a group of ids and retrieves the list of shapes
        ids->shapes (fn [shape-ids]
                      (map #(get-in @db [:page :shapes %]) shape-ids))

        ;; Retrieve the <g> element grouped if applied
        group-svg (fn [shapes]
                    (if (= (count shapes) 1)
                      (->> shapes first shape->svg)
                      (apply vector :g
                             (->> shapes
                                  (map shape->svg)))))

        ;; Retrieve the list of shapes grouped if applies
        shapes-svg (->> @db
                        :page :groups vals
                        (sort-by :order)
                        (filter :visible)
                        (map #(update-in % [:shapes] ids->shapes))
                        (map :shapes)
                        (map group-svg))
        on-move (fn [e]
                  (let [coords (geo/clientcoord->viewportcord (.-clientX e) (.-clientY e))]
                    (pubsub/publish! [:viewport-mouse-move coords])
                    (.preventDefault e)))

        on-click (fn [e]
                   (let [coord (geo/clientcoord->viewportcord (.-clientX e) (.-clientY e))]
                     (actions/drawing-rect coord)
                     (.preventDefault e)))]
    [:div {:on-mouse-move on-move :on-click on-click}
     [debug-coordinates db]
     [:svg#viewport {:width viewport-height :height viewport-width}
      [horizontal-rule viewport-width document-start-x 100]
      [vertical-rule viewport-height document-start-y 100]
      [:svg#page-canvas  {:x 50 :y 50 :width page-width :height page-height};; Document
       [:rect {:x 0 :y 0 :width "100%" :height "100%" :fill "white"}]
       (apply vector :svg#page-layout shapes-svg)
       (when-let [shape (get-in @db [:page :drawing])]
         [shape->drawing-svg shape])]
      (if (:grid (:workspace @db))
        [grid viewport-width viewport-height document-start-x document-start-y 100])
      ]]))
