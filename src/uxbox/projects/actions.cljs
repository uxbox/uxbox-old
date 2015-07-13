(ns uxbox.projects.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn create-project
  [name]
  (let [now (js/Date.)]
    (pubsub/publish! [:create-project {:name name
                                       :uuid (random-uuid)
                                       :last-update now
                                       :created now
                                       :page-count 0
                                       :comment-count 0}])))

(defn delete-project
  [uuid]
  (pubsub/publish! [:delete-project uuid]))

(pubsub/register-transition
 :delete-project
 (fn [state uuid]
   (update state :projects #(dissoc % uuid))))

(pubsub/register-transition
 :create-project
 (fn [state project]
   (update state :projects assoc (:uuid project) project)))
