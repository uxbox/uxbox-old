(ns uxbox.projects.actions
  (:require [uxbox.pubsub :as pubsub]
            [uxbox.projects.data :as d]
            [uxbox.storage :as storage]))

(defn create-project
  [{:keys [name width height layout]}]
  (let [now (js/Date.)
        project-uuid (random-uuid)
        page (d/create-page project-uuid "Homepage")]
    (pubsub/publish! [:create-project {:name name
                                       :width width
                                       :height height
                                       :layout layout
                                       :uuid project-uuid
                                       :last-update now
                                       :created now
                                       :first-page-uuid (:uuid page)
                                       :pages {(:uuid page) page}
                                       :comment-count 0}])))

(defn create-page
  [project title]
  (pubsub/publish! [:create-page [(:uuid project) (d/create-page (:uuid project) title)]]))

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
   (update state :projects-list assoc (:uuid project) project)))

(pubsub/register-transition
 :create-page
 (fn [state [project-uuid page]]
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
 (fn [state [project-uuid page]]
   (storage/create-page project-uuid page)))

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
