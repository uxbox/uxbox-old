(ns uxbox.data.db
  (:require
   [datascript :as d]
   [uxbox.data.schema :as sch]))

(defn create
  []
  (let [conn (d/create-conn sch/schema)]
    conn))

(defn restore!
  [conn storage]
  ;; todo: handle diverging schemas, per-user storage key?
  (when-let [old-db (get storage ::datoms)]
    (reset! conn old-db)))

(defn persist-to!
  [conn storage]
  (d/listen! conn
             ::persitence
             (fn [_]
                (assoc! storage ::datoms @conn))))

(defn init!
  [conn storage]
  (restore! conn storage)
  (persist-to! conn storage))
