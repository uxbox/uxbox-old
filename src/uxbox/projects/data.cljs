(ns uxbox.projects.data
  (:require
   [uxbox.log.core :refer [to-datoms materialize]]
   [uxbox.shapes.protocols :as p]
   [uxbox.projects.queries :as q]))

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

(defmethod materialize :uxbox/create-page
  [{page :event/payload :as ev} db]
  (let [project (q/pull-project-by-id (:page/project page) db)]
    (assoc-in ev [:event/payload :page/project] project)))
