(ns uxbox.workspace.canvas.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.geometry :as geo]
            [uxbox.storage :as storage]
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
   (let [selected-uuid
         (->> state
              :page :groups vals
              (sort-by #(- (:order %)))
              (filter :visible)
              (mapcat :shapes)
              (filter #(not (nil? (get-in state [:page :shapes %]))))
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
         shape-val (shapes/new-path-shape rect-x rect-y rect-width rect-height (-> symbol :svg second :d) 48 48)
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
          cx (:cx drawing-val)
          cy (:cy drawing-val)
          r (geo/distance x y cx cy)
          ;;Avoid drawing circles with negatives coordinates
          dx (- (geo/distance cx cy cx 0) r)
          dy (- (geo/distance cx cy 0 cy) r)
          r (if (or (< dx 0) (< dy 0)) (- r (Math/abs (min dx dy))) r)
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

(pubsub/register-transition
 :viewport-mouse-drag
 (fn [state [state coords]]
   (if (get-in state [:page :selected])
     state
     state)))

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
            (update-in [:page :groups] remove-element selected-uuid)
            (update-in [:page :shapes] dissoc selected-uuid)
            (update-in [:page] dissoc :selected))
         state))))

(pubsub/register-transition
 :viewport-mouse-down
 (fn [state]
   (if-let [selected-uuid (get-in state [:page :selected])]
     (-> state
         (update-in [:page :shapes selected-uuid] assoc :dragging true))
     state)))

(pubsub/register-transition
 :viewport-mouse-up
 (fn [state]
   (if-let [selected-uuid (get-in state [:page :selected])]
     (-> state
         (update-in [:page :shapes selected-uuid] dissoc :dragging))
     state)))

(pubsub/register-transition
 :viewport-mouse-move
 (let [last-event (atom [0 0])]
   (fn [state [x y]]
     (let [[old-x old-y] @last-event
           selected-uuid (get-in state [:page :selected])]
       (reset! last-event [x y])
       (if (and selected-uuid (get-in state [:page :shapes selected-uuid :dragging]))
         (let [deltax (- x old-x)
               deltay (- y old-y)
               shape-x (get-in state [:page :shapes selected-uuid :x])
               shape-y (get-in state [:page :shapes selected-uuid :y])
               selected-uuid (get-in state [:page :selected])
               project-uuid (get-in state [:project :uuid])
               page-uuid (get-in state [:page :uuid])]
           (storage/move-shape project-uuid page-uuid selected-uuid deltax deltay)
           (-> state
               (update-in [:page :shapes selected-uuid] shapes/move-delta deltax deltay)))
         state)))))
