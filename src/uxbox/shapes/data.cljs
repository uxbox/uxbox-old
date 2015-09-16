(ns uxbox.shapes.data
  (:require
   [uxbox.projects.queries :as pq]
   [uxbox.shapes.queries :as q]
   [uxbox.shapes.protocols :as p]
   [uxbox.log.core :refer [to-datoms]]))

(defn create-shape
  [uuid page-uuid shape]
  (let [now (js/Date.)]
    {:shape/uuid uuid
     :shape/page page-uuid
     :shape/data shape
     :shape/locked? false
     :shape/visible? true
     :shape/created now}))

(defmethod to-datoms :uxbox/create-shape
  [{shape :event/payload} db]
  [(assoc shape
          :shape/page
          (pq/page-by-id (:shape/page shape) db))])

(defmethod to-datoms :uxbox/delete-shape
  [{uuid :event/payload} db]
  [[:db.fn/retractEntity (q/shape-by-id uuid db)]])

(defmethod to-datoms :uxbox/move-shape
  [{[uuid dx dy] :event/payload} db]
  (let [s (q/shape-by-id uuid db)]
    [[:db/add
      s
      :shape/data
      (p/move-delta (:shape/data (q/pull-shape-by-id uuid db)) dx dy)]]))

(defmethod to-datoms :uxbox/change-shape
  [{[uuid attr value] :event/payload} db]
  (let [s (q/shape-by-id uuid db)]
    [[:db/add
      s
      :shape/data
      (assoc (:shape/data (q/pull-shape-by-id uuid db))
             attr
             value)]]))

;; TODO: udpate last-updated
(defmethod to-datoms :uxbox/update-shapes
  [{nshapes :event/payload} db]
  (into [] (for [[uuid
                  data
                  :as shape] nshapes
                  :let [s (q/shape-by-id uuid db)]]
             [:db/add s :shape/data data])))

(defmethod to-datoms :uxbox/toggle-shape-visibility
  [{uuid :event/payload} db]
  (let [{visible? :shape/visible?
         s        :db/id} (q/pull-shape-by-id uuid db)]
    [[:db/add
      s
      :shape/visible?
      (not visible?)]]))

(defmethod to-datoms :uxbox/toggle-shape-lock
  [{uuid :event/payload} db]
  (let [{locked? :shape/locked?
         s       :db/id} (q/pull-shape-by-id uuid db)]
    [[:db/add
      s
      :shape/locked?
      (not locked?)]]))
