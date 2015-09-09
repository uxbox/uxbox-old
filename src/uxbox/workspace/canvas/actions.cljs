(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
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
   (let [selected-uuid ;; find the uuid of selected shape by intersection
         (->> state
              :page :root
              (filter #(not (nil? (get-in state [:shapes %]))))
              (filter #(get-in state [:shapes % :visible]))
              (filter #(shapes/intersect (get-in state [:shapes %]) x y))
              first)]
     (assoc-in state [:page :selected] selected-uuid))))

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
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (-> state
      (assoc-in [:shapes shape-uuid] shape-val)
      (update-in [:page :root] #(conj % shape-uuid)))))

(pubsub/register-effect
 :insert-shape
 (fn [state [shape-uuid shape-val]]
   (let [project-uuid (get-in state [:project :uuid])
         page-uuid (get-in state [:page :uuid])]
     #_(storage/create-shape project-uuid page-uuid shape-uuid shape-val))))

(pubsub/register-event
  :canvas-mouse-click
  (fn [state coords]
    (if (get-in state [:workspace :selected-tool])
      (drawing-shape coords)
      (select-shape coords))))

(pubsub/register-transition
  :delete-key-pressed
  (fn [state]
    (let [selected-uuid (get-in state [:page :selected])
          project-uuid (get-in state [:project :uuid])
          page-uuid (get-in state [:page :uuid])]

      (when selected-uuid
         #_(storage/remove-shape project-uuid page-uuid selected-uuid))

      (if selected-uuid
         (-> state
            (update-in [:page :root] #(filter (fn [uuid] (not= selected-uuid uuid)) %))
            (update-in [:shapes] dissoc selected-uuid)
            (update-in [:page] dissoc :selected))
         state))))

(pubsub/register-transition
 :canvas-mouse-down
 (fn [state]
   (if-let [selected-uuid (get-in state [:page :selected])]
     (let [coors {:x (get-in state [:shapes selected-uuid :x]) :y (get-in state [:shapes selected-uuid :y])}]
       (-> state
           (update-in [:shapes selected-uuid] assoc :dragging-coors coors)
           (update-in [:shapes selected-uuid] assoc :dragging true)))
     state)))

(pubsub/register-transition
 :canvas-mouse-up
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
         #_(storage/move-shape project-uuid page-uuid selected-uuid deltax deltay)
         (-> state
             (update-in [:shapes selected-uuid] dissoc :dragging-coors)
             (update-in [:shapes selected-uuid] dissoc :dragging))))
     state)))

(pubsub/register-transition
 :canvas-mouse-move
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
         initial-list (get-in state [:page :root])
         begin (take-while #(not= selected-uuid %) initial-list)
         end (reverse (take-while #(not= selected-uuid %) (reverse initial-list)))]
     (if (> (count end) 0)
       (-> state
           (assoc-in [:page :root] (concat begin [(first end) selected-uuid] (rest end))))
       state))))

(pubsub/register-transition
 :move-layer-up
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         initial-list (get-in state [:page :root])
         begin (take-while #(not= selected-uuid %) initial-list)
         end (reverse (take-while #(not= selected-uuid %) (reverse initial-list)))]
     (if (> (count begin) 0)
       (-> state
           (assoc-in [:page :root] (concat (butlast begin) [selected-uuid (last begin)] end)))
       state))))

(pubsub/register-transition
 :move-layer-to-bottom
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         initial-list (get-in state [:page :root])
         final-list (conj (filter #(not= selected-uuid %) initial-list) selected-uuid)]
     (-> state
         (assoc-in [:page :root] final-list)))))

(pubsub/register-transition
 :move-layer-to-top
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         initial-list (get-in state [:page :root])
         final-list (reverse (conj (reverse (filter #(not= selected-uuid %) initial-list)) selected-uuid))]
     (-> state
         (assoc-in [:page :root] final-list)))))

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
 :canvas-mouse-move
 (fn [state data]
   (let [zoom (get-in state [:workspace :zoom])]
     (assoc state :mouse-position [(int (/ (first data) zoom)) (int (/ (second data) zoom))]))))
