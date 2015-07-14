(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]))

(defn drawing-shape
  [coordinates]
  (pubsub/publish! [:drawing-shape coordinates]))


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
         (assoc-in state [:page :drawing] nil)))

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
          (assoc-in state [:page :drawing] nil)))

    (assoc-in state [:page :drawing] {:shape :line :x1 x :y1 y :x2 x :y2 y})))

(pubsub/register-transition
  :drawing-shape
  (fn [state x y]
   (let [selected-tool (get-in state [:workspace :selected-tool])]
     (cond
       (= selected-tool :rect) (drawing-rect state x y)
       (= selected-tool :line) (drawing-line state x y)
     )
   )
  )
)

(pubsub/register-transition
 :insert-group
 (fn [state [group-uuid group-val]]
   (assoc-in state [:page :groups group-uuid] group-val)))


(pubsub/register-transition
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (assoc-in state [:page :shapes shape-uuid] shape-val)))
