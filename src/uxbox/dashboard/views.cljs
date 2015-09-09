(ns uxbox.dashboard.views
  (:require
   rum
   [datascript :as d]
   [uxbox.data.db :refer [conn]]
   [uxbox.data.queries :as q]
   [uxbox.user.views :refer [user]]
   [uxbox.activity :refer [activity-timeline]]
   [uxbox.dashboard.actions :as actions]
   [uxbox.projects.actions :refer [delete-project]]
   [uxbox.dashboard.icons :as icons]
   [uxbox.icons :refer [chat logo]]
   [uxbox.navigation :refer [navigate! link workspace-page-route workspace-route]]
   [uxbox.time :refer [ago]]))

;; Materialized queries
(def projects-atom
  (q/pipe-to-atom q/pull-projects conn :projects))
(def project-count-atom
  (q/pipe-to-atom q/project-count conn :project-count))

;; Config
;; TODO: i18nized names
(def project-orderings {:project/name "name"
                        :project/last-update "date updated"
                        :project/created "date created"})

(def name->order (into {} (for [[k v] project-orderings] [v k])))

(rum/defc header < rum/static
  []
  [:header#main-bar.main-bar
   [:div.main-logo
    (link "/" logo)]
   (user)])

(rum/defc project-count < rum/reactive
  []
  [:span.dashboard-projects (rum/react project-count-atom) " projects"])

(rum/defc project-sort-selector < rum/reactive
  [sort-order]
  (let [sort-name (get project-orderings (rum/react sort-order))]
    [:select.input-select
     {:on-change #(reset! sort-order (name->order (.-value (.-target %))))
      :value sort-name}
     (for [order (keys project-orderings)
           :let [name (get project-orderings order)]]
       [:option {:key name} name])]))

(rum/defc dashboard-bar
  [sort-order]
  [:section#dashboard-bar.dashboard-bar
    [:div.dashboard-info
     (project-count)
     [:span "Sort by"]
     (project-sort-selector sort-order)]
    [:div.dashboard-search
     icons/search]])

(rum/defc project-card < rum/static
  [{uuid :project/uuid
    last-update :project/last-update
    name :project/name
    pages :project/pages
    comment-count :project/comment-count}]
  [:div.grid-item.project-th
   {:on-click #(navigate! (workspace-route {:project-uuid uuid}))
    :key uuid}
   [:h3
    name]
   [:span.project-th-update "Updated " (ago last-update)]
   [:div.project-th-actions
    [:div.project-th-icon.pages
     icons/page
     [:span pages]]
    [:div.project-th-icon.comments
     chat
     [:span comment-count]]
    [:div.project-th-icon.delete
     {:on-click #(do (.stopPropagation %)
                     (delete-project uuid)
                     %)}
     icons/trash]]])

(def new-project
  [:div.grid-item.add-project
   {:on-click #(actions/new-project)}
   [:span "+ New project"]])

(defn sorted-projects
  [projects sort-order]
  (let [project-cards (map project-card (sort-by sort-order projects))]
    (if (= sort-order :project/name)
      project-cards
      (reverse project-cards))))

(rum/defc dashboard-grid < rum/reactive
  [sort-order]
  [:section.dashboard-grid
    [:h2 "Your projects"]
   [:div.dashboard-grid-content
    (vec
     (concat [:div.dashboard-grid-content new-project]
             (sorted-projects (rum/react projects-atom)
                              (rum/react sort-order))))]])

(rum/defcs dashboard < (rum/local :project/name :project-sort-order)
                        rum/reactive
  [{sort-order :project-sort-order} db]
  [:main.dashboard-main
    (header)
    [:section.dashboard-content
     (dashboard-bar sort-order)
     (dashboard-grid sort-order)]
    (activity-timeline (rum/cursor db [:activity]))])
