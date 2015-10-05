(ns uxbox.shapes.icon
  (:require rum
            [uxbox.svg :as svg]
            [uxbox.geometry :as geo]
            [uxbox.shapes.protocols :as proto]))

(rum/defc iconc < rum/static
  [{:keys [x y width height icon]}]
  [:g
   {:transform (str (svg/translate x y) " " (svg/scale (/ width 100) (/ height 100)))}
   (:svg icon)])

(rum/defc drawing-iconc < rum/static
  [{:keys [x y width height icon]}]
  [:g
   {:transform (str (svg/translate x y) " " (svg/scale (/ width 100) (/ height 100)))
    :opacity 0.3}
   (:svg icon)])

(rum/defc selected-iconc < rum/static
  [{:keys [x y width height icon]}]
  [:g
   {:transform (str (svg/translate x y) " " (svg/scale (/ width 100) (/ height 100)))}
   (:svg icon)])

;; todo: width and height
(defrecord SvgIcon [icon x y width height]
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

  (move-delta
    [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))

  (draw
    [shape mouse-x mouse-y]
    (assoc shape :x mouse-x :y mouse-y)) ;; todo

  (toolbar-coords
    [shape]
    [x y]) ;; todo

  (shape->svg
    [shape]
    (iconc shape))

  (shape->drawing-svg
    [shape]
    (drawing-iconc shape))

  (shape->selected-svg
    [shape]
    (selected-iconc shape)))

(defn new-icon
  [icon x y width height]
  (SvgIcon. icon x y width height))
