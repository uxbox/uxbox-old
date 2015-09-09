(ns uxbox.shapes.path
  (:require
   rum
   [uxbox.workspace.canvas.signals :refer [canvas-coordinates]]
   [uxbox.shapes.protocols :as proto]
   [uxbox.shapes.core :refer [generate-transformation actions-menu fill-menu]]
   [uxbox.pubsub :as pubsub]
   [uxbox.icons :as icons]
   [uxbox.geometry :as geo]
   [cljs.reader :as reader]))

(def path-menu {:name "Size and position"
                :icon icons/infocard
                :key :options
                :options [{:name "Position"
                           :inputs [{:name "X" :type :number :shape-key :x :value-filter int}
                                    {:name "Y" :type :number :shape-key :y :value-filter int}]}
                          {:name "Size"
                           :inputs [{:name "Width" :type :number :shape-key :width :value-filter int}
                                    {:name "lock" :type :lock}
                                    {:name "Height" :type :number :shape-key :height :value-filter int}]}]})

(rum/defc pathc < rum/static
  [{:keys [path icowidth icoheight x y width height fill fill-opacity rotate visible]}]
  [:svg {:viewBox (str "0 0 " icowidth " " icoheight)
         :width width
         :height height
         :x x
         :y y
         :style #js {:visibility (if visible "visible" "hidden")}
         :preserveAspectRatio "none"}
     [:g
      {:transform (generate-transformation {:rotate rotate :center {:x (/ icowidth 2) :y (/ icoheight 2)}})}
      [:path {:d path
              :fill fill
              :fill-opacity fill-opacity}]]])

(rum/defc selected-pathc < rum/static
  [{:keys [path icowidth icoheight x y width height fill fill-opacity rotate]}]
  [:g
      [:rect {:x x
              :y y
              :width width
              :height height
              :fill "transparent"
              :stroke "#4af7c3"
              :strokeWidth 2
              :strokeDasharray "5,5"
              :fill-opacity "0.5"}]
      [:rect {:x (- x 8) :y (- y 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ x width) :y (+ y height) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ x width) :y (- y 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (- x 8) :y (+ y height) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]])

(rum/defc drawing-pathc < rum/reactive
  [x y]
  (let [[mouse-x mouse-y] (rum/react canvas-coordinates)
        [rect-x rect-y rect-width rect-height] (geo/coords->rect x y mouse-x mouse-y)]
    (when (and (> rect-width 0) (> rect-height 0))
      [:rect
       {:x rect-x
        :y rect-y
        :width rect-width
        :height rect-height
        :style #js {:fill "transparent"
                    :stroke "gray"
                    :strokeDasharray "5,5"}}])))

(defrecord Path [name path icowidth icoheight x y width height fill fill-opacity rotate visible locked]
  proto/Shape
  (intersect [{:keys [x y width height]} px py]
    (and (>= px x)
         (<= px (+ x width))
         (>= py y)
         (<= py (+ y height))))

  (toolbar-coords [{:keys [x y width height]}]
    (let [vx (+ x width 50)
          vy y]
      (geo/viewportcoord->clientcoord vx vy)))

  (shape->svg
    [shape]
    (pathc shape))

  (shape->selected-svg
    [shape]
    (selected-pathc shape))

  (shape->drawing-svg [{:keys [x y]}]
    (drawing-pathc x y))

  (move-delta [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))

  (menu-info
    [shape]
    [path-menu fill-menu actions-menu])

  (icon [shape]
    [:svg {:viewBox (str "0 0 " (:icowidth shape) " " (:icoheight shape))
           :width (:icowidth shape)
           :height (:icoheight shape)
           :x 0
           :y 0
           :visibility (if (:visible shape) "visible" "hidden")
           :preserveAspectRatio "none"}
       [:g
        [:path {:d (:path shape)}]]]))

(defn new-path-shape
  "Retrieves a path with the default parameters"
  [x y width height path icowidth icoheight]
  (Path. "Path" path icowidth icoheight x y width height "black" 1 0 true false))

(defn drawing-path [state [x y] symbol]
 (if-let [drawing-val (get-in state [:page :drawing])]
   (let [shape-uuid (random-uuid)
         [rect-x rect-y rect-width rect-height] (geo/coords->rect x y (:x drawing-val) (:y drawing-val))
         shape-val (new-path-shape rect-x rect-y rect-width rect-height (-> symbol :svg second :d) 48 48)]

     (do (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
         (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:page :selected] shape-uuid)
              (assoc-in [:workspace :selected-tool] nil))))

   (assoc-in state [:page :drawing] (map->Path {:x x :y y}))))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.path/Path) "/" ".")
                             uxbox.shapes.path/map->Path)
