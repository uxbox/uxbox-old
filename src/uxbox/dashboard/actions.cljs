(ns uxbox.dashboard.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn new-project
  []
  (pubsub/publish! [:new-project]))

(pubsub/register-handler
 :new-project
 (fn [state _]
   (assoc state :lightbox :new-project)))
