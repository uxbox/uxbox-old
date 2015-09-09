(ns ^:figwheel-always uxbox.core
    (:require rum
              [datascript :as d]
              [uxbox.db :as db]
              [uxbox.data.db :refer [conn]]
              [uxbox.data.queries :as q]
              [uxbox.data.projects :as proj]
              [uxbox.navigation :as nav :refer [start-history!]]
              [uxbox.dashboard.views :refer [dashboard]]
              [uxbox.workspace.views :refer [workspace]]
              [uxbox.forms :refer [lightbox]]
              [uxbox.user.views :refer [login
                                        register
                                        recover-password]]
              [uxbox.icons-sets.core]
              [hodgepodge.core :refer [local-storage]]))

(enable-console-print!)

(rum/defc ui < rum/cursored-watch
  [app-state location]
  (let [[page params] @location]
    (case page
      ;; User
      :login (login)
      :register (register)
      :recover-password (recover-password)
      ;; Home
      :dashboard [:div
                  (dashboard app-state)
                  (lightbox app-state)]
      ;; Workspace
      :workspace (workspace conn app-state params))))

(def $el (.getElementById js/document "app"))

(defn start!
  [app-state location]
  (start-history!)
  (rum/mount (ui app-state location) $el))

(start! db/app-state nav/location)
