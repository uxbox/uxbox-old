(ns uxbox.data.log
  (:require
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

(defn record
  [entry]
  (d/transact! db/conn [(second entry)]))
