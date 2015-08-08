(ns uxbox.shapes.path
  (:require [uxbox.shapes.core :refer [Shape generate-transformation]]
            [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [cljs.reader :as reader]
            [reagent.core :refer [atom]]))

(defrecord Path [path icowidth icoheight x y width height fill fill-opacity rotate]
  Shape

  (intersect [{:keys [x y width height]} px py]
    (and (>= px x)
         (<= px (+ x width))
         (>= py y)
         (<= py (+ y height))))

  (toolbar-coords [{:keys [x y width height]}]
    (let [vx (+ x width 50)
          vy y]
      (geo/viewportcord->clientcoord vx vy)))

  (shape->svg [{:keys [path icowidth icoheight x y width height fill fill-opacity rotate]}]
    [:svg {:viewBox (str "0 0 " icowidth " " icoheight)
           :width width
           :height height
           :x x
           :y y
           :preserveAspectRatio "none"}
     [
      :g {:transform (generate-transformation {:rotate rotate :center {:x (/ icowidth 2) :y (/ icoheight 2)}})}
       [:path {:d path
               :fill fill
               :fill-opacity fill-opacity}]]])

  (shape->selected-svg [{:keys [path icowidth icoheight x y width height fill fill-opacity rotate]}]
    [:rect {:x x
            :y y
            :width width
            :height height
            :fill "transparent"
            :stroke "#4af7c3"
            :strokeWidth 2
            :strokeDasharray "5,5"
            :fill-opacity "0.5"}])

  (shape->drawing-svg [{:keys [x y]}]
    (let [coordinates (atom [[x y]])
          viewport-move (fn [state coord]
                          (reset! coordinates coord))]
      (pubsub/register-event :viewport-mouse-move viewport-move)
      (fn []
        (let [[mouseX mouseY] @coordinates
              [rect-x rect-y rect-width rect-height] (geo/coords->rect x y mouseX mouseY)]
          (if (and (> rect-width 0) (> rect-height 0))
            [:rect {:x rect-x :y rect-y :width rect-width :height rect-height
                    :style #js {:fill "transparent" :stroke "gray" :strokeDasharray "5,5"}}])))))

  (move-delta [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))
  )

(defn new-path-shape
  "Retrieves a path with the default parameters"
  [x y width height path icowidth icoheight]
  (Path. path icowidth icoheight x y width height "black" 1 0))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.core/Path) "/" ".") uxbox.shapes.core/map->Path)
