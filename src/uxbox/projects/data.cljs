(ns uxbox.projects.data)

(defn create-project
  [name]
  (let [now (js/Date.)]
    {:name name
     :uuid (random-uuid)
     :last-update now
     :created now
     :pages []
     :comment-count 0}))

(defn create-page
  [project-uuid title]
  {:title title
   :uuid (random-uuid)
   :width 640
   :height 1080
   :project project-uuid
   :shapes {}
   :groups {}})
