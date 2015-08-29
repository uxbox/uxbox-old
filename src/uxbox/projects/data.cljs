(ns uxbox.projects.data)

(defn create-project
  [name width height layout]
  {:name name
   :width width
   :height height
   :layout layout
   :uuid (random-uuid)})

(defn create-page
  [project-uuid title width height]
  {:title title
   :root []
   :uuid (random-uuid)
   :width width
   :height height
   :project-uuid project-uuid})
