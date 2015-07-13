(ns uxbox.projects.actions
  (:require
   [uxbox.projects.data :as d]
   [uxbox.pubsub :as pubsub]))

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
                                       :pages []
                                       :comment-count 0}])))

(defn create-page
  [project name]
  (pubsub/publish! [:create-page (d/create-page project name)]))

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

(pubsub/register-transition
 :create-page
 (fn [state page]
   (let [project (:project page)]
     (update-in state [:projects project :pages] conj page))))
