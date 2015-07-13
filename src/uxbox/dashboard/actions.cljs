(ns uxbox.dashboard.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn set-projects-order
  [order]
  (pubsub/publish! [:set-projects-order order]))

(defn new-project
  []
  (pubsub/publish! [:new-project]))

(pubsub/register-transition
 :new-project
 (fn [state _]
   (assoc state :lightbox :new-project)))

(pubsub/register-transition
 :set-projects-order
 (fn [state order]
   (assoc state :project-sort-order order)))
