(ns uxbox.shapes.line
  (:require
   rum
   [uxbox.workspace.tools :refer [register-drawing-tool!
                                  start-drawing]]
   [uxbox.workspace.canvas.signals :refer [canvas-coordinates]]
   [uxbox.shapes.core :refer [generate-transformation fill-menu actions-menu stroke-menu]]
   [uxbox.shapes.protocols :as proto]
   [uxbox.icons :as icons]
   [uxbox.geometry :as geo]
   [uxbox.icons :as icons]
   [cljs.reader :as reader]))

(def line-menu {:name "Position"
                :icon icons/infocard
                :key :options
                :options [{:name "Start"
                           :inputs [{:name "X" :type :number :shape-key :x1 :value-filter int}
                                    {:name "Y" :type :number :shape-key :y1 :value-filter int}]}
                          {:name "End"
                           :inputs [{:name "X" :type :number :shape-key :x2 :value-filter int}
                                    {:name "Y" :type :number :shape-key :y2 :value-filter int}]}]})


(rum/defc linec < rum/static
  [{:keys [x1 y1 x2 y2 stroke stroke-width stroke-opacity rotate visible]}]
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
              :style #js {:visibility (if visible "visible" "hidden")}
              :transform (generate-transformation {:rotate rotate :center {:x center-x :y center-y}})}]))

(rum/defc selected-linec < rum/static
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

(rum/defc drawing-linec < rum/static
  [x1 y1 x2 y2]
  [:line {:x1 x1
          :y1 y1
          :x2 x2
          :y2 y2
          :style #js {:fill "transparent"
                      :stroke "gray"
                      :strokeWidth 2
                      :strokeDasharray "5,5"}}])

(defrecord Line [name x1 y1 x2 y2 stroke stroke-width stroke-opacity rotate visible locked]
  ;; FIXME: arbitrary to make datascript happy
  IComparable
  (-compare [_ other]
    (compare x1 (.-x1 other)))

  proto/Shape
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
      (geo/viewportcoord->clientcoord vx vy)))

  (shape->svg
    [shape]
    (linec shape))

  (shape->selected-svg
    [shape]
    (selected-linec shape))

  (shape->drawing-svg
    [{:keys [x1 y1 x2 y2]}]
    (drawing-linec x1 y1 x2 y2))

  (move-delta
    [this dx dy]
    (merge this
           {:x1 (+ x1 dx)
            :x2 (+ x2 dx)
            :y1 (+ y1 dy)
            :y2 (+ y2 dy)}))

  (draw
    [this x y]
    (merge this
           {:x2 x
            :y2 y}))

  (menu-info
    [shape]
    [line-menu stroke-menu actions-menu])

  (icon [_] icons/line))

(defn new-line
  "Retrieves a line with the default parameters"
  [x1 y1 x2 y2]
  (Line. "Line" x1 y1 x2 y2 "gray" 4 1 0 true false))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.line/Line) "/" ".")
                             uxbox.shapes.line/map->Line)

(register-drawing-tool! {;;:shape Line
                         ;;:new new-line
                         :key :line
                         :icon icons/line
                         :text "Line (Ctrl + L)"
                         :menu :tools
                         :priority 30}) ;; pure data, can be in database (for example for configurable priority)

(defmethod start-drawing :line
  [_ [x y]]
  (new-line x y x y))
