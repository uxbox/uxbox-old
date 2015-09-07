(ns uxbox.data.log
  (:require
   [uxbox.data.queries :as q]
   [uxbox.data.db :as db]
   [datascript :as d]))

(def event-types #{:uxbox/create-project
                   :uxbox/delete-project
                   ;; TODO
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

(defmethod persist! :uxbox/create-page
  [_ page conn]
  (d/transact! conn [page]))

(defmethod persist! :uxbox/delete-project
  [_ uuid conn]
  (d/transact! conn [[:db.fn/retractEntity (q/project-by-id @conn uuid)]]))

(defn record
  [key data]
  (persist! key data db/conn))
