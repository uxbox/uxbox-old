(ns uxbox.geometry)

;; TODO: consider goog.math

(defn clamp
  [[x y]]
  (let [cx (max 0 x)
        cy (max 0 y)]
    [cx cy]))

(defn coords->rect
  "Given the (x1,y1) and (x2,y2) coordinates return the rectangle that
  define as (top-left corner, width, height)"
  [x1 y1 x2 y2]
  (let [rect-x (if (> x1 x2) x2 x1)
        rect-y (if (> y1 y2) y2 y1)
        rect-width (if (> x1 x2) (- x1 x2) (- x2 x1))
        rect-height (if (> y1 y2) (- y1 y2) (- y2 y1))]
    [rect-x rect-y rect-width rect-height]))

(defn slope [x1 y1 x2 y2]
  (/ (- y1 y2) (- x1 x2)))

(defn distance2 [x1 y1 x2 y2]
  (let [deltax (- x1 x2)
        deltay (- y1 y2)
        deltaxsq (* deltax deltax)
        deltaysq (* deltay deltay)]
    (+ deltaxsq deltaysq)))

(defn distance [x1 y1 x2 y2]
  (.sqrt js/Math (distance2 x1 y1 x2 y2)))

;; http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
(defn distance-line-point2 [vx vy wx wy px py]
  (let [l2 (distance2 vx vy wx wy)]
    (if (= l2 0)
      (distance2 px py vx vy)
      (let [t (/ (+ (* (- px vx) (- wx vx))
                    (* (- py vy) (- wy vy)))
                 l2)]
        (cond (< t 0) (distance2 px py vx vy)
              (> t 1) (distance2 px py wx wy)
              :else (let [newx (+ vx (* t (- wx vx)))
                          newy (+ vy (* t (- wy vy)))]
                      (distance2 px py newx newy)))))))

(defn distance-line-point [vx vy wx wy px py]
  (.sqrt js/Math (distance-line-point2 vx vy wx wy px py)))

(defn distance-line-circle [cx cy r px py]
  (let [d (distance cx cy px py)]
    (- d r)))
