(ns uxbox.shapes.core
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [cljs.reader :as reader]
            [reagent.core :refer [atom]]))

;;=============================
;; SHAPE PROTOCOL DEFINITION
;;=============================
(defprotocol Shape
  (intersect [shape px py]
    "Retrieves true when the point (px, py) is inside the shape")

  (toolbar-coords [shape]
    "Retrieves a pair of coordinates (px, py) where the toolbar has to be displayed for this shape")

  (shape->svg [shape]
    "Returns the markup for the SVG of static shape")

  (shape->selected-svg [shape]
    "Returns the markup for the SVG of the elements selecting the shape")

  (shape->drawing-svg [shape]
    "Returns the markup for the SVG of the shape while is bein drawed")

  (move-delta [shape delta-x delta-y]
    "Moves the shape to an increment given by the delta-x and delta-y coordinates")
  )

(defn generate-transformation
  [{:keys [rotate center]}]
  (let [x (:x center) y (:y center)]
    (str "translate( "x" "y") rotate(" rotate ") translate( -"x" -"y")")))

;;=============================
;; LINES
;;=============================
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

;;=============================
;; RECTANGLES
;;=============================
(defrecord Rectangle [x y width height rx ry fill fill-opacity stroke stroke-width stroke-opacity rotate]
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
            [:rect {:x rect-x :y rect-y :width rect-width :height rect-height
                    :style #js {:fill "transparent" :stroke "gray" :strokeDasharray "5,5"}}])))))

  (move-delta
    [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))
  )

(defn new-rectangle
  "Retrieves a line with the default parameters"
  [x y width height]
  (Rectangle. x y width height 0 0 "#cacaca" 1 "gray" 5 1 0))

;;=============================
;; PATH
;;=============================
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

;;=============================
;; CIRCLE
;;=============================
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

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.core/Rectangle) "/" ".") uxbox.shapes.core/map->Rectangle)
(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.core/Line) "/" ".") uxbox.shapes.core/map->Line)
(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.core/Path) "/" ".") uxbox.shapes.core/map->Path)
(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.core/Circle) "/" ".") uxbox.shapes.core/map->Circle)
