(ns uxbox.workspace.actions
  (:require [uxbox.pubsub :as pubsub]))

(defn change-shape-attr
  [project-uuid page-uuid shape-uuid attr value]
  (pubsub/publish! [:change-shape-attr [project-uuid page-uuid shape-uuid attr value]]))

(defn set-tool
  [tool]
  (pubsub/publish! [:set-tool tool]))

(defn copy-selected
  []
  (pubsub/publish! [:copy-selected]))

(defn paste-selected
  []
  (pubsub/publish! [:paste-selected]))

(defn set-icons-set
  [icons-set]
  (pubsub/publish! [:set-icons-set icons-set]))

(defn toggle-grid
  []
  (pubsub/publish! [:toggle-grid]))

(defn toggle-select-shape
  [shape-id]
  (pubsub/publish! [:toggle-select-shape shape-id]))

(defn toggle-shape-visibility
  [shape-id]
  (pubsub/publish! [:toggle-shape-visiblity shape-id]))

(defn toggle-shape-lock
  [shape-id]
  (pubsub/publish! [:toggle-shape-lock shape-id]))

(pubsub/register-transition
 :change-shape-attr
 (fn [state [project-uuid page-uuid shape-uuid attr value]]
   (assoc-in state [:shapes shape-uuid attr] value)))

(pubsub/register-effect
 :change-shape-attr
 (fn [state [project-uuid page-uuid shape-uuid attr value]]
   #_(storage/change-shape-attr project-uuid page-uuid shape-uuid attr value)))

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
         shape-uuid (random-uuid)]
      (do (pubsub/publish! [:insert-shape [shape-uuid shape-val]])
          (assoc-in state [:workspace :selected] nil)))))

(pubsub/register-transition
 :toggle-grid
 (fn [state _]
   (update-in state [:workspace :grid?] not)))

(pubsub/register-transition
 :toggle-select-shape
 (fn [state shape-id]
   (assoc-in state [:page :selected] shape-id)))

(pubsub/register-transition
 :toggle-shape-visiblity
 (fn [state shape-id]
   (update-in state [:shapes shape-id :visible] #(not %1))))

(pubsub/register-effect
 :toggle-shape-visiblity
 (fn [state shape-id]
   #_(storage/toggle-shape-visibility shape-id)))

(pubsub/register-effect
 :toggle-shape-lock
 (fn [state shape-id]
   #_(storage/toggle-shape-lock shape-id)))

(pubsub/register-transition
 :toggle-shape-lock
 (fn [state shape-id]
   (update-in state [:shapes shape-id :locked] #(not %1))))
