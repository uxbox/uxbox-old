(ns uxbox.activity
  (:require rum
            [uxbox.navigation :refer [navigate! workspace-page-route workspace-route]]
            [uxbox.time :refer [ago]]))

(rum/defc create-page-activity < rum/static
  [{:keys [project author event datetime]}]
  [:div.activity-content
    [:span.bold (:user/fullname author)]
    [:span "Create new page"]
    [:div.activity-project
     [:a
      {:on-click #(navigate! (workspace-page-route {:project-uuid (:uuid project) :page-uuid (:page-uuid event)}))}
      (:name event)]
     [:span "in"]
     [:a
      {:on-click #(navigate! (workspace-route {:project-uuid (:uuid project)}))}
      (:name project)]]
    [:span.activity-time (ago datetime)]])

(rum/defc create-project-activity < rum/static
  [{:keys [project author datetime]}]
  [:div.activity-content
    [:span.bold (:user/fullname author)]
    [:span "Create new project"]
     [:a {:on-click #(navigate! (workspace-route {:project-uuid (:uuid project)}))} (:name project)]
    [:span.activity-time (ago datetime)]])

(rum/defc activity-item < rum/static
  [{:keys [uuid event] :as a}]
  [:div.activity-input
   {:key uuid}
   [:img.activity-author
    {:border "0"
     :src "../../images/avatar.jpg"}]
   (case (:type event)
     :create-page
     (create-page-activity a)

     :create-project
     (create-project-activity a))])

(defn- activity-date
  [a]
   (.toDateString (:datetime a)))

(defn- activities-by-date
  [as]
  (->> as
       (take 15)
       (group-by activity-date)))

(rum/defc activity-timeline < rum/cursored
  [activities]
  [:aside#activity-bar.activity-bar
   [:div.activity-bar-inside
    [:h4 "ACTIVITY"]
     (for [[date items] (activities-by-date @activities)]
       (concat
        [[:span.date-ribbon
          {:key date}
          (.calendar (js/moment. date))]]
        (map activity-item items)))]])
