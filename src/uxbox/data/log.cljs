(ns uxbox.data.log
  (:require
   [uxbox.data.queries :as q]
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
                   :uxbox/change-shape-attr

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
  (d/transact! conn [[:db.fn/retractEntity (q/project-by-id @conn uuid)]]))

;; Page

(defmethod persist! :uxbox/create-page
  [_ page conn]
  (d/transact! conn [page]))

(defmethod persist! :uxbox/change-page-title
  [_ [page new-title] conn]
  (d/transact! conn [[:db/add
                      (q/page-by-id @conn (:page/uuid page))
                      :page/title
                      new-title]]))

(defmethod persist! :uxbox/delete-page
  [_ uuid conn]
  (d/transact! conn [[:db.fn/retractEntity (q/page-by-id @conn uuid)]]))

(defn record
  [key data]
  (persist! key data db/conn))
