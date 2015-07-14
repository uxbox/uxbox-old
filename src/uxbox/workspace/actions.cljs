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

(defn enter-workspace
  []
  (pubsub/publish! [:enter-workspace]))

(defn leave-workspace
  []
  (pubsub/publish! [:leave-workspace]))

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

(defn view-project
  [project]
  (pubsub/publish! [:view-project project]))

(defn view-page
  [page]
  (pubsub/publish! [:view-page page]))

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
 :enter-workspace
 (fn [state _]
   (-> state
    (assoc :workspace (:workspace-defaults state))
    (assoc :open-setting-boxes (:default-open-setting-boxes state)))))

(pubsub/register-transition
 :leave-workspace
 (fn [state _]
   (assoc state :visible-project-bar false)))

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
 :view-project
 (fn [state project]
   (assoc state :project (:uuid project)
                :page (get (first (:pages project)) :uuid))))

(pubsub/register-transition
 :view-page
 (fn [state page]
   (assoc state :page (:uuid page))))
