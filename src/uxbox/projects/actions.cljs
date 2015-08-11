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

(defn create-simple-page
  [project title]
  (pubsub/publish! [:create-page (d/create-page (:uuid project) title (:width project) (:height project))]))

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
   (assoc-in state [:project-pages (:uuid page)] page)))

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
         new-state (update-in state [:project-pages] dissoc page-uuid)]
     (if (= (:uuid (:page state)) page-uuid)
       (assoc new-state :page (first (vals (get-in new-state [:project-pages]))))
       new-state))))

(pubsub/register-transition
 :change-page-title
 (fn [state [project page title]]
   (assoc-in state [:project-pages (:uuid page) :title] title)))

(pubsub/register-effect
 :change-page-title
 (fn [state [project page title]]
   (storage/change-page-title (:uuid project) page title)))

(pubsub/register-effect
 :delete-page
 (fn [state [project page]]
   (storage/delete-page (:uuid project) page)))
