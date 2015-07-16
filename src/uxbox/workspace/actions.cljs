(ns uxbox.workspace.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.storage :as storage]))

(defn change-shape-attr
  [project-uuid page-uuid shape-uuid attr value]
  (pubsub/publish! [:change-shape-attr [project-uuid page-uuid shape-uuid attr value]]))

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

(defn toggle-select-group
  [group-id]
  (pubsub/publish! [:toggle-select-group group-id]))

(defn toggle-group-visibility
  [group-id]
  (pubsub/publish! [:toggle-group-visiblity group-id]))

(defn toggle-group-lock
  [group-id]
  (pubsub/publish! [:toggle-group-lock group-id]))

(defn view-page
  [project-uuid page-uuid]
  (pubsub/publish! [:view-page [project-uuid page-uuid]]))

(pubsub/register-transition
 :change-shape-attr
 (fn [state [project-uuid page-uuid shape-uuid attr value]]
   (assoc-in state [:page :shapes shape-uuid attr] value)))

(pubsub/register-effect
 :change-shape-attr
 (fn [state [project-uuid page-uuid shape-uuid attr value]]
   (storage/change-shape-attr project-uuid page-uuid shape-uuid attr value)))

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

(pubsub/register-transition
 :toggle-select-group
 (fn [state group-id]
   (assoc-in state [:workspace :selected-groups] #{group-id})))

(pubsub/register-transition
 :toggle-group-visiblity
 (fn [state group-id]
   (update-in state [:page :groups group-id :visible] #(not %1))))

(pubsub/register-transition
 :toggle-group-lock
 (fn [state group-id]
   (update-in state [:page :groups group-id :locked] #(not %1))))

(pubsub/register-transition
 :view-page
 (fn [state [project-uuid page-uuid]]
   (assoc state :page (storage/get-page project-uuid page-uuid))))

(pubsub/register-transition
 :location
 (fn [state data]
   (let [[location project-uuid page-uuid] data]
     (if (= location :workspace)
       (assoc state :project (storage/get-project project-uuid)
                    :page (storage/get-page project-uuid page-uuid)
                    :workspace (:workspace-defaults state)
                    :open-setting-boxes (:default-open-setting-boxes state)
                    :visible-project-bar false)
       state
       ))))
