(ns uxbox.projects.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.storage :as storage]))

(defn create-project
  [{:keys [name width height layout]}]
  (let [now (js/Date.)]
    (pubsub/publish! [:create-project {:name name
                                       :width width
                                       :height height
                                       :layout layout
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
   (update state :projects-list #(dissoc % uuid))))

(pubsub/register-transition
 :create-project
 (fn [state project]
   (update state :projects-list assoc (:uuid project) project)))

(pubsub/register-effect
 :create-project
 (fn [state project]
   (storage/create-project project)))

(pubsub/register-effect
 :delete-project
 (fn [state uuid]
   (storage/delete-project uuid)))
