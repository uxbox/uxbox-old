(ns ^:figwheel-always uxbox.core
    (:require rum
              [uxbox.db :as db]
              [uxbox.navigation :refer [start-history! location]]
              [uxbox.keyboard :refer [start-keyboard!]]
              [uxbox.storage.core :refer [start-storage!]]
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
  [db location]
  (let [[page params] @location]
    (case page
      ;; User
      :login (login)
      :register (register)
      :recover-password (recover-password)
      ;; Home
      :dashboard [:div
                  (dashboard db)
                  (lightbox db)]
      ;; Workspace
      :workspace (workspace db))))

(defn render!
  [app-state element]
  (rum/mount (ui app-state location) element))

(def $el (.getElementById js/document "app"))

(defn start!
  [app-state]
  (start-storage! local-storage)
  (start-history!)
  (start-keyboard!)
  (render! app-state $el))

(start! db/app-state)
