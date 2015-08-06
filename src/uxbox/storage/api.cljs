(ns uxbox.storage.api
  (:require [uxbox.storage.views :refer [projects-view pages-view activity-view]]
            [uxbox.storage.core :refer [insert-event]]
            [uxbox.shapes.core :refer [move-delta]]))

(defn get-activity
      [username]
      (reverse (get @activity-view username)))

(defn get-projects
    [username]
    (.log js/console (clj->js @projects-view))
    @projects-view)

(defn get-project [uuid]
    (get @projects-view uuid))

(defn get-pages
    [project-uuid]
    (into {} (filter #(= (:project-uuid %)) @pages-view)))

(defn get-page
    [project-uuid page-uuid]
    (get @pages-view [page-uuid]))

(defn create-project [project]
    (insert-event {:type :create-project :data project}))

(defn create-page
    [page]
    (insert-event {:type :create-page :data page}))

(defn change-page-title
    [project-uuid page title]
    (insert-event {:type :change-page-title :data {:project-uuid project-uuid :page-uuid (:uuid page) :old-title (:page title) :new-title title}}))

(defn delete-page
    [project-uuid page]
    (insert-event {:type :delete-page :data {:project-uuid project-uuid :page-uuid (:uuid page)}}))

(defn delete-project
    [uuid]
    (insert-event {:type :delete-project :data {:project-uuid uuid}}))

(defn create-shape
    [project-uuid page-uuid shape-uuid shape]
    (let [shape-data (-> shape (assoc :project-uuid project-uuid) (assoc :page-uuid page-uuid))]
      (insert-event {:type :create-shape :data shape-data})))

(defn remove-shape
    [project-uuid page-uuid shape-uuid]
    (insert-event {:type :delete-shape :data {:project-uuid project-uuid :page-uuid page-uuid :shape-uuid shape-uuid}}))

(defn move-shape
    [project-uuid page-uuid shape-uuid deltax deltay]
    (insert-event {:type :move-shape :data {:project-uuid project-uuid :page-uuid page-uuid :shape-uuid shape-uuid :delta (move-delta deltax deltay)}}))
