(ns ^:figwheel-always uxbox.core
    (:require rum
              [datascript :as d]
              [uxbox.mouse :as mouse]
              [uxbox.data.db :as db]
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

(defn render!
  [$el location conn]
  (let [component (ui location)]
    (rum/mount component $el)))

(defn start!
  [location]
  (start-history!)
  (db/init-db! db/conn local-storage)
  (db/persist-to! db/conn local-storage)
  (render! $el location db/conn))

(start! nav/location)
