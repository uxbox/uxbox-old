(ns uxbox.workspace.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn close-setting-box
  [setting-box]
  (pubsub/publish! [:close-setting-box setting-box]))

(defn open-setting-box
  [setting-box]
  (pubsub/publish! [:open-setting-box setting-box]))

(defn set-tool
  [tool]
  (pubsub/publish! [:set-tool tool]))

(pubsub/register-handler
 :close-setting-box
 (fn [state setting-box]
   (update state :open-setting-boxes #(disj %1 setting-box))))

(pubsub/register-handler
 :open-setting-box
 (fn [state setting-box]
   (if (= setting-box :layers)
     (update state :open-setting-boxes #(conj %1 setting-box))
     (update state :open-setting-boxes #(clojure.set/intersection (conj %1 setting-box) #{:layers setting-box})))))

(pubsub/register-handler
 :set-tool
 (fn [state tool]
   (assoc-in state [:workspace :selected-tool] tool)))

