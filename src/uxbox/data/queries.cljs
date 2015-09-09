(ns uxbox.data.queries
  (:require [datascript :as d]))

(defn pipe-to-atom
  [q conn key]
  (let [a (atom (q @conn))]
    (d/listen! conn
               key
               (fn [tx-report]
                 (let [new (q (:db-after tx-report))]
                   (when-not (= new @a)
                     (reset! a new)))))
    ;; TODO: cleanup
    a))

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

(defn projects
  [db]
  (let [eids (d/q projects-query db)]
    (d/pull-many db '[*] eids)))

(defn project-count
  [db]
  (first
   (d/q
    '[:find [(count ?e)]
      :where
      [?e :project/uuid ?u]]
    db)))
