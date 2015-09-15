(ns uxbox.data.projects)

(defn create-project
  [uuid name width height layout]
  (let [now (js/Date.)]
    {:project/name name
     :project/width width
     :project/height height
     :project/layout layout
     :project/uuid uuid
     :project/created now
     :project/last-updated now}))

(defn create-page
  [uuid project-uuid title width height]
  (let [now (js/Date.)]
    {:page/title title
     :page/width width
     :page/height height
     :page/project project-uuid
     :page/uuid uuid
     :page/created now
     :page/last-updated now}))

(defn create-shape
  [uuid page-uuid shape]
  (let [now (js/Date.)]
    {:shape/uuid uuid
     :shape/page page-uuid
     :shape/data shape
     :shape/locked? false
     :shape/visible? true
     :shape/created now}))
