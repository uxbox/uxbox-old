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

(pubsub/register-handler
 :create-project
 (fn [state project]
   (update state :projects conj project)))
