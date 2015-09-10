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
  (first
   (d/q
    '[:find [(count ?e)]
      :where
      [?e :project/uuid ?u]]
    db)))

(defn shape-by-id
  [sid db]
  (first
   (d/q
    `[:find [?e]
      :where [?e :shape/uuid ~sid]]
    db)))

(defn pull-shape-by-id
  [sid db]
  (d/pull db '[*] (shape-by-id sid db)))

(defn shapes-by-page-id
  [pid db]
  (d/q
   `[:find [?e ...]
     :where [?e :shape/page ?p]
            [?p :page/uuid ~pid]]
   db))

(defn pull-shapes-by-page-id
  [pid db]
  (d/pull-many db '[*] (shapes-by-page-id pid db)))

(defn shape-count-by-page-id
  [pid db]
  (count (shapes-by-page-id pid db)))
