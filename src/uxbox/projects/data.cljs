(ns uxbox.projects.data)

(defn create-project
  [name width height layout]
  (let [now (js/Date.)]
    {:name name
     :width width
     :height height
     :layout layout
     :uuid (random-uuid)
     :last-update now
     :created now
     :pages []
     :comment-count 0}))

(defn create-page
  [project-uuid title width height]
  {:title title
   :uuid (random-uuid)
   :width width
   :height height
   :project project-uuid
   :shapes {}
   :groups {}})
