(ns uxbox.data.log
  (:require
   [uxbox.data.queries :as q]
   [uxbox.shapes.protocols :as p]
   [uxbox.data.db :as db]
   [datascript :as d]))

(def event-types #{:uxbox/create-project
                   :uxbox/delete-project
                   :uxbox/create-page
                   :uxbox/change-page-title
                   :uxbox/delete-page
                   :uxbox/create-shape
                   :uxbox/delete-shape
                   :uxbox/move-shape
                   :uxbox/change-shape
                   :uxbox/toggle-shape-visibility
                   :uxbox/toggle-shape-lock
                   :uxbox/move-shape-up
                   :uxbox/move-shape-down})

(defmulti persist! (fn [key data conn] key))

;; Project

(defmethod persist! :uxbox/create-project
  [_ project conn]
  (d/transact! conn [project]))

(defmethod persist! :uxbox/delete-project
  [_ uuid conn]
  (d/transact! conn [[:db.fn/retractEntity (q/project-by-id uuid @conn)]]))

;; Page

(defmethod persist! :uxbox/create-page
  [_ page conn]
  (let [relpage (assoc page
                       :page/project
                       (q/project-by-id (:page/project page) @conn))]
    (d/transact! conn [relpage])))

(defmethod persist! :uxbox/change-page-title
  [_ [page new-title] conn]
  (d/transact! conn [[:db/add
                      (q/page-by-id (:page/uuid page) @conn)
                      :page/title
                      new-title]]))

(defmethod persist! :uxbox/delete-page
  [_ uuid conn]
  (d/transact! conn [[:db.fn/retractEntity (q/page-by-id uuid @conn)]]))

;; Shape

(defmethod persist! :uxbox/create-shape
  [_ shape conn]
  (let [relshape (assoc shape
                        :shape/page
                        (q/page-by-id (:shape/page shape) @conn))]
    (d/transact! conn [relshape])))

(defmethod persist! :uxbox/delete-shape
  [_ uuid conn]
  (d/transact! conn [[:db.fn/retractEntity (q/shape-by-id uuid @conn)]]))

;; TODO: call fn transactionally with db.fn/call?
(defmethod persist! :uxbox/move-shape
  [_ [uuid dx dy] conn]
  (let [s (q/shape-by-id uuid @conn)]
    (d/transact! conn [[:db/add
                        s
                        :shape/data
                        (p/move-delta (:shape/data (q/pull-shape-by-id uuid @conn))
                                      dx
                                      dy)]])))

(defmethod persist! :uxbox/change-shape
  [_ [uuid attr value] conn]
  (let [s (q/shape-by-id uuid @conn)]
    (d/transact! conn [[:db/add
                        s
                        :shape/data
                        (assoc (:shape/data (q/pull-shape-by-id uuid @conn))
                               attr
                               value)]])))
;; :uxbox/toggle-shape-visibility
;; :uxbox/toggle-shape-lock

;; :uxbox/move-shape-up
;; :uxbox/move-shape-down

(defn record
  [key data]
  (persist! key data db/conn))
