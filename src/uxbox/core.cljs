(ns ^:figwheel-always uxbox.core
    (:require rum
              [datascript :as d]
              [uxbox.streams :as s]
              [uxbox.mouse :as mouse]
              [uxbox.data.db :as db]
              [uxbox.data.queries :as q]
              [uxbox.data.projects :as proj]
              [uxbox.navigation :as nav :refer [start-history!]]
              [uxbox.dashboard.views :refer [dashboard]]
              [uxbox.workspace.views :refer [workspace]]
              [uxbox.user.views :refer [login
                                        register
                                        recover-password]]
              [uxbox.icons-sets.core]
              [hodgepodge.core :refer [local-storage]]))

(enable-console-print!)

(rum/defc ui < rum/cursored-watch
  [location]
  (let [[page params] @location]
    (case page
      ;; User
      :login (login)
      :register (register)
      :recover-password (recover-password)
      ;; Home
      :dashboard (dashboard db/conn)
      ;; Workspace
      :workspace (workspace db/conn params))))

(def $el (.getElementById js/document "app"))

(defn start!
  [location]
  (start-history!)
  (db/init-db! db/conn local-storage)
  (db/persist-to! db/conn local-storage)
  (rum/mount (ui location) $el))


#_(s/log :client mouse/client-position)

#_(s/pr-log :delta mouse/delta)

(start! nav/location)
