(ns uxbox.ui.activity
  (:require rum
            [uxbox.navigation :refer [navigate! workspace-page-route workspace-route]]
            [uxbox.log.queries :as q]
            [uxbox.projects.queries :as pq]
            [uxbox.time :refer [ago day]]
            [uxbox.ui.mixins :as mx]))

(def shown-events
  #{:uxbox/create-project :uxbox/create-page})

(defn materialize
  [ev conn]
  (case (:event/type ev)
    :uxbox/create-project
    (let [path [:event/payload :page/project]
          project (pq/pull-project-by-id (get-in ev path) @conn)]
      (assoc-in ev path project))
    ev))

(rum/defc create-page-activity < rum/static
  [{{project :page/project
     :as page} :event/payload
    author :event/author
    timestamp :event/timestamp
    :as event}]
  (let [puuid (:project/uuid project)]
    [:div.activity-content
     [:span.bold (:user/fullname author)]
     [:span "created new page"]
     [:div.activity-project
      [:a
       {:on-click #(navigate! (workspace-page-route {:project-uuid puuid
                                                     :page-uuid (:page/uuid page)}))}
       (:page/title page)]
      [:span "in"]
      [:a
       {:on-click #(navigate! (workspace-route {:project-uuid puuid}))}
       (:project/name project)]]
     [:span.activity-time (ago timestamp)]]))

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
  [conn ev]
  [:div.activity-input
   {:key uuid}
   [:img.activity-author
    {:border "0"
     :src "../../images/avatar.jpg"}]
   (case (:event/type ev)
     :uxbox/create-page
     (create-page-activity (materialize ev conn))

     :uxbox/create-project
     (create-project-activity (materialize ev conn)))])

(defn- activity-date
  [a]
   (.toDateString (:event/timestamp a)))

(defn- activities-by-date
  [es]
  (let [xform (comp
               (filter #(shown-events (:event/type %)))
               (take 15))]
    (->> (sequence xform es)
         reverse
         (group-by activity-date))))

(rum/defcs activity-timeline < (mx/pull-query
                                :events
                                q/all-events
                                '[:event/type
                                  :event/payload
                                  :event/author
                                  :event/timestamp])
  [{events :events} conn]
  [:aside#activity-bar.activity-bar
   [:div.activity-bar-inside
    [:h4 "ACTIVITY"]
    (for [[date items] (activities-by-date @events)]
      (concat
       [[:span.date-ribbon
         {:key date}
         (day date)]]
       (map #(activity-item conn %) items)))]])
