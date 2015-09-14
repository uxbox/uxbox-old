(ns ^:figwheel-always uxbox.core
    (:require rum
              [datascript :as d]
              [uxbox.streams :as s]
              [uxbox.mouse :as mouse]
              [uxbox.data.db :refer [conn]]
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
      :dashboard (dashboard conn)
      ;; Workspace
      :workspace (workspace conn params))))

(def $el (.getElementById js/document "app"))

(defn start!
  [location]
  (start-history!)
  (rum/mount (ui location) $el))


#_(s/log :client mouse/client-position)

#_(s/pr-log :delta mouse/delta)

(start! nav/location)
