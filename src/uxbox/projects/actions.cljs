(ns uxbox.projects.actions
  (:require
   [uxbox.projects.data :as d]
   [uxbox.pubsub :as pubsub]))

(defn create-project
  [{:keys [name width height layout]}]
  (let [now (js/Date.)]
    (pubsub/publish! [:create-project {:name name
                                       :width width
                                       :height height
                                       :layout layout
                                       :uuid (random-uuid)
                                       :last-update now
                                       :created now
                                       :pages []
                                       :comment-count 0}])))

(defn create-page
  [project name]
  (pubsub/publish! [:create-page (d/create-page project name)]))

(defn change-page-name
  [page name]
  (pubsub/publish! [:change-page-name {:page page :name name}]))

(defn delete-page
  [page]
  (pubsub/publish! [:delete-page page]))

(defn delete-project
  [uuid]
  (pubsub/publish! [:delete-project uuid]))

(pubsub/register-transition
 :delete-project
 (fn [state uuid]
   (update state :projects #(dissoc % uuid))))

(pubsub/register-transition
 :create-project
 (fn [state project]
   (update state :projects assoc (:uuid project) project)))

(pubsub/register-transition
 :create-page
 (fn [state page]
   (let [project (:project page)]
     (update-in state [:projects project :pages] conj page))))

(pubsub/register-transition
 :delete-page
 (fn [state page]
   (let [project-uuid (:project page)
         project (get (:projects state) project-uuid)
         pages (:pages project)
         page-uuid (:uuid page)
         new-pages (into [] (filter #(not= (:uuid %) page-uuid) pages))
         new-project (assoc project :pages new-pages)
         new-state (assoc-in state [:projects project-uuid] new-project)]
     (if (= (:page state) page-uuid)
       (assoc new-state :page (get (first new-pages) :uuid))
       new-state))))

(pubsub/register-transition
 :change-page-name
 (fn [state {:keys [page name]}]
   (let [new-page (assoc page :name name)
         project-uuid (:project page)
         project (get-in state [:projects project-uuid])
         new-pages (into [] (for [p (:pages project)]
                              (if (= (:uuid p) (:uuid new-page))
                                new-page
                                p)))]
     (assoc-in state [:projects project-uuid :pages] new-pages))))
