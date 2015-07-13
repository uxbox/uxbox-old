(ns uxbox.workspace.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn close-setting-box
  [setting-box]
  (pubsub/publish! [:close-setting-box setting-box]))

(defn set-tool
  [tool]
  (pubsub/publish! [:set-tool tool]))

(pubsub/register-handler
 :close-setting-box
 (fn [state setting-box]
   (update state :open-setting-boxes (fn [boxes] (filter #(not= setting-box %1) boxes)))))

(pubsub/register-handler
 :set-tool
 (fn [state tool]
   (assoc-in state [:workspace :selected-tool] tool)))

