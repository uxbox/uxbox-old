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
  [project name]
  {:name name
   :uuid (random-uuid)
   :width 640
   :height 1080
   :project (:uuid project)
   :shapes {}
   :groups {}})
