(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn drawing-rect
  [coordinates]
  (pubsub/publish! [:drawing-rect coordinates]))

(pubsub/register-transition
 :drawing-rect
 (fn [state [x y]]
   (if-let [drawing-val (get-in state [:page :drawing])]
     (let [shape-uuid (random-uuid)
           group-uuid (random-uuid)
           shape-val {:shape :rectangle
                      :x (:x drawing-val)
                      :y (:y drawing-val)
                      :width (- x (:x drawing-val))
                      :height (- y (:y drawing-val))
                      :fill "red"
                      :stroke "black"}
           group-val {:name "Temp" :order 100 :visible true :locked false
                      :icon :square :shapes [shape-uuid]}]
       (println shape-val)
       (println group-val)
       (-> state
           (assoc-in [:page :shapes shape-uuid] shape-val)
           (assoc-in [:page :groups group-uuid] group-val)
           (assoc-in [:page :drawing] nil)))
     (assoc-in state [:page :drawing] {:shape :rectangle :x x :y y}))))

