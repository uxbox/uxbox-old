(ns uxbox.workspace.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.storage.api :as storage]
            [uxbox.workspace.canvas.actions :refer [new-group]]))

(defn change-shape-attr
  [project-uuid page-uuid shape-uuid attr value]
  (pubsub/publish! [:change-shape-attr [project-uuid page-uuid shape-uuid attr value]]))

(defn close-setting-box
  [setting-box]
  (pubsub/publish! [:close-setting-box setting-box]))

(defn toggle-setting-box
  [setting-box]
  (pubsub/publish! [:toggle-setting-box setting-box]))

(defn set-tool
  [tool]
  (pubsub/publish! [:set-tool tool]))

(defn copy-selected
  []
  (pubsub/publish! [:copy-selected]))

(defn paste-selected
  []
  (pubsub/publish! [:paste-selected]))

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

(pubsub/register-transition
 :change-shape-attr
 (fn [state [project-uuid page-uuid shape-uuid attr value]]
   (assoc-in state [:shapes shape-uuid attr] value)))

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

(pubsub/register-event
 :toggle-setting-box
 (fn [state setting-box]
   (let [setting-boxes (:open-setting-boxes state)]
     (if (contains? setting-boxes setting-box)
       (pubsub/publish! [:close-setting-box setting-box])
       (pubsub/publish! [:open-setting-box setting-box])))))

(pubsub/register-transition
 :set-tool
 (fn [state tool]
   (assoc-in state [:workspace :selected-tool] tool)))

 (pubsub/register-transition
  :copy-selected
  (fn [state]
    (let [selected-uuid (get-in state [:page :selected])
          selected-shape (get-in state [:shapes selected-uuid])]
          (assoc-in state [:workspace :selected] selected-shape))))

(pubsub/register-effect
 :paste-selected
 (fn [state]
   (let [shape-val (get-in state [:workspace :selected])
         shape-uuid (random-uuid)
         group-uuid (random-uuid)
         new-group-order (->> state :groups vals (sort-by :order) last :order inc)
         group-val (new-group (str "Group " new-group-order) new-group-order shape-uuid)]
      (do (pubsub/publish! [:insert-group [group-uuid group-val]])
          (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
          (assoc-in state [:workspace :selected] nil)))))

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
   (update-in state [:groups group-id :visible] #(not %1))))

(pubsub/register-effect
 :toggle-group-visiblity
 (fn [state group-id]
   (storage/toggle-group-visibility group-id)))

(pubsub/register-effect
 :toggle-group-lock
 (fn [state group-id]
   (storage/toggle-group-lock group-id)))

(pubsub/register-transition
 :toggle-group-lock
 (fn [state group-id]
   (update-in state [:groups group-id :locked] #(not %1))))

(pubsub/register-transition
 :location
 (fn [state data]
   (let [[location project-uuid page-uuid] data]
     (if (= location :workspace)
       (assoc state :project (storage/get-project project-uuid)
                    :page (storage/get-page page-uuid)
                    :project-pages (storage/get-pages project-uuid)
                    :groups (storage/get-groups project-uuid page-uuid)
                    :shapes (storage/get-shapes project-uuid page-uuid)
                    :workspace (:workspace-defaults state)
                    :open-setting-boxes (:default-open-setting-boxes state)
                    :visible-project-bar false)
       state
       ))))
