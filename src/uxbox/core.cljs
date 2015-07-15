(ns ^:figwheel-always uxbox.core
    (:require [uxbox.db :as db]
              [uxbox.navigation :refer [start-history!]]
              [uxbox.keyboard :refer [start-keyboard!]]
              [uxbox.dashboard.views :refer [dashboard]]
              [uxbox.workspace.views :refer [workspace]]
              [uxbox.forms :refer [lightbox]]
              [uxbox.user.views :refer [login register recover-password]]
              [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defn ui [db]
  (let [[page params] (:location @db)]
    (case page
      :dashboard [:div
                  [dashboard db]
                  [lightbox db]]
      :login [login]
      :recover-password [recover-password]
      :register [register]
      :workspace [workspace db])))

(defn render!
  [app-state element]
  (reagent/render-component [ui app-state] element))

(def $el (.getElementById js/document "app"))

(defn start!
  [app-state]
  (start-history!)
  (start-keyboard!)
  (render! app-state $el))

(start! db/app-state)
