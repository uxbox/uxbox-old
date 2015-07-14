(ns uxbox.dashboard.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.storage :as storage]))

(defn set-projects-order
  [order]
  (pubsub/publish! [:set-projects-order order]))

(defn new-project
  []
  (pubsub/publish! [:new-project]))

(pubsub/register-transition
 :new-project
 (fn [state _]
   (assoc state :lightbox :new-project
                :new-project (:new-project-defaults state))))

(pubsub/register-transition
 :set-projects-order
 (fn [state order]
   (assoc state :project-sort-order order)))

(pubsub/register-transition
 :location
 (fn [state location]
   (if (= (last location) :dashboard)
     (assoc state :projects-list (storage/get-projects "user-1"))
     state)))
