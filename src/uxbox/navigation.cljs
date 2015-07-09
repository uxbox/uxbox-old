(ns uxbox.navigation
  (:require [uxbox.db :as db]
            [secretary.core :as s :refer-macros [defroute]]
            [goog.events :as events])
  (:import [goog.history Html5History]
           goog.history.EventType))

;; Routes

(defn- set-location!
  [location]
  (swap! db/app-state assoc :location location))

(defroute login-route "/" []
  (set-location! [:login]))

(defroute dashboard-route "/dashboard" []
  (set-location! [:dashboard]))

(defroute workspace-route "/workspace" []
  (set-location! [:workspace]))

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
  (.setEnabled history true))

(defn navigate!
  [uri]
  (.setToken history uri))
