(ns uxbox.workspace.views
  (:require [reagent.core :refer [atom]]
            [uxbox.user.views :refer [user]]
            [uxbox.icons :refer [chat close page folder trash pencil]]
            [uxbox.navigation :refer [link]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.icons :as icons]
            [uxbox.workspace.figures.catalogs :as figures-catalogs]
            [uxbox.workspace.canvas.views :refer [canvas]]))

(defn project-tree
  [db]
  (let [page (:page @db)
        title (:title page)]
    [:div.project-tree-btn
     {:on-click #(swap! db update :visible-project-bar not)}
     icons/project-tree
     [:span title]]))

(defn header
  [db]
  [:header#workspace-bar.workspace-bar
    [:div.main-icon
     [link "/dashboard"
      icons/logo]]
    [project-tree db]
    [:div.workspace-options
     [:ul.options-btn
      [:li
       icons/undo]
      [:li
       icons/redo]]
     [:ul.options-btn
      [:li
       icons/export]
      [:li
       icons/image]]
     [:ul.options-btn
      [:li
       icons/ruler]
      [:li {:class (if (:grid (:workspace @db)) "selected" "") :on-click #(actions/toggle-grid)} icons/grid]
      [:li
       icons/alignment]
      [:li
       icons/organize]]]
   [user (:user @db)]])

(defn figures
  [db]
  (let [{:keys [workspace]} @db]
   [:div#form-figures.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/window]
      [:span "Figures"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :figures)}
       close]]
     [:div.tool-window-content
      [:div.figures-catalog
       [:select {:on-change #(actions/set-figures-catalog (keyword (.-value (.-target %))))}
        (for [[catalog-id catalog] (seq figures-catalogs/catalogs)]
          [:option {:key catalog-id :value catalog-id} (:name catalog)])]]
      (.log js/console (seq (get-in figures-catalogs/catalogs [(:current-catalog @db) :symbols])))
      (for [[figure-id figure] (seq (get-in figures-catalogs/catalogs [(:current-catalog @db) :symbols]))]
        [:div.figure-btn {:key figure-id
                          :class (if (= (:selected-tool workspace) [:figure (:current-catalog @db) figure-id]) "selected" "")
                          :on-click #(actions/set-tool [:figure (:current-catalog @db) figure-id])}
          [:svg (:svg figure)]])]]))

(defn components
  [db]
  (let [{:keys [workspace]} @db]
    [:div#form-components.tool-window
      [:div.tool-window-bar
       [:div.tool-window-icon
        icons/window]
       [:span "Components"]
       [:div.tool-window-close {:on-click #(actions/close-setting-box :components)}
        close]]
     [:div.tool-window-content
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :rect) "selected" "")
                      :on-click #(actions/set-tool :rect)} icons/box]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :circle) "selected" "")
                      :on-click #(actions/set-tool :circle)} icons/circle]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :line) "selected" "")
                      :on-click #(actions/set-tool :line)} icons/line]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :curve) "selected" "")
                      :on-click #(actions/set-tool :curve)} icons/curve]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :text) "selected" "")
                      :on-click #(actions/set-tool :text)} icons/text]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :arrow) "selected" "")
                      :on-click #(actions/set-tool :arrow)} icons/arrow]]]))

(defn tools
  [db]
  (let [{:keys [workspace]} @db]
   [:div#form-tools.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/window]
      [:span "Tools"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :tools)}
       close]]
     [:div.tool-window-content
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :rect) "selected" "")
                      :on-click #(actions/set-tool :rect)} icons/box]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :circle) "selected" "")
                      :on-click #(actions/set-tool :circle)} icons/circle]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :line) "selected" "")
                      :on-click #(actions/set-tool :line)} icons/line]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :curve) "selected" "")
                      :on-click #(actions/set-tool :curve)} icons/curve]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :text) "selected" "")
                      :on-click #(actions/set-tool :text)} icons/text]
      [:div.tool-btn {:class (if (= (:selected-tool workspace) :arrow) "selected" "")
                      :on-click #(actions/set-tool :arrow)} icons/arrow]]]))

(defn layers
  [db]
  (let [{:keys [page workspace]} @db
        {:keys [groups]} page

        group (fn [[group-id group] item]
           [:li {:key group-id
                 :class (if (contains? (:selected-groups workspace) group-id) "selected" "")
                 }
            [:div.toggle-element {:class (if (:visible group) "selected" "")
                                  :on-click #(actions/toggle-group-visibility group-id)} icons/eye]
            [:div.block-element {:class (if (:locked group) "selected" "")
                                 :on-click #(actions/toggle-group-lock group-id)} icons/lock]
            [:div.element-icon
             (cond
              (= (:icon group) :square) icons/box
              (= (:icon group) :circle) icons/circle
              (= (:icon group) :line) icons/line
              (= (:icon group) :text) icons/text
              (= (:icon group) :arrow) icons/arrow
              (= (:icon group) :curve) icons/curve)]
            [:span {:on-click #(actions/toggle-select-group group-id)} (:name group)]])
        ]
   [:div#layers.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/layers]
      [:span "Elements"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :layers)}
       close]]
     [:div.tool-window-content
      [:ul.element-list
       (map group (seq groups))]]]))

(defn toolbar
  [db]
  [:div#tool-bar.tool-bar
    [:div.tool-bar-inside
     [:ul.main-tools
      (if (:tools (:open-setting-boxes @db))
        [:li.current icons/shapes]
        [:li {:on-click #(actions/open-setting-box :tools)} icons/shapes])
      (if (:figures (:open-setting-boxes @db))
        [:li.current icons/puzzle]
        [:li {:on-click #(actions/open-setting-box :figures)} icons/puzzle])
      (if (:components (:open-setting-boxes @db))
        [:li.current icons/icon-set]
        [:li {:on-click #(actions/open-setting-box :components)} icons/icon-set])
      (if (:layers (:open-setting-boxes @db))
        [:li.current icons/layers]
        [:li {:on-click #(actions/open-setting-box :layers)} icons/layers])
      [:li
       chat]]]])

(defn project-page
  [db a-page]
  (let [hover? (atom false)]
    (fn []
      (let [title (:title a-page)]
        [:li.single-page.current
         {:on-click #(when (not= a-page (:page @db))
                       (actions/view-page a-page))
          :on-mouse-enter #(reset! hover? true)
          :on-mouse-leave #(reset! hover? false)}
         [:div.tree-icon page]
         [:span title]
         [:div
          (if (not @hover?)
            {:class "options hide"}
            {:class "options"})
          [:div pencil]
          [:div trash]]]))))

(defn projectbar
  [db]
  (let [location (:location @db)
        [_ uuid] location
        projects (:projects @db)
        project (get projects uuid)
        pages (:pages project)
        page-components (map (fn [p] [project-page db p]) pages)]
    [:div#project-bar.project-bar
     (when (not (:visible-project-bar @db))
       {:class "toggle"})
     [:div.project-bar-inside
      [:span.project-name (:name project)]
      [:ul.tree-view
       (when (seq pages)
         page-components)]
      [:button.btn-primary.btn-small "+ Add new page"]
      ]]))

(defn settings
  [db]
  [:aside#settings-bar.settings-bar
    [:div.settings-bar-inside
     (if (:tools (:open-setting-boxes @db))
      [tools db])
     (if (:figures (:open-setting-boxes @db))
      [figures db])
     (if (:components (:open-setting-boxes @db))
      [components db])
     (if (:layers (:open-setting-boxes @db))
      [layers db])]])

(defn workspace
  [db]
  [:div
   [header db]
   [:main.main-content
    [:section.workspace-content
     [toolbar db]
     [projectbar db]
     [:section.workspace-canvas {:class (if (empty? (:open-setting-boxes @db)) "no-tool-bar" "")}
      [canvas db]]]
    (if (not (empty? (:open-setting-boxes @db)))
     [settings db])]])
