(ns uxbox.ui.activity
  (:require rum
            [uxbox.log.queries :as q]
            [uxbox.projects.queries :as pq]
            [uxbox.time :refer [ago day]]
            [uxbox.ui.navigation :as nav]
            [uxbox.ui.mixins :as mx]))

(def shown-events
  #{:uxbox/create-project :uxbox/create-page})

(defn materialize
  [conn ev]
  (case (:event/type ev)

    :uxbox/create-project
    (let [path [:event/payload :page/project]
          project (pq/pull-project-by-id (get-in ev path) @conn)]
      (assoc-in ev path project))

    ;; else
    ev))

(defn activity-date
  [a]
   (.toDateString (:event/timestamp a)))

(def last-activities-xform
  (comp
   (filter #(shown-events (:event/type %)))
   (take 15)))

(defn activities-by-date
  [es]
  (->> (into [] last-activities-xform es)
       reverse
       (group-by activity-date)))

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
       {:on-click #(nav/navigate! :page {:project-uuid puuid
                                         :page-uuid (:page/uuid page)})}
       (:page/title page)]
      [:span "in"]
      [:a
       {:on-click #(nav/navigate! :project {:project-uuid puuid})}
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
      {:on-click #(nav/navigate! :project {:project-uuid (:project/uuid project)})}
      (:project/name project)]
    [:span.activity-time (ago timestamp)]])

(rum/defc activity-item < rum/static
  [ev]
  [:div.activity-input
   {:key uuid}
   [:img.activity-author
    {:border "0"
     :src "../../images/avatar.jpg"}]
   (case (:event/type ev)
     :uxbox/create-page
     (create-page-activity ev)

     :uxbox/create-project
     (create-project-activity ev))])

(rum/defcs timeline < (mx/pull-query :events
                                     q/all-events
                                     '[:event/type
                                       :event/payload
                                       :event/author
                                       :event/timestamp])
  [{events :events} conn]
  [:aside#activity-bar.activity-bar
   [:div.activity-bar-inside
    [:h4 "ACTIVITY"]
    (for [[date evs] (activities-by-date @events)]
      (concat
       [[:span.date-ribbon
         {:key date}
         (day date)]]
       (map #(activity-item (materialize conn %)) evs)))]])
