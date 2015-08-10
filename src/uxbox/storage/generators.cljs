(ns uxbox.storage.generators
  (:require [uxbox.storage.atoms :refer [projects-view pages-view groups-view shapes-view]]
            [uxbox.shapes.core :as shapes]))

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
      :change-page-title (swap! pages-view (fn [current] (assoc-in current [(:page-uuid event-data) :title] (:new-title event-data))))
      :delete-project (swap! pages-view (fn [current] (into {} (filter #(not= (:project-uuid event-data) (:project-uuid (second %))) current))))
      "default")))

(defn groups-data [event]
  (let [event-data (:data event)]
    (case (:type event)
      :create-group (swap! groups-view (fn [current] (assoc current (:uuid event-data) event-data)))
      :toggle-group-visibility (swap! groups-view (fn [current] (update-in current [(:group-uuid event-data) :visible] not)))
      :toggle-group-lock (swap! groups-view (fn [current] (update-in current [(:group-uuid event-data) :locked] not)))
      :delete-group (swap! groups-view (fn [current] (dissoc current (:group-uuid event-data))))
      :delete-page (swap! groups-view (fn [current] (into {} (filter #(not= (:page-uuid event-data) (:page-uuid (second %))) current))))
      :delete-project (swap! groups-view (fn [current] (into {} (filter #(not= (:project-uuid event-data) (:project-uuid (second %))) current))))
      "default")))

(defn shapes-data [event]
  (let [event-data (:data event)]
    (case (:type event)
      :create-shape (swap! shapes-view (fn [current] (assoc current (:uuid event-data) event-data)))
      :delete-shape (swap! shapes-view (fn [current] (dissoc current (:shape-uuid event-data))))
      :delete-group (swap! shapes-view (fn [current] (into {} (filter #(not= (:group-uuid event-data) (:group-uuid (second %))) current))))
      :delete-page (swap! shapes-view (fn [current] (into {} (filter #(not= (:page-uuid event-data) (:page-uuid (second %))) current))))
      :delete-project (swap! shapes-view (fn [current] (into {} (filter #(not= (:project-uuid event-data) (:project-uuid (second %))) current))))
      :move-shape (swap! shapes-view (fn [current] (update-in current [(:shape-uuid event-data)] shapes/move-delta (:delta-x event-data) (:delta-y event-data))))
      :change-shape-attr (swap! shapes-view (fn [current] (assoc-in current [(:shape-uuid event-data) (:attr event-data)] (:value event-data))))
      "default")))
