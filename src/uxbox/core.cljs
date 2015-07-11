(ns ^:figwheel-always uxbox.core
    (:require [uxbox.db :as db]
              [uxbox.pubsub :as pubsub :refer [start-pubsub!]]
              [uxbox.navigation :refer [start-history!]]
              [uxbox.dashboard.views :refer [dashboard]]
              [uxbox.workspace.views :refer [workspace]]
              [uxbox.user.views :refer [login]]
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
      :dashboard [dashboard db]
      :login [login]
      :workspace [workspace db]
      :default [:h3 "Not implemented"])))

(defn render!
  [app-state element]
  (reagent/render-component [ui app-state] element))

(def $el (.getElementById js/document "app"))

(defn start!
  [app-state]
  (start-pubsub!)
  (start-history!)
  (render! app-state $el))

(start! db/app-state)
