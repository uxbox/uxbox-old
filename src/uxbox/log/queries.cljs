(ns uxbox.log.queries
  (:require
   [uxbox.log.core :as log]
   [datascript :as d]))

(def events-query
  '[:find [?e ...]
    :where [?e :event/type ?t]])

(defn all-events
  [db]
  (map :e (d/datoms db :avet :event/timestamp)))

(defn events-by-type
  [t db]
  (d/q `[:find [?e ...]
         :where
         [?e :event/type ~t]]
       db))

(defn create-project-events
  [db]
  (events-by-type :uxbox/create-project db))

(defn create-page-events
  [db]
  (events-by-type :uxbox/create-page db))

(defn events
  [db]
  (d/pull-many db '[*] (all-events db)))
