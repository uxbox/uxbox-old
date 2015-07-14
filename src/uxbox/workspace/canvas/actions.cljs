(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]))

(defn drawing-rect
  [coordinates]
  (pubsub/publish! [:drawing-rect coordinates]))


(defn new-rectangle [x y width height]
  {:shape :rectangle
   :x x
   :y y
   :width width
   :height height
   :fill "red"
   :stroke "black"})

(defn new-group [name order shape-uuid]
  {:name name
   :order order
   :visible true
   :locked false
   :icon :square
   :shapes [shape-uuid]})


(pubsub/register-transition
 :drawing-rect
 (fn [state [x y]]
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

     (assoc-in state [:page :drawing] {:shape :rectangle :x x :y y}))))

(pubsub/register-transition
 :insert-group
 (fn [state [group-uuid group-val]]
   (assoc-in state [:page :groups group-uuid] group-val)))


(pubsub/register-transition
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (assoc-in state [:page :shapes shape-uuid] shape-val)))

