(ns uxbox.ui
  (:require rum
            [uxbox.ui.shapes]
            [uxbox.ui.users :as u]
            [uxbox.ui.dashboard :as d]
            [uxbox.ui.workspace :as w]))

(rum/defc app < rum/cursored-watch
  [conn location]
  (let [[page params] @location]
    (case page
      ;; User
      :login (u/login)
      :register (u/register)
      :recover-password (u/recover-password)
      ;; Home
      :dashboard (d/dashboard conn)
      ;; Workspace
      :project (w/workspace conn params)
      :page    (w/workspace conn params))))

(defn render!
  [$el location conn]
  (let [component (app conn location)]
    (rum/mount component $el)))
