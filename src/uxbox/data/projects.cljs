(ns uxbox.data.projects)

(defn create-project
  [uuid name width height layout]
  {:project/name name
   :project/width width
   :project/height height
   :project/layout layout
   :project/uuid uuid})

(defn create-page
  [uuid project-uuid title width height]
  {:page/title title
   :page/width width
   :page/height height
   :page/project project-uuid
   :page/uuid uuid})
