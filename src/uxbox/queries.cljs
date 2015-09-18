(ns uxbox.queries
  (:require
   [datascript :as d]
   [uxbox.streams :as s]))

;; reactive query

(defn- eids-changed?
  [q tx-report]
  (let [before (d/q q (:db-before tx-report))
        after (d/q q (:db-after tx-report))]
    (when (not= before after)
      after)))

(defn rquery
  [q conn]
  (let [k (gensym)]
    (s/from-binder (fn [sink]
                     ;; put first
                     (sink (d/q q @conn))
                     ;; put subsequent
                     (d/listen! conn
                                k
                                (fn [txr]
                                  (when-let [after (eids-changed? q txr)]
                                    (sink after))))
                     ;; unsub fn
                     (fn []
                       (d/unlisten! conn k))))))

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
        pulls
        (s/from-binder (fn [sink]
                         ;; put first
                         (sink (pull-one-or-many (d/q q @conn) p @conn))
                         ;; put subsequent
                         (d/listen! conn
                                    k
                                    (fn [txr]
                                      (let [after (d/q q (:db-after txr))]
                                        (sink (pull-one-or-many after p (:db-after txr))))))
                         ;; unsub fn
                         (fn []
                           (d/unlisten! conn k))))]
    (s/dedupe pulls)))

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
         pulls
         (s/from-binder (fn [sink]
                          ;; put first
                          (sink (pull-entity id p @conn))
                          ;; put subsequent
                          (d/listen! conn
                                     k
                                     (fn [txr]
                                       (when-let [e (entity-changed? id p txr)]
                                         (sink e))))
                          ;; unsub fn
                          (fn []
                            (d/unlisten! conn k))))]
     (s/dedupe pulls))))
