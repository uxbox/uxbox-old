(ns uxbox.projects.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn create-project
  [{:keys [name width height layout]}]
  (println "C" "N" name "W" width "H" height "l" layout)
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
   (update state :projects #(dissoc % uuid))))

(pubsub/register-transition
 :create-project
 (fn [state project]
   (update state :projects assoc (:uuid project) project)))

(defn select-project
  [uuid]
  (println "kk2")
  (pubsub/publish! [:select-project uuid]))

(pubsub/register-transition
 :select-project
 (fn [state project-uuid]
   (println "KK" project-uuid (get-in state [:projects]))
   (let [project-uuid (uuid project-uuid)
     project (get-in state [:projects project-uuid])]
     (println project)
     (assoc-in state [:workspace :current-project] project)
   )))
