(ns uxbox.shapes.circle
  (:require
   rum
   [uxbox.svg :as svg]
   [uxbox.shapes.protocols :as proto]
   [uxbox.geometry :as geo]))

(rum/defc circlec < rum/static
  [{:keys [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate visible]}]
  [:circle {:cx cx
            :cy cy
            :r r
            :fill fill
            :fillOpacity fill-opacity
            :stroke stroke
            :strokeWidth stroke-width
            :stroke-opacity stroke-opacity
            :style #js {:visibility (if visible "visible" "hidden")}
            :transform (svg/transform {:rotate rotate
                                       :center {:x cx :y cy}})}])

(rum/defc selected-circlec < rum/static
  [{:keys [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
  [:g
   [:rect {:x (- cx r 4)
           :y (- cy r 4)
           :width (+ 8 (* r 2))
           :height (+ 8 (* r 2))
           :fill "transparent"
           :stroke "#4af7c3"
           :strokeWidth 2
           :strokeDasharray "5,5"
           :fill-opacity "0.5"
           :transform (svg/transform {:rotate rotate :center {:x cx :y cy}})}]
   [:rect {:x (- cx r 8) :y (- cy r 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
   [:rect {:x (+ cx r)   :y (+ cy r)   :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
   [:rect {:x (+ cx r)   :y (- cy r 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
   [:rect {:x (- cx r 8) :y (+ cy r)   :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]])

(rum/defc drawing-circlec < rum/static
  [cx cy r]
  [:circle {:cx cx
            :cy cy
            :r r
            :style #js {:fill "transparent"
                        :stroke "gray"
                        :strokeDasharray "5,5"}}])

(defrecord Circle [name cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate visible locked]
  IComparable
  (-compare [_ other]
    (compare cx (.-cx other)))

  proto/Shape
  (intersect
    [{:keys [cx cy r]} px py]
    (let [distance (geo/distance-line-circle cx cy r px py)]
      (<= distance 15)))

  (toolbar-coords
    [{:keys [cx cy r]}]
    (let [vx (+ cx r 20)
          vy (- cy r 40)]
      [vx vy]))

  (shape->svg
    [shape]
    (circlec shape))

  (shape->selected-svg
    [shape]
    (selected-circlec shape))

  (shape->drawing-svg
    [{:keys [cx cy r]}]
    (drawing-circlec cx cy r))

  (move-delta
    [{:keys [cx cy] :as shape} delta-x delta-y]
    (-> shape
        (assoc :cx (+ cx delta-x))
        (assoc :cy (+ cy delta-y))))

  (draw
    [{:keys [cx cy] :as shape} x y]
    (let [r (geo/distance cx cy x y)
          r (if (js/isNaN r) 0 r)
          dx (- (geo/distance cx cy cx 0) r)
          dy (- (geo/distance cx cy 0 cy) r)
          nr (if (or (< dx 0)
                    (< dy 0))
              (- r (Math/abs (min dx dy)))
              r)]
      (assoc shape :r nr))))

(defn new-circle
  "Retrieves a circle with the default parameters"
  [cx cy r]
  (Circle. "Circle" cx cy r "#cacaca" 1 "gray" 5 1 0 true false))
