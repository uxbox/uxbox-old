(ns uxbox.dashboard.views
  (:require [uxbox.dashboard.actions :as actions]
            [uxbox.projects.actions :refer [delete-project]]
            [uxbox.dashboard.icons :as icons]
            [uxbox.icons :refer [chat logo]]
            [uxbox.user.views :refer [user]]
            [uxbox.navigation :refer [link navigate! workspace-route]]
            [uxbox.time :refer [ago]]))

(defn header [usr]
  [:header#main-bar.main-bar
   [:div.main-logo
    [link "/" logo]]
   [user usr]])

(defn activity [db]
  [:aside#activity-bar.activity-bar
   [:div.activity-bar-inside
    [:h4 "ACTIVITY"]
    [:span.date-ribbon "TODAY"]
    [:div.activity-input
     [:img.activity-author
      {:border "0", :src "../../images/avatar.jpg"}]
     [:div.activity-content
      [:span.bold "Michael Buchannon"]
      [:span "created new page"]
      [:div.activity-project
       [link "" "Contact"]
       [:span "in"]
       [link "" "Wireframes Taiga Tribe"]]
      [:span.activity-time "12 min ago"]]]
    [:div.activity-input
     [:img.activity-author
      {:border "0", :src "../../images/avatar.jpg"}]
     [:div.activity-content
      [:span.bold "Michael Buchannon"]
      [:span "created new page"]
      [:div.activity-project
       [link "" "Contact"]
       [:span "in"]
       [link "" "Wireframes Taiga Tribe"]]
      [:span.activity-time "12 min ago"]]]
    [:span.date-ribbon "YESTERDAY"]
    [:div.activity-input
     [:img.activity-author
      {:border "0", :src "../../images/avatar.jpg"}]
     [:div.activity-content
      [:span.bold "Michael Buchannon"]
      [:span "created new page"]
      [:div.activity-project
       [link "" "Contact"]
       [:span "in"]
       [link "" "Wireframes Taiga Tribe"]]
      [:span.activity-time "12 min ago"]]]
    [:div.activity-input
     [:img.activity-author
      {:border "0", :src "../../images/avatar.jpg"}]
     [:div.activity-content
      [:span.bold "Michael Buchannon"]
      [:span "created new page"]
      [:div.activity-project
       [link "" "Contact"]
       [:span "in"]
       [link "" "Wireframes Taiga Tribe"]]
      [:span.activity-time "12 min ago"]]]]])

(defn mysvg [db icon-name]
  [:svg {:src (str "/assets/images/" icon-name ".svg") }])

(defn canvas [db]
  [:section.canvas.viewport
   [mysvg db "trash"]])


(defn dashboard-info [db]
  (let [projects (:projects @db)
        sort-order (:project-sort-order @db)
        orderings (:project-orderings @db)
        name->order (into {} (for [[k v] orderings] [v k]))
        sort-name (get orderings sort-order)]
    [:div.dashboard-info
     [:span.dashboard-projects (count projects) " projects"]
     [:span "Sort by"]
     [:select.sort-by
      {:on-change #(actions/set-projects-order (name->order (.-value (.-target %))))
       :value sort-name}
      (for [order (keys orderings)
            :let [name (get orderings order)]]
        [:option {:key name} name])]]))

(defn dashboard-bar [db]
  [:section#dashboard-bar.dashboard-bar
    [dashboard-info db]
    [:div.dashboard-search
     icons/search]])

(defn new-project []
  [:div.grid-item.add-project
   {:on-click #(actions/new-project)}
   [:span "+ New project"]])

(defn project-card [project]
  (let [{:keys [uuid last-update]} project]
    [:div.grid-item.project-th
     {:key uuid}
     [:h3 {:on-click #(navigate! (workspace-route {:uuid uuid}))}
      (:name project)]
     [:span.project-th-update "Updated " (ago last-update)]
     [:div.project-th-actions
      [:div.project-th-icon.pages
       icons/page
       [:span (:page-count project)]]
      [:div.project-th-icon.comments
       chat
       [:span (:comment-count project)]]
      [:div.project-th-icon.delete {:on-click #(delete-project (:uuid project))}
       icons/trash]]]))

(defn dashboard-grid [db]
  (let [projects (sort-by (:project-sort-order @db) (:projects @db))]
    [:section.dashboard-grid
     [:h2 "Your projects"]
     [:div.dashboard-grid-content
      [new-project]
      (if (= (:project-sort-order @db) :name)
        (map project-card projects)
        (reverse (map project-card projects)))]]))

(defn dashboard-content [db]
  [:main.dashboard-main
    [:section.dashboard-content
     [dashboard-bar db]
     [dashboard-grid db]]
    [activity db]])

(defn dashboard [db]
  [:div
   [header (:user @db)]
   [dashboard-content db]])
