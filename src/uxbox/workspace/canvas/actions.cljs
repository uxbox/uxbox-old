(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [uxbox.shapes.core :as shapes]
            [uxbox.workspace.figures.catalogs :refer [catalogs]]))

(defn drawing-shape
  [coordinates]
  (pubsub/publish! [:drawing-shape coordinates]))

(defn select-shape
  [coordinates]
  (pubsub/publish! [:select-shape coordinates]))

(defn new-group [name order shape-uuid]
  {:name name
   :order order
   :visible true
   :locked false
   :icon :square
   :shapes [shape-uuid]})

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
              (filter #(shapes/intersect (get-in state [:page :shapes %]) x y))
              first)]
     (assoc-in state [:page :selected] selected-uuid)) ))

(defn drawing-rect [state [x y]]
 (if-let [drawing-val (get-in state [:page :drawing])]
   (let [shape-uuid (random-uuid)
         group-uuid (random-uuid)
         [rect-x rect-y rect-width rect-height] (geo/coords->rect x y (:x drawing-val) (:y drawing-val))
         new-group-order (->> state :page :groups vals (sort-by :order) last :order inc)
         shape-val (shapes/new-rectangle rect-x rect-y rect-width rect-height)
         group-val (new-group (str "Group " new-group-order) new-group-order shape-uuid)]

     (do (pubsub/publish! [:insert-group [group-uuid group-val]])
         (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
         (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:workspace :selected-tool] nil))))

   (assoc-in state [:page :drawing] (shapes/map->Rectangle {:x x :y y}))))

(defn drawing-line [state [x y]]
  (if-let [drawing-val (get-in state [:page :drawing])]
    (let [shape-uuid (random-uuid)
          group-uuid (random-uuid)
          new-group-order (->> state :page :groups vals (sort-by :order) last :order inc)
          shape-val (shapes/new-line (:x1 drawing-val) (:y1 drawing-val) x y)
          group-val (new-group (str "Group " new-group-order) new-group-order shape-uuid)]

      (do (pubsub/publish! [:insert-group [group-uuid group-val]])
          (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
          (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:workspace :selected-tool] nil))))

    (assoc-in state [:page :drawing] (shapes/map->Line {:x1 x :y1 y :x2 x :y2 y}))))

(defn drawing-path [state [x y] symbol]
 (if-let [drawing-val (get-in state [:page :drawing])]
   (let [shape-uuid (random-uuid)
         group-uuid (random-uuid)
         [rect-x rect-y rect-width rect-height] (geo/coords->rect x y (:x drawing-val) (:y drawing-val))
         new-group-order (->> state :page :groups vals (sort-by :order) last :order inc)
         shape-val (shapes/new-path rect-x rect-y rect-width rect-height (-> symbol :svg second :d) 48 48)
         group-val (new-group (str (:name symbol) " " new-group-order) new-group-order shape-uuid)]

     (do (pubsub/publish! [:insert-group [group-uuid group-val]])
         (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
         (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:workspace :selected-tool] nil))))

   (assoc-in state [:page :drawing] (shapes/map->Path {:x x :y y}))))

(defn drawing-circle [state [x y]]
  (if-let [drawing-val (get-in state [:page :drawing])]
    (let [shape-uuid (random-uuid)
          group-uuid (random-uuid)
          new-group-order (->> state :page :groups vals (sort-by :order) last :order inc)
          r (geo/distance x y (:cx drawing-val) (:cy drawing-val))
          shape-val (shapes/new-circle (:cx drawing-val) (:cy drawing-val) r)
          group-val (new-group (str "Group " new-group-order) new-group-order shape-uuid)]

      (do (pubsub/publish! [:insert-group [group-uuid group-val]])
          (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
          (-> state
              (assoc-in [:page :drawing] nil)
              (assoc-in [:workspace :selected-tool] nil))))

    (assoc-in state [:page :drawing] (shapes/map->Circle {:cx x :cy y}))))

(pubsub/register-transition
  :drawing-shape
  (fn [state coords]
   (let [selected-tool (get-in state [:workspace :selected-tool])]
     (cond
       (= selected-tool :rect) (drawing-rect state coords)
       (= selected-tool :line) (drawing-line state coords)
       (= selected-tool :circle) (drawing-circle state coords)
       (= (first selected-tool) :figure)
         (let [[_ catalog symbol] selected-tool]
           (drawing-path state coords (get-in catalogs [catalog :symbols symbol])))       
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
