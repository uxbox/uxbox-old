(ns uxbox.navigation
  (:require [uxbox.db :as db]
            [uxbox.pubsub :refer [publish! register-transition]]
            [secretary.core :as s :refer-macros [defroute]]
            [goog.events :as events])
  (:import [goog.history Html5History]
           goog.history.EventType))

;; Routes

(defn- set-location!
  [location]
  (publish! [:location location]))

(defroute login-route "/" []
  (set-location! [:login]))

(defroute register-route "/register" []
  (set-location! [:register]))

(defroute recover-password-route "/recover-password" []
  (set-location! [:recover-password]))

(defroute dashboard-route "/dashboard" []
  (set-location! [:dashboard]))

(defroute workspace-route "/workspace/:project-uuid/:page-uuid" [project-uuid page-uuid]
  (set-location! [:workspace (uuid project-uuid) (uuid page-uuid)]))

;; History

(def history (doto (Html5History.)
               (.setUseFragment false)
               (.setPathPrefix "")))

(defn- dispatch-uri
  [u]
  (s/dispatch! (.-token u)))

(defn start-history!
  []
  (events/listen history EventType.NAVIGATE dispatch-uri)
  (.setEnabled history true)
  (register-transition
   :location
   (fn [state location]
     (assoc state :location location))))

(defn navigate!
  [uri]
  (.setToken history uri))

;; Components

(defn link
  [href component]
  [:a
   {:href href
    :on-click #(do (.preventDefault %) (navigate! href))}
   component])
