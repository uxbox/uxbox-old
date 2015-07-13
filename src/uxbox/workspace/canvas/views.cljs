(ns uxbox.workspace.canvas.views)

;; Transform from the "shape" internal datastructure to SVG tags
(defmulti shape->svg :shape)

(defmethod shape->svg :rectangle [{:keys [x y width height rx ry fill stroke]}]
  [:rect {:x x :y y :width width :height height :fill fill :rx rx :ry ry}])

(defmethod shape->svg :line [{:keys [x1 y1 x2 y2 color width]}]
  [:line {:x1 x1 :y1 y1 :x2 x2 :y2 y2 :style #js {:stroke color :strokeWidth width}}])

(defn canvas [db]
  (let [viewport-height 3000
        viewport-width 3000

        page-width (get-in @db [:page :width])
        page-height (get-in @db [:page :height])

        ;; Get a group of ids and retrieves the list of shapes
        ids->shapes (fn [shape-ids]
                    (map #(get-in @db [:page :shapes %]) shape-ids))

        ;; Retrieve the <g> element grouped if applied
        group-svg (fn [shapes]
                    (if (= (count shapes) 1)
                      (->> shapes first shape->svg)
                      (apply vector :g
                             (->> shapes
                                  (map shape->svg)))))

        ;; Retrieve the list of shapes grouped if applies
        shapes-svg (->> @db
                        :page :groups vals
                        (sort-by :order)
                        (filter :visible)
                        (map #(update-in % [:shapes] ids->shapes))
                        (map :shapes)
                        (map group-svg))]

    [:svg {:width viewport-height :height viewport-width}
     [:svg  {:x 50 :y 50 :width page-width :height page-height};; Document
      [:rect {:x 0 :y 0 :width "100%" :height "100%" :fill "white"}]
      (apply vector :svg shapes-svg)]
     ]))
