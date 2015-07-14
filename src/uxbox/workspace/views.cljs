(ns uxbox.workspace.views
  (:require [uxbox.user.views :refer [user]]
            [uxbox.icons :refer [chat close page folder trash pencil action fill stroke infocard]]
            [uxbox.navigation :refer [link]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.icons :as icons]
            [uxbox.workspace.figures.catalogs :as figures-catalogs]
            [uxbox.workspace.canvas.views :refer [canvas]]))

(defn header
  [db]
  [:header#workspace-bar.workspace-bar
    [:div.main-icon
     [link "/dashboard"
      icons/logo]]
    [:div.project-tree-btn
     icons/project-tree
     [:span "Page name"]]
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
       [:select.input-select.small {:on-change #(actions/set-figures-catalog (keyword (.-value (.-target %))))}
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

(defn elementoptions
  [db]
  [:div#element-options.element-options
    [:ul.element-icons
      [:li#e-info.selected
        infocard]
      [:li#e-fill
        fill]
      [:li#e-stroke
        stroke]
      [:li#e-text
        icons/text]
      [:li#e-actions
        action]]
    ;;ELEMENT BASIC INFO
    [:div#element-basics.element-set
      [:div.element-set-title "Element name"]
      [:div.element-set-content
        [:span "Size"]
        [:div.row-flex
          [:input#element-width.input-text {:placeholder "Width" :type "text"}]
          [:div.lock-size
            icons/lock]
          [:input#element-height.input-text {:placeholder "Height" :type "text"}]]
        [:span "Position"]
        [:div.row-flex
          [:input#element-positionx.input-text {:placeholder "X" :type "text"}]
          [:input#element-positiony.input-text {:placeholder "Y" :type "text"}]]
        [:span "Padding"]
        [:div.row-flex
          [:input#element-padding-top.input-text {:placeholder "Top" :type "text"}]
          [:input#element-padding-rigth-.input-text {:placeholder "Right" :type "text"}]]
        [:div.row-flex
          [:input#element-padding-bottom.input-text {:placeholder "Bottom" :type "text"}]
          [:input#element-padding-left-.input-text {:placeholder "Left" :type "text"}]]
        [:div.row-flex
          [:span.half "Border radius"]
          [:span.half "Opacity"]]
        [:div.row-flex
          [:input#element-border-radius.input-text {:placeholder "px" :type "text"}]
          [:input#element-opacity.input-text      {:placeholder "%" :type "text"}]]]]
    ;;ELEMENT FILL
    [:div#element-fill.element-set.hide
      [:div.element-set-title "Fill color"]
      [:div.element-set-content
        [:span "Choose a color"]
        [:p "COLOR PICKER"]]]
    ;;ELEMENT STROKE
    [:div#element-stroke.element-set.hide
      [:div.element-set-title "Stroke"]
      [:div.element-set-content
        [:span "Border color"]
        [:p "COLOR PICKER"]
        [:div.row-flex
          [:span.half "Border width"]
          [:span.half "Border style"]]
        [:div.row-flex
          [:input#element-border-width.input-text      {:placeholder "px" :type "text"}]
          [:select#element-border-style.input-select
            [:option "Solid"]
            [:option "Dotted"]
            [:option "Dashed"]
            [:option "Double"]]]]]
    ;;ELEMENT TEXT
    [:div#element-text.element-set.hide
      [:div.element-set-title "Text"]
      [:div.element-set-content]]
    ;;ELEMENT ACTIONS
    [:div#element-actions.element-set.hide
      [:div.element-set-title "Actions"]
      [:div.element-set-content]]])

(defn tools [db]
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
      [:li {:class (if (:tools (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :tools)} icons/shapes]
      [:li {:class (if (:components (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :components)} icons/puzzle]
      [:li {:class (if (:figures (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :figures)} icons/icon-set]
      [:li {:class (if (:layers (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :layers)} icons/layers]
      [:li
       chat]]]])

(defn projectbar
  [db]
  [:div#project-bar.project-bar.toggle
    [:div.project-bar-inside
      [:span.project-name "Project name"]
      [:ul.tree-view
        [:li.single-page.current
          [:div.tree-icon page]
          [:span "Homepage"]]
        [:li.single-page
          [:div.tree-icon page]
          [:span "Profile"]
          [:div.options
            [:div pencil]
            [:div trash]]]
        [:li.group-page
          [:div.tree-icon page]
          [:span "Contact"]]]
      [:button.btn-primary.btn-small "+ Add new page"]
      ]])

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
      [elementoptions db]
      [canvas db]]]
    (if (not (empty? (:open-setting-boxes @db)))
     [settings db])]])
