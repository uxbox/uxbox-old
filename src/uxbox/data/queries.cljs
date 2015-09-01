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

(defn project-count
  [db]
  (ffirst
   (d/q
    '[:find (count ?e)
      :where
      [?e :project/uuid ?u]]
    db)))
