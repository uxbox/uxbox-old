(ns uxbox.shapes.circle
  (:require
   rum
   [jamesmacaulay.zelkova.signal :as z]
   [uxbox.workspace.canvas.signals :refer [canvas-coordinates]]
   [uxbox.mouse :as mouse]
   [uxbox.shapes.core :refer [Shape generate-transformation fill-menu actions-menu stroke-menu]]
   [uxbox.pubsub :as pubsub]
   [uxbox.icons :as icons]
   [uxbox.geometry :as geo]
   [uxbox.icons :as icons]
   [cljs.reader :as reader]))

(def circle-menu {:name "Size and position"
                  :icon icons/infocard
                  :key :options
                  :options [{:name "Position"
                             :inputs [{:name "X" :type :number :shape-key :cx :value-filter int}
                                      {:name "Y" :type :number :shape-key :cy :value-filter int}]}
                            {:name "Radius"
                             :inputs [{:name "Radius" :type :number :shape-key :r :value-filter int}]}]})

(rum/defc circlec < rum/static
  [{:keys [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate visible locked]}]
  [:circle {:cx cx
            :cy cy
            :r r
            :fill fill
            :fillOpacity fill-opacity
            :stroke stroke
            :strokeWidth stroke-width
            :stroke-opacity stroke-opacity
            :style #js {:visibility (if visible "visible" "hidden")}
            :transform (generate-transformation {:rotate rotate
                                                 :center {:x cx
                                                          :y cy}})}])

(rum/defc selected-circlec < rum/static
  [{:keys [cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate]}]
  [:g
      [:rect {:x (- cx r 4)
              :y (- cy r 4)
              :width (+ 8 (* r 2))
              :height (+ 8 (* r 2))
              :fill "transparent"
              :stroke "#4af7c3"
              :strokeWidth 2
              :strokeDasharray "5,5"
              :fill-opacity "0.5"
              :transform (generate-transformation {:rotate rotate :center {:x cx :y cy}})}]
      [:rect {:x (- cx r 8) :y (- cy r 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ cx r) :y (+ cy r) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (+ cx r) :y (- cy r 8) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]
      [:rect {:x (- cx r 8) :y (+ cy r) :width 8 :height 8 :fill "#4af7c3" :fill-opacity "0.75"}]])

(rum/defc drawing-circlec < rum/reactive
  [cx cy]
  (let [[mouse-x mouse-y] (rum/react canvas-coordinates)
         r (geo/distance cx cy mouse-x mouse-y)
         r (if (js/isNaN r) 0 r)
         dx (- (geo/distance cx cy cx 0) r)
         dy (- (geo/distance cx cy 0 cy) r)
         r (if (or (< dx 0)
                   (< dy 0))
             (- r (Math/abs (min dx dy)))
             r)]
    [:circle {:cx cx
              :cy cy
              :r r
              :style #js {:fill "transparent"
                          :stroke "gray"
                          :strokeDasharray "5,5"}}]))

(defrecord Circle [name cx cy r fill fill-opacity stroke stroke-width stroke-opacity rotate visible]
  Shape
  (intersect
    [{:keys [cx cy r]} px py]
    (let [distance (geo/distance-line-circle cx cy r px py)]
      (<= distance 15)))

  (toolbar-coords
    [{:keys [cx cy r]}]
    (let [vx (+ cx r 20)
          vy (- cy r 40)]
      (geo/viewportcoord->clientcoord vx vy)))

  (shape->svg
    [shape]
    (circlec shape))

  (shape->selected-svg
    [shape]
    (selected-circlec shape))

  (shape->drawing-svg
    [{:keys [cx cy]}]
    (drawing-circlec cx cy))

  (move-delta [{:keys [cx cy] :as shape} delta-x delta-y]
    (-> shape
        (assoc :cx (+ cx delta-x))
        (assoc :cy (+ cy delta-y))))

  (menu-info
    [shape]
    [circle-menu stroke-menu fill-menu actions-menu])

  (icon [_] icons/circle))

(defn new-circle
  "Retrieves a circle with the default parameters"
  [cx cy r]
  (Circle. "Circle" cx cy r "#cacaca" 1 "gray" 5 1 0 true false))

(defn drawing-circle [state [x y]]
  (if-let [drawing-val (get-in state [:page :drawing])]
    (let [shape-uuid (random-uuid)
          cx (:cx drawing-val)
          cy (:cy drawing-val)
          r (geo/distance x y cx cy)
          ;;Avoid drawing circles with negatives coordinates
          dx (- (geo/distance cx cy cx 0) r)
          dy (- (geo/distance cx cy 0 cy) r)
          r (if (or (< dx 0) (< dy 0)) (- r (Math/abs (min dx dy))) r)
          shape-val (new-circle (:cx drawing-val) (:cy drawing-val) r)]

      (do (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
          (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:page :selected] shape-uuid)
              (assoc-in [:workspace :selected-tool] nil))))

    (assoc-in state [:page :drawing] (map->Circle {:cx x :cy y}))))

(reader/register-tag-parser! (clojure.string/replace (pr-str uxbox.shapes.circle/Circle) "/" ".")
                             uxbox.shapes.circle/map->Circle)

(pubsub/publish! [:register-shape {:shape Circle
                                   :new new-circle
                                   :drawing drawing-circle
                                   :key :circle
                                   :icon icons/circle
                                   :text "Circle (Ctrl + E)"
                                   :menu :tools
                                   :order 20}])
