(ns uxbox.storage.generators
  (:require [uxbox.storage.views :refer [projects-view pages-view]]))

;; TODO
;; (defn undo-tree [events])

(defn projects-data [event]
  (let [event-data (:data event)]
    (case (:type event)
      :create-project (swap! projects-view (fn [current] (assoc current (:uuid event-data) (assoc event-data :pages 0 :comments 0))))
      :delete-project (swap! projects-view (fn [current] (dissoc current (:project-uuid event-data))))
      :create-page (swap! projects-view (fn [current] (update-in current [(:project-uuid event-data) :pages] inc)))
      :delete-page (swap! projects-view (fn [current] (update-in current [(:project-uuid event-data) :pages] dec)))
      :create-comment (swap! projects-view (fn [current] (update-in current [(:project-uuid event-data) :comments] inc)))
      :delete-comment (swap! projects-view (fn [current] (update-in current [(:project-uuid event-data) :comments] dec)))
      "default")))

(defn pages-data [event]
  (let [event-data (:data event)]
    (case (:type event)
      :create-page (swap! pages-view (fn [current] (assoc current (:uuid event-data) event-data)))
      :delete-page (swap! pages-view (fn [current] (dissoc current (:page-uuid event-data))))
      :delete-project (swap! pages-view (fn [current] (into {} (filter #(not= (:project-uuid event-data) (:project-uuid (second %))) current))))
      "default")))
