(ns uxbox.shapes.core
  (:require [uxbox.icons :as icons]))

(defn generate-transformation
  [{:keys [rotate center]}]
  (let [x (:x center) y (:y center)]
    (str "translate( " x "," y ") rotate(" rotate ") translate( -" x ", -" y ")")))

(def stroke-menu {:name "Stroke"
                  :icon icons/stroke
                  :key :stroke
                  :options [{:name "Color"
                             :inputs [{:name "Color" :type :color :shape-key :stroke :value-filter identity}]}
                            {:name "Opacity"
                             :inputs [{:name "Opacity" :type :number :shape-key :stroke-opacity :value-filter float}]}
                            {:name "Width"
                             :inputs [{:name "Width" :type :number :shape-key :stroke-width :value-filter int}]}]})

(def fill-menu {:name "Fill"
                :icon icons/fill
                :key :fill
                :options [{:name "Color"
                           :inputs [{:name "Color" :type :color :shape-key :fill :value-filter identity}]}
                          {:name "Opacity"
                           :inputs [{:name "Opacity" :type :number :shape-key :fill-opacity :value-filter float}]}]})

(def actions-menu {:name "Actions"
                   :icon icons/action
                   :key :actions
                   :options [{:name "Rotation"
                              :inputs [{:name "Rotation" :type :number :shape-key :rotate :value-filter int}]}]})
