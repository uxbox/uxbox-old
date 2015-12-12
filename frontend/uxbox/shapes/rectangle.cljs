(ns uxbox.shapes.rectangle
  (:require
   rum
   [uxbox.svg :as svg]
   [uxbox.shapes.protocols :as proto]
   [uxbox.geometry :as geo]))

(rum/defc rectanglec < rum/static
  [{:keys [x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate visible]}]
  [:rect
   {:x x
    :y y
    :width width
    :height height
    :fill fill
    :fillOpacity fill-opacity
    :rx rx
    :ry ry
    :stroke stroke
    :strokeWidth stroke-width
    :stroke-opacity stroke-opacity
    :rotate rotate
    :style #js {:visibility (if visible "visible" "hidden")}
    :transform (svg/transform {:rotate rotate
                               :center {:x (+ x (/ width 2)) :y (+ y (/ height 2))}})}])

(rum/defc selected-rectanglec < rum/static
  [{:keys [x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
  [:g
     [:rect
      {:x (- x 4)
       :y (- y 4)
       :width (+ width 8)
       :height (+ height 8)
       :fill "transparent"
       :stroke "#4af7c3"
       :strokeWidth 2
       :strokeDasharray "5,5"
       :fill-opacity "0.5"
       :rotate rotate
       :transform (svg/transform {:rotate rotate
                                  :center {:x (+ x (/ width 2)) :y (+ y (/ height 2))}})}]
      [:rect {:x (- x 8)
              :y (- y 8)
              :width 8
              :height 8
              :fill "#4af7c3"
              :fill-opacity "0.75"}]
      [:rect {:x (+ x width)
              :y (+ y height)
              :width 8
              :height 8
              :fill "#4af7c3"
              :fill-opacity "0.75"}]
      [:rect {:x (+ x width)
              :y (- y 8)
              :width 8
              :height 8
              :fill "#4af7c3"
              :fill-opacity "0.75"}]
      [:rect {:x (- x 8)
              :y (+ y height)
              :width 8
              :height 8
              :fill "#4af7c3"
              :fill-opacity "0.75"}]])

(rum/defc drawing-rectanglec < rum/static
  [{:keys [x y width height]}]
  [:rect
   {:x x
    :y y
    :width width
    :height height
    :style #js {:fill "transparent"
                :stroke "gray"
                :strokeDasharray "5,5"}}])

(defrecord Rectangle [name x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate visible locked]
  IComparable
  (-compare [_ other]
    (compare x (.-x other)))

  proto/Shape
  (intersect
    [{:keys [x y width height]} px py]
    (and (>= px x)
         (<= px (+ x width))
         (>= py y)
         (<= py (+ y height))))

  (toolbar-coords [{:keys [x y width height]}]
    (let [vx (+ x width 50)
          vy (- y 50)]
      [vx vy]))

  (shape->svg
    [shape]
    (rectanglec shape))

  (shape->selected-svg
    [shape]
    (selected-rectanglec shape))

  (shape->drawing-svg
    [shape]
    (drawing-rectanglec shape))

  (move-delta
    [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))

  ;; FIXME: not working as expected
  (draw
    [{:keys [x y] :as shape} mouse-x mouse-y]
    (let [[nx ny width height] (geo/coords->rect x y mouse-x mouse-y)]
      (merge shape {:x nx
                    :y ny
                    :width width
                    :height height}))))

(defn new-rectangle
  "Retrieves a line with the default parameters"
  [x y width height]
  (Rectangle. "Rectangle" x y width height 0 0 "#cacaca" 1 "gray" 5 1 0 true false))
