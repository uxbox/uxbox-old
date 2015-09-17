(ns uxbox.ui.shapes
  (:require
   [uxbox.ui.icon-sets.core]
   [uxbox.ui.tools :refer [register-drawing-tool! start-drawing]]
   [uxbox.ui.icons :as icons]
   [uxbox.shapes.protocols :as proto]
   [uxbox.shapes.rectangle :as rect]
   [uxbox.shapes.circle :as circle]
   [uxbox.shapes.line :as line]
   [uxbox.shapes.icon :as icon]
   [cljs.reader :as reader]))

(defn register-shape!
 [type parser]
 (reader/register-tag-parser! (clojure.string/replace (pr-str type) "/" ".") parser))

;; FIXME: put in DB

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

;; ================================================================================
;; Rectangle

(register-shape! rect/Rectangle rect/map->Rectangle)

(extend-type rect/Rectangle
  proto/Icon
  (name [s] (:name s))
  (icon [_] icons/box))

(defmethod start-drawing :rect
  [_ [x y]]
  (rect/new-rectangle x y 0 0))

(def rectangle-menu {:name "Size and position"
                     :icon icons/infocard
                     :key :options
                     :options [{:name "Position"
                                :inputs [{:name "X" :type :number :shape-key :x :value-filter int}
                                         {:name "Y" :type :number :shape-key :y :value-filter int}]}
                               {:name "Size"
                                :inputs [{:name "Width" :type :number :shape-key :width :value-filter int}
                                         {:name "lock" :type :lock}
                                         {:name "Height" :type :number :shape-key :height :value-filter int}]}]})

(register-drawing-tool! {:key :rect
                         :icon icons/box
                         :text "Box (Ctrl + B)" ;; TODO: i18n
                         :menu :tools
                         :menu-info [rectangle-menu stroke-menu fill-menu actions-menu]
                         :priority 10})


;; ================================================================================
;; Circle

(register-shape! circle/Circle circle/map->Circle)

(extend-type circle/Circle
  proto/Icon
  (name [s] (:name s))
  (icon [_] icons/circle))

(defmethod start-drawing :circle
  [_ [x y]]
  (circle/new-circle x y 10))

(def circle-menu {:name "Size and position"
                  :icon icons/infocard
                  :key :options
                  :options [{:name "Position"
                             :inputs [{:name "X" :type :number :shape-key :cx :value-filter int}
                                      {:name "Y" :type :number :shape-key :cy :value-filter int}]}
                            {:name "Radius"
                             :inputs [{:name "Radius" :type :number :shape-key :r :value-filter int}]}]})

(register-drawing-tool! {:key :circle
                         :icon icons/circle
                         :text "Circle (Ctrl + E)" ;; TODO: i18n
                         :menu :tools
                         :menu-info [circle-menu stroke-menu actions-menu]
                         :priority 20})

;; ================================================================================
;; Line

(register-shape! line/Line line/map->Line)

(extend-type line/Line
  proto/Icon
  (name [s] (:name s))
  (icon [_] icons/line))

(defmethod start-drawing :line
  [_ [x y]]
  (line/new-line x y x y))

(def line-menu {:name "Position"
                :icon icons/infocard
                :key :options
                :options [{:name "Start"
                           :inputs [{:name "X" :type :number :shape-key :x1 :value-filter int}
                                    {:name "Y" :type :number :shape-key :y1 :value-filter int}]}
                          {:name "End"
                           :inputs [{:name "X" :type :number :shape-key :x2 :value-filter int}
                                    {:name "Y" :type :number :shape-key :y2 :value-filter int}]}]})

(register-drawing-tool! {:key :line
                         :icon icons/line
                         :text "Line (Ctrl + L)"
                         :menu :tools
                         :menu-info [line-menu stroke-menu actions-menu]
                         :priority 30})

;; ================================================================================
;; Icon

(register-shape! icon/SvgIcon icon/map->SvgIcon)

(extend-type icon/SvgIcon
  proto/Icon
  (name [s]
    (get-in s [:icon :name]))

  (icon
    [shape]
    (let [i (get-in shape [:icon :svg])]
      [:svg
       {:viewBox "0 0 50 50"}
       i])))

(defmethod start-drawing :icon
  [[_ icon] [x y]]
  (icon/new-icon icon x y))
