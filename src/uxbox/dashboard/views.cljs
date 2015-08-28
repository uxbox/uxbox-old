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

(rum/defc header < rum/cursored
  [user-cursor]
  [:header#main-bar.main-bar
   [:div.main-logo
    (link "/" logo)]
   (user @user-cursor)])

(rum/defc project-count < rum/static
  [n]
  [:span.dashboard-projects n " projects"])


(defn reverse-associative
  [m]
  (into (empty m) (for [[k v] m] [v k])))

(rum/defc dashboard-info < rum/static
  [projects sort-order orderings]
  (let [name->order (reverse-associative orderings)
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

(rum/defc project-sort-selector < rum/static
  [sort-order orderings]
  (let [sort-name (get orderings sort-order)
        name->order (reverse-associative orderings)]
    [:select.input-select
     {:on-change #(actions/set-projects-order (name->order (.-value (.-target %))))
      :value sort-name}
     (for [order (keys orderings)
           :let [name (get orderings order)]]
       [:option {:key name} name])]))

(rum/defc dashboard-bar < rum/cursored
  [projects-cursor
   sort-order-cursor
   orderings-cursor]
  [:section#dashboard-bar.dashboard-bar
    [:div.dashboard-info
     (project-count (count @projects-cursor))
     [:span "Sort by"]
     (project-sort-selector @sort-order-cursor
                            @orderings-cursor)]
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

(rum/defc dashboard-grid < rum/cursored
  [projects-cursor sort-order-cursor]
  [:section.dashboard-grid
    [:h2 "Your projects"]
   [:div.dashboard-grid-content
    (vec
     (concat [:div.dashboard-grid-content new-project]
             (sorted-projects (vals @projects-cursor)
                              @sort-order-cursor)))]])

(rum/defc dashboard [db]
  [:main.dashboard-main
    (header (rum/cursor db [:user]))
    [:section.dashboard-content
     (dashboard-bar (rum/cursor db [:projects])
                    (rum/cursor db [:project-sort-order])
                    (rum/cursor db [:project-orderings]))
     (dashboard-grid (rum/cursor db [:projects])
                     (rum/cursor db [:project-sort-order]))]
    (activity-timeline (rum/cursor db [:activity]))])
