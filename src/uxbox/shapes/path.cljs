(ns uxbox.shapes.path
  (:require [uxbox.shapes.core :refer [Shape generate-transformation actions-menu fill-menu new-group]]
            [uxbox.pubsub :as pubsub]
            [uxbox.icons :as icons]
            [uxbox.geometry :as geo]
            [cljs.reader :as reader]
            [reagent.core :refer [atom]]))

(def path-menu {:name "Size and position"
                :icon icons/infocard
                :key :options
                :options [{:name "Position" :inputs [{:name "X" :type :number :shape-key :x :value-filter int}
                                                     {:name "Y" :type :number :shape-key :y :value-filter int}]}
                          {:name "Size" :inputs [{:name "Width" :type :number :shape-key :width :value-filter int}
                                                 {:name "lock" :type :lock}
                                                 {:name "Height" :type :number :shape-key :height :value-filter int}]}]})

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

  (menu-info
    [shape]
    [path-menu fill-menu actions-menu])
  )

(defn new-path-shape
  "Retrieves a path with the default parameters"
  [x y width height path icowidth icoheight]
  (Path. path icowidth icoheight x y width height "black" 1 0))

(defn drawing-path [state [x y] symbol]
 (if-let [drawing-val (get-in state [:page :drawing])]
   (let [shape-uuid (random-uuid)
         group-uuid (random-uuid)
         [rect-x rect-y rect-width rect-height] (geo/coords->rect x y (:x drawing-val) (:y drawing-val))
         new-group-order (->> state :groups vals (sort-by :order) last :order inc)
         shape-val (new-path-shape rect-x rect-y rect-width rect-height (-> symbol :svg second :d) 48 48)
         group-val (new-group (str (:name symbol) " " new-group-order) new-group-order shape-uuid)]

     (do (pubsub/publish! [:insert-group [group-uuid group-val]])
         (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
         (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:page :selected] shape-uuid)
              (assoc-in [:workspace :selected-tool] nil))))

   (assoc-in state [:page :drawing] (map->Path {:x x :y y}))))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.path/Path) "/" ".") uxbox.shapes.path/map->Path)
