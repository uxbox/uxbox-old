(ns uxbox.shapes.text
  (:require [uxbox.shapes.core :refer [Shape generate-transformation]]
            [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [cljs.reader :as reader]
            [reagent.core :refer [atom]]))

(defrecord Text [x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate]
  Shape

  (intersect [{:keys [x y width height]} px py]
    (and (>= px x)
         (<= px (+ x width))
         (>= py y)
         (<= py (+ y height))))

  (toolbar-coords [{:keys [x y width height]}]
    (let [vx (+ x width 50)
          vy (- y 50)]
      (geo/viewportcord->clientcoord vx vy)))

  (shape->svg [{:keys [x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
    [:text
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
      :transform (generate-transformation {:rotate rotate :center {:x (+ x (/ width 2)) :y (+ y (/ height 2))}})}])

  (shape->selected-svg [{:keys [x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
    [:rect {:x (- x 4)
            :y (- y 4)
            :width (+ width 8)
            :height (+ height 8)
            :fill "transparent"
            :stroke "#4af7c3"
            :strokeWidth 2
            :strokeDasharray "5,5"
            :fill-opacity "0.5"
            :rotate rotate
            :transform (generate-transformation {:rotate rotate :center {:x (+ x (/ width 2)) :y (+ y (/ height 2))}})}])

  (shape->drawing-svg [{:keys [x y]}]
    (let [coordinates (atom [[x y]])
          viewport-move (fn [state coord]
                          (reset! coordinates coord))]
      (pubsub/register-event :viewport-mouse-move viewport-move)
      (fn []
        (let [[mouseX mouseY] @coordinates
              [rect-x rect-y rect-width rect-height] (geo/coords->rect x y mouseX mouseY)]
          (if (and (> rect-width 0) (> rect-height 0))
            [:text {:x rect-x :y rect-y :width rect-width :height rect-height
                    :style #js {:fill "transparent" :stroke "gray" :strokeDasharray "5,5"}}])))))

  (move-delta
    [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))
  )

(defn new-text
  "Retrieves a text with the default parameters"
  [x y width height]
  (Text. x y width height 0 0 "#cacaca" 1 "gray" 5 1 0))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.text/Text) "/" ".") uxbox.shapes.text/map->Text)
