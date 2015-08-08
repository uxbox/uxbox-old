(ns uxbox.shapes.circle
  (:require [uxbox.shapes.core :refer [Shape generate-transformation]]
            [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [cljs.reader :as reader]
            [reagent.core :refer [atom]]))

(defrecord Circle [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate]
  Shape

  (intersect
    [{:keys [cx cy r]} px py]
    (let [distance (geo/distance-line-circle cx cy r px py)]
      (<= distance 15)))

  (toolbar-coords
    [{:keys [cx cy r]}]
    (let [vx (+ cx r 20)
          vy (- cy r 40)]
      (geo/viewportcord->clientcoord vx vy)))

  (shape->svg [{:keys [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
    [:circle {:cx cx
              :cy cy
              :r r
              :fill fill
              :fillOpacity fill-opacity
              :stroke stroke
              :strokeWidth stroke-width
              :stroke-opacity stroke-opacity
              :transform (generate-transformation {:rotate rotate :center {:x cx :y cy}})}])

  (shape->selected-svg [{:keys [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
    [:rect {:x (- cx r 4)
            :y (- cy r 4)
            :width (+ 8 (* r 2))
            :height (+ 8 (* r 2))
            :fill "transparent"
            :stroke "#4af7c3"
            :strokeWidth 2
            :strokeDasharray "5,5"
            :fill-opacity "0.5"
            :transform (generate-transformation {:rotate rotate :center {:x cx :y cy}})}])


  (shape->drawing-svg [{:keys [cx cy r]}]
    (let [coordinates (atom [[cx cy]])
          viewport-move (fn [state coord]
                          (reset! coordinates coord))]
      (pubsub/register-event :viewport-mouse-move viewport-move)
      (fn []
        (let [[mouseX mouseY] @coordinates
              r (geo/distance cx cy mouseX mouseY)
              r (if (js/isNaN r) 0 r)
              dx (- (geo/distance cx cy cx 0) r)
              dy (- (geo/distance cx cy 0 cy) r)
              r (if (or (< dx 0) (< dy 0)) (- r (Math/abs (min dx dy))) r)]
          [:circle {:cx cx :cy cy :r r
                    :style #js {:fill "transparent" :stroke "gray" :strokeDasharray "5,5"}}]
          ))))

  (move-delta [{:keys [cx cy] :as shape} delta-x delta-y]
    (-> shape
        (assoc :cx (+ cx delta-x))
        (assoc :cy (+ cy delta-y))))
  )

(defn new-circle
  "Retrieves a circle with the default parameters"
  [cx cy r]
  (Circle. cx cy r "#cacaca" 1 "gray" 5 1 0))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.circle/Circle) "/" ".") uxbox.shapes.circle/map->Circle)
