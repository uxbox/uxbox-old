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

(defn set-figures-catalog
  [catalog]
  (pubsub/publish! [:set-figures-catalog catalog]))

(defn toggle-grid
  []
  (pubsub/publish! [:toggle-grid]))

(pubsub/register-transition
 :close-setting-box
 (fn [state setting-box]
   (update state :open-setting-boxes #(disj %1 setting-box))))

(pubsub/register-transition
 :open-setting-box
 (fn [state setting-box]
   (if (= setting-box :layers)
     (update state :open-setting-boxes #(conj %1 setting-box))
     (update state :open-setting-boxes #(clojure.set/intersection (conj %1 setting-box) #{:layers setting-box})))))

(pubsub/register-transition
 :set-tool
 (fn [state tool]
   (assoc-in state [:workspace :selected-tool] tool)))

(pubsub/register-transition
 :set-figures-catalog
 (fn [state catalog]
   (assoc state :current-catalog catalog)))

(pubsub/register-transition
 :toggle-grid
 (fn [state _]
   (update-in state [:workspace :grid] not)))
