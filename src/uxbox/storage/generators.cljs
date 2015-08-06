(ns uxbox.storage.generators
  (:require [uxbox.storage.views :refer [projects-view pages-view]]))

;; TODO
;; (defn undo-tree [events])

(defn projects-data [event]
  (let [event-data (:data event)]
    (.log js/console (clj->js event))
    (case (:type event)
      :create-project (swap! projects-view (fn [current] (assoc current (:uuid event-data) (assoc event-data :pages 1 :comments 0))))
      :create-page (swap! projects-view (fn [current] (update-in current [(:uuid event-data) :pages] inc)))
      :delete-page (swap! projects-view (fn [current] (update-in current [(:uuid event-data) :pages] dec)))
      :create-comment (swap! projects-view (fn [current] (update-in current [(:uuid event-data) :comments] inc)))
      :delete-comment (swap! projects-view (fn [current] (update-in current [(:uuid event-data) :comments] dec)))
      :delete-project (swap! projects-view (fn [current] (dissoc current (:uuid event-data))))
      "default")))

(defn pages-data [event]
  (let [event-data (:data event)]
    (case (:type event)
      :create-page (swap! pages-view (fn [current] (assoc current (:uuid event-data) {:project-uuid (:project-uuid event-data) :title (:title event-data)})))
      :delete-page (swap! pages-view (fn [current] (dissoc current (:uuid event-data))))
      "default")))
