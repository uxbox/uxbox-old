(ns uxbox.workspace.views
  (:require [reagent.core :refer [atom]]
            [uxbox.user.views :refer [user]]
            [uxbox.icons :refer [chat close page folder trash pencil action fill stroke infocard]]
            [uxbox.navigation :refer [link]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.icons :as icons]
            [uxbox.workspace.figures.catalogs :as figures-catalogs]
            [uxbox.workspace.canvas.views :refer [canvas]]
            [uxbox.geometry :as geo]))

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
      [:li.tooltip.tooltip-bottom {:alt "Undo (Ctrl + Z)"}
       icons/undo]
      [:li.tooltip.tooltip-bottom {:alt "Redo (Ctrl + Shift + Z)"}
       icons/redo]]
     [:ul.options-btn
      [:li.tooltip.tooltip-bottom {:alt "Export (Ctrl + E)"}
       icons/export]
      [:li.tooltip.tooltip-bottom {:alt "Image (Ctrl + I)"}
       icons/image]]
     [:ul.options-btn
      [:li.tooltip.tooltip-bottom {:alt "Ruler (Ctrl + R)"}
       icons/ruler]
      [:li.tooltip.tooltip-bottom {:alt "Grid (Ctrl + G)" :class (if (:grid (:workspace @db)) "selected" "") :on-click #(actions/toggle-grid)} icons/grid]
      [:li.tooltip.tooltip-bottom {:alt "Align (Ctrl + A)"}
       icons/alignment]
      [:li.tooltip.tooltip-bottom {:alt "Organize (Ctrl + O)"}
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


(defmulti toolbar-coords (fn [shape _] (:shape shape)))

(defmethod toolbar-coords :rectangle [{:keys [x y width height]} px py]
  (let [vx (+ x width 50)
        vy y]
    (geo/viewportcord->clientcoord vx vy)))

(defmethod toolbar-coords :line [{:keys [x1 y1 x2 y2]} px py]
  (let [max-x (if (> x1 x2) x1 x2)
        min-y (if (< y1 y2) y1 y2)
        vx (+ max-x 50)
        vy min-y]
    (geo/viewportcord->clientcoord vx vy)))

(defmethod toolbar-coords :default [_ x y] [x y])

(defn elementoptions
  [db]
  (let [show-element (atom :options)]
    (fn []
      (let [selected-uuid (get-in @db [:page :selected])
            selected-shape (get-in @db [:page :shapes selected-uuid])
            [popup-x popup-y] (toolbar-coords selected-shape)]
        [:div#element-options.element-options
         {:style #js {:left popup-x :top popup-y}}
         [:ul.element-icons
          [:li#e-info {:on-click (fn [e] (reset! show-element :options))
                       :class (when (= @show-element :options) "selected")} infocard]
          [:li#e-fill {:on-click (fn [e] (reset! show-element :fill))
                       :class (when (= @show-element :fill) "selected")} fill]
          [:li#e-stroke {:on-click (fn [e] (reset! show-element :stroke))
                         :class (when (= @show-element :stroke) "selected")} stroke]
          [:li#e-text {:on-click (fn [e] (reset! show-element :text))
                       :class (when (= @show-element :text) "selected")} icons/text]
          [:li#e-actions {:on-click (fn [e] (reset! show-element :actions))
                          :class (when (= @show-element :actions) "selected")} action]]
         ;;ELEMENT BASIC INFO
         [:div#element-basics.element-set
          {:class (when (not (= @show-element :options)) "hide")}
          [:div.element-set-title "Element name"]
          [:div.element-set-content
           (when (and (:width selected-shape) (:height selected-shape))
             [:div
              [:span "Size"]
              [:div.row-flex
               [:input#element-width.input-text {:placeholder "Width"
                                                 :type "text"
                                                 :value (:width selected-shape)}]
               [:div.lock-size icons/lock]
               [:input#element-height.input-text {:placeholder "Height"
                                                  :type "text"
                                                  :value (:height selected-shape)}]]])
           (when (and (:x selected-shape) (:y selected-shape))
             [:div
              [:span "Position"]
              [:div.row-flex
               [:input#element-positionx.input-text {:placeholder "X" :type "text"}]
               [:input#element-positiony.input-text {:placeholder "Y" :type "text"}]]])

           (when (and (:x1 selected-shape) (:y1 selected-shape) (:x2 selected-shape) (:y2 selected-shape))
             [:div
              [:span "Initial position"]
              [:div.row-flex
               [:input#element-positionx.input-text {:placeholder "X" :type "text"}]
               [:input#element-positiony.input-text {:placeholder "Y" :type "text"}]]
              [:span "End position"]
              [:div.row-flex
               [:input#element-positionx.input-text {:placeholder "X" :type "text"}]
               [:input#element-positiony.input-text {:placeholder "Y" :type "text"}]]])

           (if (and (:rx selected-shape) (:ry selected-shape))
             [:div
              [:div.row-flex
               [:span.half "Border radius"]
               [:span.half "Opacity"]]
              [:div.row-flex
               [:input#element-border-radius.input-text {:placeholder "px" :type "text"}]
               [:input#element-opacity.input-text      {:placeholder "%" :type "text"}]]]

             [:div
              [:div.row-flex
               [:span.half "Opacity"]]
              [:div.row-flex
               [:input#element-opacity.input-text      {:placeholder "%" :type "text"}]]]
             )]]
         ;;ELEMENT FILL
         [:div#element-fill.element-set
          {:class (when (not (= @show-element :fill)) "hide")}
          [:div.element-set-title "Fill color"]
          [:div.element-set-content
           [:span "Choose a color"]
           [:p "COLOR PICKER"]]]
         ;;ELEMENT STROKE
         [:div#element-stroke.element-set
          {:class (when (not (= @show-element :stroke)) "hide")}
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
         [:div#element-text.element-set
          {:class (when (not (= @show-element :text)) "hide")}
          [:div.element-set-title "Text"]
          [:div.element-set-content]]
         ;;ELEMENT ACTIONS
         [:div#element-actions.element-set
          {:class (when (not (= @show-element :actions)) "hide")}
          [:div.element-set-title "Actions"]
          [:div.element-set-content]]]))))

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
      [:div.tool-btn.tooltip.tooltip-hover {:alt "Box (Ctrl + B)" :class (if (= (:selected-tool workspace) :rect) "selected" "")
                      :on-click #(actions/set-tool :rect)} icons/box]
      [:div.tool-btn.tooltip.tooltip-hover {:alt "Circle (Ctrl + E)" :class (if (= (:selected-tool workspace) :circle) "selected" "")
                      :on-click #(actions/set-tool :circle)} icons/circle]
      [:div.tool-btn.tooltip.tooltip-hover {:alt "Line (Ctrl + L)" :class (if (= (:selected-tool workspace) :line) "selected" "")
                      :on-click #(actions/set-tool :line)} icons/line]
      [:div.tool-btn.tooltip.tooltip-hover {:alt "Bezier (Ctrl + U)" :class (if (= (:selected-tool workspace) :curve) "selected" "")
                      :on-click #(actions/set-tool :curve)} icons/curve]
      [:div.tool-btn.tooltip.tooltip-hover {:alt "Text (Ctrl + T)" :class (if (= (:selected-tool workspace) :text) "selected" "")
                      :on-click #(actions/set-tool :text)} icons/text]
      [:div.tool-btn.tooltip.tooltip-hover {:alt "Arrow (Ctrl + A)" :class (if (= (:selected-tool workspace) :arrow) "selected" "")
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
      [:li.tooltip {:alt "Shapes (Ctrl + Shift + F)" :class (if (:tools (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :tools)} icons/shapes]
      [:li.tooltip {:alt "Components (Ctrl + Shift + C)" :class (if (:components (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :components)} icons/puzzle]
      [:li.tooltip {:alt "Icons (Ctrl + Shift + I)" :class (if (:figures (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :figures)} icons/icon-set]
      [:li.tooltip {:alt "Elements (Ctrl + Shift + L)" :class (if (:layers (:open-setting-boxes @db)) "current" "")
            :on-click #(actions/open-setting-box :layers)} icons/layers]
      [:li.tooltip {:alt "Feedback (Ctrl + Shift + M)"}
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
      (when (get-in @db [:page :selected])
        [elementoptions db])
      [canvas db]]]
    (if (not (empty? (:open-setting-boxes @db)))
     [settings db])]])
