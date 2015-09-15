(ns uxbox.activity
  (:require rum
            [datascript :as d]
            [uxbox.navigation :refer [navigate! workspace-page-route workspace-route]]
            [uxbox.data.queries :as q]
            [uxbox.data.mixins :as mx]
            [uxbox.time :refer [ago]]))

(rum/defc create-page-activity < rum/static
  [{page :event/payload
    author :event/author
    timestamp :event/timestamp
    :as event}]
  [:div.activity-content
    [:span.bold (:user/fullname author)]
    [:span "created new page"]
    [:div.activity-project
     [:a
      {:on-click #(navigate! (workspace-page-route {:project-uuid (:page/project page)
                                                    :page-uuid (:page/uuid page)}))}
      (:page/title page)]
     [:span "in"]
     [:a
      {:on-click #(navigate! (workspace-route {:project-uuid (:page/project page)}))}
      (:project/name (:page/project page))]]
    [:span.activity-time (ago timestamp)]])

(rum/defc create-project-activity < rum/static
  [{project :event/payload
    author :event/author
    timestamp :event/timestamp
    :as event}]
  [:div.activity-content
    [:span.bold (:user/fullname author)]
    [:span "created new project"]
     [:a
      {:on-click #(navigate! (workspace-route {:project-uuid (:project/uuid project)}))}
      (:project/name project)]
    [:span.activity-time (ago timestamp)]])

(rum/defc activity-item < rum/static
  [event]
  [:div.activity-input
   {:key uuid}
   [:img.activity-author
    {:border "0"
     :src "../../images/avatar.jpg"}]
   (case (:event/type event)
     :uxbox/create-page
     (create-page-activity event)

     :uxbox/create-project
     (create-project-activity event))])

(defn- activity-date
  [a]
   (.toDateString (:event/timestamp a)))

;; todo: push down to query
(defn- activities-by-date
  [es]
  (->> es
       (sort-by :event/timestamp)
       (reverse)
       (take 15)
       (group-by activity-date)))

(rum/defcs activity-timeline < (mx/query q/events :events)
  [{events :events} conn]
  [:aside#activity-bar.activity-bar
   [:div.activity-bar-inside
    [:h4 "ACTIVITY"]
    (for [[date items] (activities-by-date @events)]
      (concat
       [[:span.date-ribbon
         {:key date}
         (do
           (.calendar (js/moment. date)
                      nil
                      #js {:sameDay "[Today]"
                           :sameElse "[Today]"
                           :lastDay "[Yesterday]"
                           :lastWeek "[Last] dddd"
                           }))]]
       (map activity-item items)))]])
