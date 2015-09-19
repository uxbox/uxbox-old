(ns uxbox.queries
  (:require
   [datascript :as d]
   [uxbox.streams :as s]))

(defn- query
  [q db]
  (d/q q db))

;; reactive query

(defn- eids-changed?
  [q tx-report]
  (let [before (query q (:db-before tx-report))
        after (query q (:db-after tx-report))]
    (when (not= before after)
      after)))

(defn rquery
  [q conn]
  (let [k (gensym)
        a (atom (query q @conn))
        sink #(reset! a %)]
    (d/listen! conn
               k
               (fn [txr]
                 (when-let [after (eids-changed? q txr)]
                   (sink after))))
    a))

;; reactive pull

(defn- pull-one-or-many
  [eids p db]
  (cond
    (sequential? eids)
    (d/pull-many db p eids)

    (not (nil? eids))
    (d/pull db p eids)))

(defn rpull
  [q p conn]
  (let [k (gensym)
        a (atom (pull-one-or-many (query q @conn) p @conn))
        sink #(reset! a %)]
    (d/listen! conn
               k
               (fn [txr]
                 (let [after (query q (:db-after txr))]
                   (sink (pull-one-or-many after p (:db-after txr))))))
    a))

;; reactive entity

(defn- pull-entity
  [id p db]
  (d/pull db p id))

(defn- entity-changed?
  [id p tx-report]
  (let [before (pull-entity id p (:db-before tx-report))
        after (pull-entity id p (:db-after tx-report))]
    (when (not= before after)
      after)))

(defn rentity
  ([id conn]
   (rentity id '[*] conn))
  ([id p conn]
   (let [k (gensym)
         a (atom (pull-entity id p @conn))
         sink #(reset! a %)]
     (d/listen! conn
                k
                (fn [txr]
                  (when-let [e (entity-changed? id p txr)]
                    (sink e))))
     a)))
