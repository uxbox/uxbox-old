(ns uxbox.shapes.protocols)

;; TODO: Decomplect
(defprotocol Shape
  (intersect [shape px py] ;; rename to contains
    "Retrieves true when the point (px, py) is inside the shape")

  (toolbar-coords [shape] ;; simple coords, handle conversion on UI layer
    "Retrieves a pair of coordinates (px, py) where the toolbar has to be displayed for this shape")

  (shape->svg [shape]
    "Returns the component for the SVG of static shape")

  (shape->selected-svg [shape]
    "Returns the component for the SVG of the elements selecting the shape")

  (shape->drawing-svg [shape]
    "Returns the component for the SVG of the shape while is bein drawed")

  (move-delta [shape delta-x delta-y]
    "Moves the shape to an increment given by the delta-x and delta-y coordinates")

  (menu-info [shape]
    "Get the info to build the shape menu")

  (icon [shape]
    "Get the icon of the shape"))
