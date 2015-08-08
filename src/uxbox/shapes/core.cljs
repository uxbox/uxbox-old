(ns uxbox.shapes.core)

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
