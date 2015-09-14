(ns uxbox.data.db
  (:require
   [datascript :as d]
   [uxbox.data.schema :as sch]))


(def conn (d/create-conn sch/schema))

(defn init-db!
  [conn storage]
  (when-let [old-db (get storage ::datoms)]
    (reset! conn old-db)))

(defn persist-to!
  [conn storage]
  (d/listen! conn
             ::persitence
             (fn [_]
               (assoc! storage ::datoms @conn))))
