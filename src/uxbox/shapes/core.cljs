(ns uxbox.shapes.core
  (:require [uxbox.icons :as icons]
            [uxbox.pubsub :as pubsub]))

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

  (menu-info [shape]
    "Get the info to build the shape menu")
  )

(defn generate-transformation
  [{:keys [rotate center]}]
  (let [x (:x center) y (:y center)]
    (str "translate( "x" "y") rotate(" rotate ") translate( -"x" -"y")")))

(defn new-group [name order shape-uuid]
  {:name name
   :order order
   :visible true
   :locked false
   :icon :square
   :shapes [shape-uuid]})

(pubsub/register-transition
 :register-shape
 (fn [state shape-info]
   (assoc-in state [:components :tools (:key shape-info)] shape-info)))

(def stroke-menu {:name "Stroke"
                  :icon icons/stroke
                  :key :stroke
                  :options [{:name "Color" :inputs [{:name "Color" :type :color :shape-key :stroke :value-filter identity}]}
                            {:name "Opacity" :inputs [{:name "Opacity" :type :number :shape-key :stroke-opacity :value-filter float}]}
                            {:name "Width" :inputs [{:name "Width" :type :number :shape-key :stroke-width :value-filter int}]}]})

(def fill-menu {:name "Fill"
                :icon icons/fill
                :key :fill
                :options [{:name "Color" :inputs [{:name "Color" :type :color :shape-key :fill :value-filter identity}]}
                          {:name "Opacity" :inputs [{:name "Opacity" :type :number :shape-key :fill-opacity :value-filter float}]}]})

(def actions-menu {:name "Actions"
                   :icon icons/action
                   :key :actions
                   :options [{:name "Rotation" :inputs [{:name "Rotation" :type :number :shape-key :rotate :value-filter int}]}]})
