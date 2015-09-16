(ns uxbox.ui.dashboard
  (:require
   rum
   [datascript :as d]
   [cuerdas.core :refer [trim]]
   [uxbox.data.mixins :as mx]
   [uxbox.ui.icons :as i]
   [uxbox.ui.users :refer [user]]
   [uxbox.ui.lightbox :refer [lightbox
                              render-lightbox
                              set-lightbox!
                              close-lightbox!]]
   [uxbox.ui.activity :refer [activity-timeline]]
   [uxbox.ui.icons.dashboard :as icons]
   [uxbox.projects.queries :as q]
   [uxbox.projects.actions :as actions]
   [uxbox.navigation :refer [navigate! link workspace-page-route workspace-route]]
   [uxbox.time :refer [ago]]))

;; Config
;; TODO: i18nized names
(def project-orderings {:project/name "name"
                        :project/last-updated "date updated"
                        :project/created "date created"})

(def project-layouts {:mobile {:name "Mobile"
                               :id "mobile"
                               :width 320
                               :height 480}
                      :tablet {:name "Tablet"
                               :id "tablet"
                               :width 1024
                               :height 768}
                      :notebook {:name "Notebook"
                                 :id "notebook"
                                 :width 1366
                                 :height 768}
                      :desktop {:name "Desktop"
                                :id "desktop"
                                :width 1920
                                :height 1080}})

(def new-project-defaults {:name ""
                           :width 1920
                           :height 1080
                           :layout :desktop})

(def name->order (into {} (for [[k v] project-orderings] [v k])))

;; Views

(defn layout-input
  [layout new-project]
  (let [human-name (get-in project-layouts [layout :name])
        id (str "project-" (get-in project-layouts [layout :id]))
        tag (str "input#" id)
        tag (keyword tag)]
    [[tag
       {:type "radio"
        :key id
        :name "project-layout"
        :value human-name
        :checked (= layout (:layout @new-project))
        :on-change #(swap! new-project merge {:layout layout
                                              :width (get-in project-layouts [layout :width])
                                              :height (get-in project-layouts [layout :height])})}]
      [:label
        {:value name
         :for id}
         human-name]]))

(rum/defc layout-selector
  [new-project]
  (vec (cons :div.input-radio.radio-primary
             (mapcat #(layout-input % new-project) (keys project-layouts)))))

(rum/defcs new-project-lightbox < (rum/local new-project-defaults :new-project)
  [{:keys [new-project]} conn]
  (let [{:keys [name width height layout]} @new-project]
    [:div.lightbox-body
     [:h3 "New project"]
     [:form
      {:on-submit (fn [e]
                    (.preventDefault e)
                    (let [new-project-attributes {:name (trim name)
                                                  :width (int width)
                                                  :height (int height)
                                                  :layout layout}]
                     (actions/create-project conn new-project-attributes)
                     (close-lightbox!)))}
      [:input#project-name.input-text
       {:placeholder "New project name"
        :type "text"
        :value name
        :auto-focus true
        :on-change #(swap! new-project assoc :name (.-value (.-target %)))}]
      [:div.project-size
       [:input#project-witdh.input-text
        {:placeholder "Width"
         :type "number"
         :min 0 ;;TODO check this value
         :max 666666 ;;TODO check this value
         :value width
         :on-change #(swap! new-project assoc :width (.-value (.-target %)))}]
       [:a.toggle-layout
        {:href "#"
         :on-click #(swap! new-project assoc :width height :height width)}
        i/toggle]
       [:input#project-height.input-text
        {:placeholder "Height"
         :type "number"
         :min 0 ;;TODO check this value
         :max 666666 ;;TODO check this value
         :value height
         :on-change #(swap! new-project assoc :height (.-value (.-target %)))}]]
      ;; Layout selector
      (layout-selector new-project)
      ;; Submit
      (when-not (empty? (trim name))
        [:input#project-btn.btn-primary
         {:value "Go go go!"
          :type "submit"}])]
     [:a.close
      {:href "#"
       :on-click #(close-lightbox!)}
      i/close]]))

(defmethod render-lightbox :new-project
  [_ conn]
  (new-project-lightbox conn))

(rum/defc header < rum/static
  [conn]
  [:header#main-bar.main-bar
   [:div.main-logo
    (link "/" i/logo)]
   (user conn)])

(rum/defc project-count < rum/static
  [n]
  [:span.dashboard-projects n " projects"])

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
  [sort-order pcount]
  [:section#dashboard-bar.dashboard-bar
    [:div.dashboard-info
     (project-count pcount)
     [:span "Sort by"]
     (project-sort-selector sort-order)]
    [:div.dashboard-search
     icons/search]])

(rum/defc project-card < rum/static
  [conn
   {uuid :project/uuid
    last-update :project/last-updated
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
     i/chat
     [:span comment-count]]
    [:div.project-th-icon.delete
     {:on-click #(do (.stopPropagation %)
                     (actions/delete-project conn uuid)
                     %)}
     icons/trash]]])

(rum/defc new-project < rum/static
  []
  [:div.grid-item.add-project
   {:on-click #(set-lightbox! :new-project)}
   [:span "+ New project"]])

(defn sorted-projects
  [conn projects sort-order]
  (let [project-cards (map (partial project-card conn) (sort-by sort-order projects))]
    (if (= sort-order :project/name)
      project-cards
      (reverse project-cards))))

(rum/defc dashboard-grid < rum/reactive
  [conn projects sort-order]
  [:section.dashboard-grid
    [:h2 "Your projects"]
   [:div.dashboard-grid-content
    (vec
     (concat [:div.dashboard-grid-content
              (new-project conn)]
             (sorted-projects conn
                              projects
                              (rum/react sort-order))))]])

(rum/defcs dashboard* < (rum/local :project/name :project-sort-order)
                        (mx/query :projects
                                  q/pull-projects)
                        (mx/query :project-count
                                  q/project-count)
  [{sort-order :project-sort-order
    projects :projects
    project-count :project-count} conn]
  [:main.dashboard-main
   (header conn)
   [:section.dashboard-content
    (dashboard-bar sort-order @project-count)
    (dashboard-grid conn @projects sort-order)]
   (activity-timeline conn)])

(rum/defc dashboard
  [conn]
  [:div
   (dashboard* conn)
   (lightbox conn)])
