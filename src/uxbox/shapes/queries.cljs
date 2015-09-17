(ns uxbox.shapes.queries
  (:require [datascript :as d]))

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
