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

(def projects-query '[:find ?e
                      :where
                      [?e :project/uuid ?u]])

(defn project-by-id
  [db uuid]
  (ffirst
   (d/q
    `[:find ?e
      :where [?e :project/uuid ~uuid]]
    db)))

(defn page-by-id
  [db uuid]
  (ffirst
   (d/q
    `[:find ?e
      :where [?e :page/uuid ~uuid]]
    db)))

(defn projects
  [db]
  (let [raw-eids (d/q projects-query db)
        eids (flatten (into [] raw-eids))]
    (d/pull-many db '[*] eids)))

(defn project-count
  [db]
  (ffirst
   (d/q
    '[:find (count ?e)
      :where
      [?e :project/uuid ?u]]
    db)))
