(ns uxbox.shapes.line
  (:require [uxbox.shapes.core :refer [Shape generate-transformation]]
            [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [cljs.reader :as reader]
            [reagent.core :refer [atom]]))

(defrecord Line [x1 y1 x2 y2 stroke stroke-width stroke-opacity rotate]
  Shape

  (intersect
    [{:keys [x1 y1 x2 y2]} px py]
    (let [distance (geo/distance-line-point x1 y1 x2 y2 px py)]
      (<= distance 15)))

  (toolbar-coords
    [{:keys [x1 y1 x2 y2]}]
    (let [max-x (if (> x1 x2) x1 x2)
          min-y (if (< y1 y2) y1 y2)
          vx (+ max-x 50)
          vy min-y]
      (geo/viewportcord->clientcoord vx vy)))

  (shape->svg
    [{:keys [x1 y1 x2 y2 stroke stroke-width stroke-opacity rotate]}]
    (let [length-x (geo/distance x1 0 x2 0)
          length-y (geo/distance 0 y1 0 y2)
          min-x (min x1 x2)
          min-y (min y1 y2)
          center-x (+ min-x (/ length-x 2))
          center-y (+ min-y (/ length-y 2))]
      [:line {:x1 x1
              :y1 y1
              :x2 x2
              :y2 y2
              :stroke stroke
              :strokeWidth stroke-width
              :stroke-opacity stroke-opacity
              :transform (generate-transformation {:rotate rotate :center {:x center-x :y center-y}})}]))

  (shape->selected-svg
    [{:keys [x1 y1 x2 y2 stroke stroke-width stroke-opacity rotate]}]
    (let [length-x (geo/distance x1 0 x2 0)
          length-y (geo/distance 0 y1 0 y2)
          min-x (min x1 x2)
          min-y (min y1 y2)
          center-x (+ min-x (/ length-x 2))
          center-y (+ min-y (/ length-y 2))]
      [:g {:transform (generate-transformation {:rotate rotate :center {:x center-x :y center-y}})}
        [:rect {:x (- x1 4)
                 :y (- y1 4)
                 :width 8
                 :height 8
                 :fill "#4af7c3"
                 :fill-opacity "0.75"}]
         [:rect {:x (- x2 4)
                 :y (- y2 4)
                 :width 8
                 :height 8
                 :fill "#4af7c3"
                 :fill-opacity "0.75"}]]))

  (shape->drawing-svg
    [{:keys [x1 y1 x2 y2]}]
    (let [coordinates1 (atom [x1 y1])
          coordinates2 (atom [x2 y2])
          viewport-move (fn [state coords]
                          (reset! coordinates2 coords))]
      (pubsub/register-event :viewport-mouse-move viewport-move)
      (fn []
        (let [[mouseX mouseY] @coordinates2]
          [:line {:x1 x1 :y1 y1 :x2 mouseX :y2 mouseY
                  :style #js {:fill "transparent" :stroke "gray" :stroke-width 2 :strokeDasharray "5,5"}}]))))

  (move-delta
    [{:keys [x1 y1 x2 y2] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x1 (+ x1 delta-x))
        (assoc :y1 (+ y1 delta-y))
        (assoc :x2 (+ x2 delta-x))
        (assoc :y2 (+ y2 delta-y))))
  )

(defn new-line
  "Retrieves a line with the default parameters"
  [x1 y1 x2 y2]
  (Line. x1 y1 x2 y2 "gray" 4 1 0))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.line/Line) "/" ".") uxbox.shapes.line/map->Line)
