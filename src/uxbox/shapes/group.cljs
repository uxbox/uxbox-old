(ns uxbox.shapes.group
  (:require
   rum
   [uxbox.shapes.core :as s]
   [uxbox.workspace.canvas.signals :refer [canvas-coordinates]]
   [uxbox.pubsub :as pubsub]
   [uxbox.icons :as icons]
   [uxbox.geometry :as geo]
   [uxbox.icons :as icons]
   [cljs.reader :as reader]))


;; THIS IS NOT READY TO USE, THIS WILL BE IMPLEMENTED AFTER OR WITH MULTISELECT

(def group-menu {:name "Size and position"
                 :icon icons/infocard
                 :key :options
                 :options [{:name "Position"
                            :inputs [{:name "X" :type :number :shape-key :x :value-filter int}
                                     {:name "Y" :type :number :shape-key :y :value-filter int}]}]})

(rum/defc groupc < rum/static
  [{:keys [x y rotate shapes visible]}]
  ;; TODO: Calc height and width based on child shapes
  (let [height (+ 10 x)
        width (+ 10 y)]
    [:g
     {:style #js {:visibility (if visible "visible" "hidden")}}
     [:rect {:x (- x 4)
             :y (- y 4)
             :rotate rotate
             :transform (s/generate-transformation {:rotate rotate
                                                    :center {:x (+ x (/ width 2))
                                                             :y (+ y (/ height 2))}})}]

      [:rect {:x (- x 8) :y (- y 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ x width) :y (+ y height) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ x width) :y (- y 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (- x 8) :y (+ y height) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]]))

(rum/defc selected-groupc < rum/static
  [{:keys [x y rotate shapes]}]
  ;; TODO: Calc height and width based on child shapes
  (let [height (+ 10 x)
        width (+ 10 y)]
    [:g
     [:rect {:x (- x 4)
             :y (- y 4)
             :rotate rotate
             :transform (s/generate-transformation {:rotate rotate
                                                    :center {:x (+ x (/ width 2))
                                                             :y (+ y (/ height 2))}})}]

      [:rect {:x (- x 8) :y (- y 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ x width) :y (+ y height) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ x width) :y (- y 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (- x 8) :y (+ y height) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]]))

(rum/defc drawing-groupc < rum/reactive
  [x y]
  (let [[mouse-x mouse-y] (rum/react canvas-coordinates)
        [rect-x rect-y rect-width rect-height] (geo/coords->rect x y mouse-x mouse-y)]
    (when (and (> rect-width 0)
               (> rect-height 0))
      [:rect
       {:x rect-x
        :y rect-y
        :width rect-width
        :height rect-height
        :style #js {:fill "transparent"
                    :stroke "gray"
                    :strokeDasharray "5,5"}}])))

(defrecord Group [name x y rotate shapes visible locked]
  s/Shape

  (intersect [{:keys [shapes]} px py]
    (apply some (map s/intersect shapes)))

  (toolbar-coords [{:keys [x y shapes]}]
    (let [vx (+ x 50)
          vy (- y 50)]
      (geo/viewportcoord->clientcoord vx vy)))


  (shape->svg
    [shape]
    (groupc shape))

  (shape->selected-svg
    [shape]
    (selected-groupc shape))

  (shape->drawing-svg [{:keys [x y]}]
    (drawing-groupc x y))

  (move-delta
    [{:keys [x y] :as shape} delta-x delta-y]
    (-> shape
        (assoc :x (+ x delta-x))
        (assoc :y (+ y delta-y))))

  (menu-info
    [shape]
    [group-menu s/actions-menu][])

  (icon [_] icons/box))

(defn new-group
  "Retrieves a line with the default parameters"
  [x y shapes]
  (Group. "Group" x y 0 shapes true false))

(defn drawing-group [state [x y]]
 (if-let [drawing-val (get-in state [:page :drawing])]
   (let [shape-uuid (random-uuid)
         [rect-x rect-y rect-width rect-height] (geo/coords->rect x y (:x drawing-val) (:y drawing-val))
         shape-val (new-group rect-x rect-y [])]

     (do (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
         (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:page :selected] shape-uuid)
              (assoc-in [:workspace :selected-tool] nil))))

   (assoc-in state [:page :drawing] (map->Group {:x x :y y}))))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.group/Group) "/" ".") uxbox.shapes.group/map->Group)
