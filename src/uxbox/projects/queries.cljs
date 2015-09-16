(ns uxbox.projects.queries
  (:require [datascript :as d]))

(def projects-query '[:find [?e ...]
                      :where
                      [?e :project/uuid ?u]])

(defn project-by-id
  [uuid db]
  (first
   (d/q
    `[:find [?e]
      :where [?e :project/uuid ~uuid]]
    db)))

(defn pull-project-by-id
  [uuid db]
  (d/pull db '[*] (project-by-id uuid db)))

(defn first-page-id-by-project-id
  [uuid db]
  (first
   (d/q
    `[:find [?u]
      :where [?e :page/project ?p]
             [?e :page/uuid ?u]
             [?p :project/uuid ~uuid]]
    db)))

(defn page-by-id
  [uuid db]
  (first
   (d/q
    `[:find [?e]
      :where [?e :page/uuid ~uuid]]
    db)))

(defn pull-page-by-id
  [uuid db]
  (d/pull db '[*] (page-by-id uuid db)))

(defn pages-by-project-id
  [puuid db]
  (let [pid (project-by-id puuid db)]
    (d/q
     `[:find [?e ...]
       :where [?e :page/project ~pid]]
     db)))

(defn pull-pages-by-project-id
  [puuid db]
  (d/pull-many db '[*] (pages-by-project-id puuid db)))

(defn page-count-by-project-id
  [puuid db]
  (count (pages-by-project-id puuid db)))

(defn pull-projects
  [db]
  (let [eids (d/q projects-query db)]
    (d/pull-many db '[*] eids)))

(defn project-count
  [db]
  (count (d/q projects-query db)))
