(ns uxbox.projects.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.projects.data :as d]
            [uxbox.storage.api :as storage]))

(defn create-project
  [{:keys [name width height layout]}]
  (let [now (js/Date.)
        project (d/create-project name width height layout)
        page (d/create-page (:uuid project) "Homepage" width height)]
    (pubsub/publish! [:create-project project])
    (pubsub/publish! [:create-page page])))

(defn create-page
  [page]
  (pubsub/publish! [:create-page page]))

(defn change-page-title
  [project page title]
  (pubsub/publish! [:change-page-title [project page title]]))

(defn delete-page
  [project page]
  (pubsub/publish! [:delete-page [project page]]))

(defn delete-project
  [uuid]
  (pubsub/publish! [:delete-project uuid]))

(pubsub/register-transition
 :move-layer-down
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         groups (get-in state [:page :groups])
         selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
         previous-group (last (take-while #(not= (nth % 0) (nth selected-group 0)) (sort-by #(:order (nth % 1)) (seq groups))))
         selected-group-order (:order (nth selected-group 1))
         previous-group-order (:order (nth previous-group 1))]
     (if (and selected-group previous-group)
       (-> state
           (assoc-in [:page :groups (nth selected-group 0) :order] previous-group-order)
           (assoc-in [:page :groups (nth previous-group 0) :order] selected-group-order))
       state))))

(pubsub/register-transition
 :move-layer-up
 (fn [state _]
   (let [selected-uuid (get-in state [:page :selected])
         groups (get-in state [:page :groups])
         selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
         next-group (last (take-while #(not= (nth % 0) (nth selected-group 0)) (reverse (sort-by #(:order (nth % 1)) (seq groups)))))
         selected-group-order (:order (nth selected-group 1))
         next-group-order (:order (nth next-group 1))]
     (if (and selected-group next-group)
       (-> state
           (assoc-in [:page :groups (nth selected-group 0) :order] next-group-order)
           (assoc-in [:page :groups (nth next-group 0) :order] selected-group-order))
       state))))

;; Not working yet
;; (pubsub/register-transition
;;  :move-layer-to-bottom
;;  (fn [state _]
;;    (let [selected-uuid (get-in state [:page :selected])
;;          groups (get-in state [:page :groups])
;;          selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
;;          min-order-group (min (map :order (vals groups)))]
;;      (.log js/console min-order-group)
;;      (.log js/console (dec min-order-group))
;;      (-> state
;;          (assoc-in [:page :groups (nth selected-group 0) :order] (dec min-order-group))))))
;;
;; (pubsub/register-transition
;;  :move-layer-to-top
;;  (fn [state _]
;;    (let [selected-uuid (get-in state [:page :selected])
;;          groups (get-in state [:page :groups])
;;          selected-group (first (filter #(some (fn [shape] (= shape selected-uuid)) (:shapes (nth % 1))) (seq groups)))
;;          max-order-group (max (map :order (vals groups)))]
;;      (.log js/console max-order-group)
;;      (.log js/console (inc max-order-group))
;;      (assoc-in state [:page :groups (nth selected-group 0) :order] (inc max-order-group)))))

(pubsub/register-transition
 :delete-project
 (fn [state uuid]
   (update state :projects-list #(dissoc % uuid))))

(pubsub/register-transition
 :create-project
 (fn [state project]
   (let [now (js/Date.)
         activity {:author {:name "Michael Buchannon" :avatar "../../images/avatar.jpg"}
                   :uuid (random-uuid)
                   :project {:uuid (:uuid project) :name (:name project)}
                   :datetime now
                   :event {:type :create-project :text "Create new project"}}]
     (-> state
       (update :projects-list assoc (:uuid project) project)
       (update :activity #(into [activity] %))))))

(pubsub/register-transition
 :create-page
 (fn [state page]
   (assoc-in state [:project :pages (:uuid page)] page)))

(pubsub/register-effect
 :create-project
 (fn [state project]
   (storage/create-project project)))

(pubsub/register-effect
 :delete-project
 (fn [state uuid]
   (storage/delete-project uuid)))

(pubsub/register-effect
 :create-page
 (fn [state page]
   (storage/create-page page)))

(pubsub/register-transition
 :delete-page
 (fn [state [project page]]
   (let [page-uuid (:uuid page)
         new-state (update-in state [:project :pages] dissoc page-uuid)]
     (if (= (:uuid (:page state)) page-uuid)
       (assoc new-state :page (first (vals (get-in new-state [:project :pages]))))
       new-state))))

(pubsub/register-transition
 :change-page-title
 (fn [state [project page title]]
   (assoc-in state [:project :pages (:uuid page) :title] title)))

(pubsub/register-effect
 :change-page-title
 (fn [state [project page title]]
   (storage/change-page-title (:uuid project) page title)))

(pubsub/register-effect
 :delete-page
 (fn [state [project page]]
   (storage/delete-page (:uuid project) page)))
