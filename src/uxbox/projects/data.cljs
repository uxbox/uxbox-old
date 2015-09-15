(ns uxbox.projects.data
  (:require
   [uxbox.log.core :refer [to-datoms materialize]]
   [uxbox.shapes.protocols :as p]
   [uxbox.data.queries :as q]))

;; creation

(defn create-project
  [uuid name width height layout]
  (let [now (js/Date.)]
    {:project/name name
     :project/width width
     :project/height height
     :project/layout layout
     :project/uuid uuid
     :project/created now
     :project/last-updated now}))

(defn create-page
  [uuid project-uuid title width height]
  (let [now (js/Date.)]
    {:page/title title
     :page/width width
     :page/height height
     :page/project project-uuid
     :page/uuid uuid
     :page/created now
     :page/last-updated now}))

(defn create-shape
  [uuid page-uuid shape]
  (let [now (js/Date.)]
    {:shape/uuid uuid
     :shape/page page-uuid
     :shape/data shape
     :shape/locked? false
     :shape/visible? true
     :shape/created now}))

;; persistence

(defmethod to-datoms :uxbox/create-project
  [{project :event/payload} _]
  [project])

(defmethod to-datoms :uxbox/delete-project
  [{uuid :event/payload} db]
  [[:db.fn/retractEntity (q/project-by-id uuid db)]])

(defmethod to-datoms :uxbox/create-page
  [{page :event/payload} db]
  [(assoc page
           :page/project
           (q/project-by-id (:page/project page) db))])

(defmethod to-datoms :uxbox/delete-page
  [{uuid :event/payload} db]
  [[:db.fn/retractEntity (q/page-by-id uuid db)]])

(defmethod to-datoms :uxbox/change-page-title
  [{[page new-title] :event/payload} db]
  [[:db/add
    (q/page-by-id (:page/uuid page) db)
    :page/title
    new-title]])

(defmethod to-datoms :uxbox/create-shape
  [{shape :event/payload} db]
  [(assoc shape
          :shape/page
          (q/page-by-id (:shape/page shape) db))])

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

;; materialization

(defmethod materialize :uxbox/create-page
  [{page :event/payload :as ev} db]
  (let [project (q/pull-project-by-id (:page/project page) db)]
    (assoc-in ev [:event/payload :page/project] project)))
