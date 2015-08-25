(ns uxbox.dashboard.views
  (:require rum
            [uxbox.user.views :refer [user]]
            [uxbox.activity :refer [activity-timeline]]
            [uxbox.dashboard.actions :as actions]
            [uxbox.projects.actions :refer [delete-project]]
            [uxbox.dashboard.icons :as icons]
            [uxbox.icons :refer [chat logo]]
            [uxbox.navigation :refer [navigate! link workspace-page-route workspace-route]]
            [uxbox.time :refer [ago]]))

(defn header
  [db]
  [:header#main-bar.main-bar
   [:div.main-logo
    (link "/" logo)]
   (user db)])

(rum/defc project-count < rum/static
  [n]
  [:span.dashboard-projects n " projects"])

(rum/defc dashboard-info
  [db]
  (let [projects (vals (:projects-list @db))
        sort-order (:project-sort-order @db)
        orderings (:project-orderings @db)
        name->order (into {} (for [[k v] orderings] [v k]))
        sort-name (get orderings sort-order)]
    [:div.dashboard-info
     (project-count (count projects))
     [:span "Sort by"]
     [:select.input-select
      {:on-change #(actions/set-projects-order (name->order (.-value (.-target %))))
       :value sort-name}
      (for [order (keys orderings)
            :let [name (get orderings order)]]
        [:option {:key name} name])]]))

(rum/defc dashboard-bar
  [db]
  [:section#dashboard-bar.dashboard-bar
    (dashboard-info db)
    [:div.dashboard-search
     icons/search]])

(rum/defc project-card < rum/static
  [{:keys [uuid last-update name pages comment-count]}]
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
     {:on-click #(do (.stopPropagation %) (delete-project uuid))}
     icons/trash]]])

(def new-project
  [:div.grid-item.add-project
   {:on-click #(actions/new-project)}
   [:span "+ New project"]])

(defn sorted-projects
  [projects sort-order]
  (let [project-cards (map project-card (sort-by sort-order projects))]
    (if (= sort-order :name)
      project-cards
      (reverse project-cards))))

(rum/defc dashboard-grid
  [db]
  [:section.dashboard-grid
    [:h2 "Your projects"]
    (vec
     (concat
      [:div.dashboard-grid-content
       new-project]
      (let [projects (vals (:projects-list @db))
            sort-order (:project-sort-order @db)]
        (sorted-projects projects sort-order))))])

(rum/defc dashboard [db]
  [:main.dashboard-main
    (header db)
    [:section.dashboard-content
     (dashboard-bar db)
     (dashboard-grid db)]
    (activity-timeline db)])
