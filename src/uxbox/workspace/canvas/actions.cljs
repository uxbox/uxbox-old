(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]))


(defn drawing-shape
  [coordinates]
  (pubsub/publish! [:drawing-shape coordinates]))

(defn select-shape
  [coordinates]
  (pubsub/publish! [:select-shape coordinates]))

(defn new-rectangle [x y width height]
  {:shape :rectangle
   :x x
   :y y
   :width width
   :height height
   :fill "red"
   :stroke "black"})

(defn new-line [x1 y1 x2 y2]
  {:shape :line
   :x1 x1
   :y1 y1
   :x2 x2
   :y2 y2
   :stroke "pink"
   :stroke-width 2})

(defn new-group [name order shape-uuid]
  {:name name
   :order order
   :visible true
   :locked false
   :icon :square
   :shapes [shape-uuid]})

(defmulti intersect (fn [shape _] (:shape shape)))

(defmethod intersect :rectangle [{:keys [x y width height]} px py]
  (and (>= px x)
       (<= px (+ x width))
       (>= py y)
       (<= py (+ y height))))

(defmethod intersect :line [{:keys [x1 y1 x2 y2]} px py]
  (let [distance (geo/distance-line-point x1 y1 x2 y2 px py)]
    (<= distance 15)))

(defmethod intersect :default [_] false)

(pubsub/register-transition
 :select-shape
 (fn [state [x y]]
   ;(js* "debugger;")
   (let [selected-uuid
         (->> state
              :page :groups vals
              (sort-by #(- (:order %)))
              (filter :visible)
              (mapcat :shapes)
              (filter #(intersect (get-in state [:page :shapes %]) x y))
              first)]
     (assoc-in state [:page :selected] selected-uuid)) ))

(defn drawing-rect [state [x y]]
 (if-let [drawing-val (get-in state [:page :drawing])]
   (let [shape-uuid (random-uuid)
         group-uuid (random-uuid)
         [rect-x rect-y rect-width rect-height] (geo/coords->rect x y (:x drawing-val) (:y drawing-val))
         new-group-order (->> state :page :groups vals (sort-by :order) last :order inc)
         shape-val (new-rectangle rect-x rect-y rect-width rect-height)
         group-val (new-group (str "Group " new-group-order) new-group-order shape-uuid)]

     (do (pubsub/publish! [:insert-group [group-uuid group-val]])
         (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
         (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:workspace :selected-tool] nil))))

   (assoc-in state [:page :drawing] {:shape :rectangle :x x :y y})))

(defn drawing-line [state [x y]]
  (if-let [drawing-val (get-in state [:page :drawing])]
    (let [shape-uuid (random-uuid)
          group-uuid (random-uuid)
          new-group-order (->> state :page :groups vals (sort-by :order) last :order inc)
          shape-val (new-line (:x1 drawing-val) (:y1 drawing-val) x y)
          group-val (new-group (str "Group " new-group-order) new-group-order shape-uuid)]

      (do (pubsub/publish! [:insert-group [group-uuid group-val]])
          (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
          (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:workspace :selected-tool] nil))))

    (assoc-in state [:page :drawing] {:shape :line :x1 x :y1 y :x2 x :y2 y})))

(pubsub/register-transition
  :drawing-shape
  (fn [state coords]
   (let [selected-tool (get-in state [:workspace :selected-tool])]
     (cond
       (= selected-tool :rect) (drawing-rect state coords)
       (= selected-tool :line) (drawing-line state coords)
       :else state
       ))))

(pubsub/register-transition
 :insert-group
 (fn [state [group-uuid group-val]]
   (assoc-in state [:page :groups group-uuid] group-val)))


(pubsub/register-transition
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (assoc-in state [:page :shapes shape-uuid] shape-val)))

(pubsub/register-event
  :viewport-mouse-click
  (fn [state coords]
    (if (get-in state [:workspace :selected-tool])
      (drawing-shape coords)
      (select-shape coords))))
