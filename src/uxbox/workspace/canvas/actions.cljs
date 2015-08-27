(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [uxbox.storage.api :as storage]
            [uxbox.shapes.core :as shapes]
            [uxbox.shapes.line :refer [new-line map->Line]]
            [uxbox.shapes.rectangle :refer [new-rectangle map->Rectangle]]
            [uxbox.shapes.circle :refer [new-circle map->Circle]]
            [uxbox.shapes.path :refer [new-path-shape map->Path drawing-path]]))

(defn drawing-shape
  [coordinates]
  (pubsub/publish! [:drawing-shape coordinates]))

(defn select-shape
  [coordinates]
  (pubsub/publish! [:select-shape coordinates]))

(pubsub/register-transition
 :select-shape
 (fn [state [x y]]
   (let [selected-uuid
         (->> state
              :groups vals
              (sort-by #(- (:order %)))
              (filter :visible)
              (mapcat :shapes)
              (filter #(not (nil? (get-in state [:shapes %]))))
              (filter #(shapes/intersect (get-in state [:shapes %]) x y))
              first)]
     (assoc-in state [:page :selected] selected-uuid)) ))

(pubsub/register-transition
  :drawing-shape
  (fn [state coords]
   (let [selected-tool (get-in state [:workspace :selected-tool])
         list-of-tools (get-in state [:components :tools])]
     (cond
       (contains? list-of-tools selected-tool) ((get-in list-of-tools [selected-tool :drawing])  state coords)
       (= (first selected-tool) :icon)
         (let [[_ catalog icon] selected-tool]
           (drawing-path state coords (get-in state [:components :icons-sets catalog :icons icon])))
       :else state
       ))))

(pubsub/register-transition
 :insert-group
 (fn [state [group-uuid group-val]]
   (assoc-in state [:groups group-uuid] group-val)))


(pubsub/register-transition
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (assoc-in state [:shapes shape-uuid] shape-val)))

(pubsub/register-effect
 :insert-group
 (fn [state [group-uuid group-val]]
   (let [project-uuid (get-in state [:project :uuid])
         page-uuid (get-in state [:page :uuid])]
     (storage/create-group project-uuid page-uuid group-uuid group-val))))

(pubsub/register-effect
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (let [project-uuid (get-in state [:project :uuid])
         page-uuid (get-in state [:page :uuid])]
     (storage/create-shape project-uuid page-uuid shape-uuid shape-val))))

(pubsub/register-event
  :viewport-mouse-click
  (fn [state coords]
    (if (get-in state [:workspace :selected-tool])
      (drawing-shape coords)
      (select-shape coords))))

(defn remove-element [groups-entry element-uuid]
  (let [in? (fn [seq elm] (some #(= elm %) seq))
        has-element? (fn [[_ val]] (in? (:shapes val) element-uuid)  )
        owner-uuid (->> groups-entry (filter has-element?) first first)
        remove-vector-element (fn [v el] (vector (filter #(not (= % el)) v)))]
    (cond
      (nil? owner-uuid) groups-entry
      (= 1 (-> groups-entry (get owner-uuid) :shapes count)) (dissoc groups-entry owner-uuid)
      :else (update-in groups-entry [:shapes] remove-vector-element element-uuid)
      )))

(pubsub/register-transition
  :delete-key-pressed
  (fn [state]
    (let [selected-uuid (get-in state [:page :selected])
          project-uuid (get-in state [:project :uuid])
          page-uuid (get-in state [:page :uuid])]

      (when selected-uuid
         (storage/remove-shape project-uuid page-uuid selected-uuid))

      (if selected-uuid
         (-> state
            (update-in [:groups] remove-element selected-uuid)
            (update-in [:shapes] dissoc selected-uuid)
            (update-in [:page] dissoc :selected))
         state))))

(pubsub/register-transition
 :viewport-mouse-down
 (fn [state]
   (if-let [selected-uuid (get-in state [:page :selected])]
     (let [coors {:x (get-in state [:shapes selected-uuid :x]) :y (get-in state [:shapes selected-uuid :y])}]
       (-> state
           (update-in [:shapes selected-uuid] assoc :dragging-coors coors)
           (update-in [:shapes selected-uuid] assoc :dragging true)))
     state)))

(pubsub/register-transition
 :viewport-mouse-up
 (fn [state]
   (if-let [selected-uuid (get-in state [:page :selected])]
     (let [x (get-in state [:shapes selected-uuid :x])
           y (get-in state [:shapes selected-uuid :y])
           old-x (get-in state [:shapes selected-uuid :dragging-coors :x])
           old-y (get-in state [:shapes selected-uuid :dragging-coors :y])
           deltax (- x old-x)
           deltay (- y old-y)
           project-uuid (get-in state [:project :uuid])
           page-uuid (get-in state [:page :uuid])]
       (do
         (storage/move-shape project-uuid page-uuid selected-uuid deltax deltay)
         (-> state
             (update-in [:shapes selected-uuid] dissoc :dragging-coors)
             (update-in [:shapes selected-uuid] dissoc :dragging))))
     state)))

(pubsub/register-transition
 :viewport-mouse-move
 (let [last-event (atom [0 0])]
   (fn [state _]
     (let [[x y] (:mouse-position state)
           [old-x old-y] @last-event
           selected-uuid (get-in state [:page :selected])]
       (reset! last-event [x y])
       (if (and selected-uuid
                (get-in state [:shapes selected-uuid :dragging]))
         (let [deltax (- x old-x)
               deltay (- y old-y)
               selected-uuid (get-in state [:page :selected])]
           (-> state
               (update-in [:shapes selected-uuid] shapes/move-delta deltax deltay)))
         state)))))

(pubsub/register-transition
 :move-layer-down
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         groups (:groups state)
         selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
         previous-group (last (take-while #(not= (nth % 0) (nth selected-group 0)) (sort-by #(:order (nth % 1)) (seq groups))))
         selected-group-order (:order (nth selected-group 1))
         previous-group-order (:order (nth previous-group 1))]
     (if (and selected-group previous-group)
       (do
         (storage/move-group-down selected-group)
         (-> state
             (assoc-in [:groups (nth selected-group 0) :order] previous-group-order)
             (assoc-in [:groups (nth previous-group 0) :order] selected-group-order)))
       state))))

(pubsub/register-transition
 :move-layer-up
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         groups (:groups state)
         selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
         next-group (last (take-while #(not= (nth % 0) (nth selected-group 0)) (reverse (sort-by #(:order (nth % 1)) (seq groups)))))
         selected-group-order (:order (nth selected-group 1))
         next-group-order (:order (nth next-group 1))]
     (if (and selected-group next-group)
       (do

         (storage/move-group-up selected-group)
         (-> state
             (assoc-in [:groups (nth selected-group 0) :order] next-group-order)
             (assoc-in [:groups (nth next-group 0) :order] selected-group-order)))
       state))))

(pubsub/register-transition
 :move-layer-to-bottom
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         groups (:groups state)
         selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
         min-order-group (apply min (map :order (vals groups)))]
     (-> state
         (assoc-in [:groups (first selected-group) :order] (dec min-order-group))))))

(pubsub/register-transition
 :move-layer-to-top
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         groups (:groups state)
         selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
         max-order-group (apply max (map :order (vals groups)))]
     (assoc-in state [:groups (first selected-group) :order] (inc max-order-group)))))

(pubsub/register-transition
 :zoom-in
 (fn [state data]
   (update-in state [:workspace :zoom] #(+ % 0.1))))

(pubsub/register-transition
 :zoom-out
 (fn [state data]
   (update-in state [:workspace :zoom] #(max 0.01 (- % 0.1)))))

(pubsub/register-transition
 :zoom-reset
 (fn [state data]
   (assoc-in state [:workspace :zoom] 1)))

(pubsub/register-transition
  :zoom-wheel
  (fn [state delta]
    (update-in state [:workspace :zoom] #(max 0.01 (+ % (* % 0.015 delta))))))

(pubsub/register-transition
 :viewport-scroll
 (fn [state data]
   (assoc state :scroll data)))

(pubsub/register-transition
 :viewport-mouse-move
 (fn [state data]
   (let [zoom (get-in state [:workspace :zoom])]
     (assoc state :mouse-position [(int (/ (first data) zoom)) (int (/ (second data) zoom))]))))
