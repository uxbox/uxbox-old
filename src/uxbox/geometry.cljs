(ns uxbox.geometry)

(defn coords->rect
  "Given the (x1,y1) and (x2,y2) coordinates return the rectangle that
  define as (top-left corner, width, height)"
  [x1 y1 x2 y2]
  (let [rect-x (if (> x1 x2) x2 x1)
        rect-y (if (> y1 y2) y2 y1)
        rect-width (if (> x1 x2) (- x1 x2) (- x2 x1))
        rect-height (if (> y1 y2) (- y1 y2) (- y2 y1))]
    [rect-x rect-y rect-width rect-height]))

(defn clientcoord->viewportcord
  [client-x client-y]
  (let [canvas-element (.getElementById js/document "page-canvas")
        bounding-rect (.getBoundingClientRect canvas-element)
        offset-x (.-left bounding-rect)
        offset-y (.-top bounding-rect)
        new-x (- client-x offset-x)
        new-y (- client-y offset-y)]
    [new-x new-y]))
