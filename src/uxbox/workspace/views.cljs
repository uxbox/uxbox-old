(ns uxbox.workspace.views
  (:require [reagent.core :as reagent :refer [atom]]
            [cuerdas.core :as str]
            [uxbox.user.views :refer [user]]
            [uxbox.icons :as icons]
            [uxbox.navigation :refer [link]]
            [uxbox.projects.actions :refer [create-page change-page-title delete-page]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.figures.catalogs :as figures-catalogs]
            [uxbox.workspace.canvas.views :refer [canvas]]
            [uxbox.geometry :as geo]
            [uxbox.shapes.core :as shapes]))

(defn project-tree
  [db]
  (let [title (get-in @db [:page :title])]
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
   [user db]])

(defn figures
  [db]
  (let [{:keys [workspace]} @db]
   [:div#form-figures.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/window]
      [:span "Figures"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :figures)}
       icons/close]]
     [:div.tool-window-content
      [:div.figures-catalog
       [:select.input-select.small {:on-change #(actions/set-figures-catalog (keyword (.-value (.-target %))))}
        (for [[catalog-id catalog] (seq figures-catalogs/catalogs)]
          [:option {:key catalog-id :value catalog-id} (:name catalog)])]]
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
        icons/close]]
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
  (let [show-element (atom :options)]
    (fn []
      (let [selected-uuid (get-in @db [:page :selected])
            project-uuid (get-in @db [:project :uuid])
            page-uuid (get-in @db [:page :uuid])
            selected-shape (get-in @db [:page :shapes selected-uuid])
            [popup-x popup-y] (shapes/toolbar-coords selected-shape)]
        [:div#element-options.element-options
         {:style #js {:left popup-x :top popup-y}}
         [:ul.element-icons
          [:li#e-info {:on-click (fn [e] (reset! show-element :options))
                       :class (when (= @show-element :options) "selected")} icons/infocard]
          (if (or (and (:rx selected-shape) (:ry selected-shape)) (and (:cx selected-shape) (:cy selected-shape)) (:path selected-shape))
            [:li#e-fill {:on-click (fn [e] (reset! show-element :fill))
                         :class (when (= @show-element :fill) "selected")} icons/fill])
          [:li#e-stroke {:on-click (fn [e] (reset! show-element :stroke))
                         :class (when (= @show-element :stroke) "selected")} icons/stroke]
          [:li#e-text {:on-click (fn [e] (reset! show-element :text))
                       :class (when (= @show-element :text) "selected")} icons/text]
          [:li#e-actions {:on-click (fn [e] (reset! show-element :actions))
         ;;ELEMENT SIZE AND POSITION
                          :class (when (= @show-element :actions) "selected")} icons/action]]
         ;;ELEMENT BASIC INFO
         [:div#element-basics.element-set
          {:class (when (not (= @show-element :options)) "hide")}
          [:div.element-set-title "Size and position"]
          [:div.element-set-content
           (when (and (:width selected-shape) (:height selected-shape))
             [:div
              [:span "Size"]
              [:div.row-flex
               [:input#width.input-text
                {:placeholder "Width"
                 :type "number"
                 :value (:width selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :width (->> % .-target .-value int))}]
               [:div.lock-size icons/lock]
               [:input#height.input-text
                {:placeholder "Height"
                 :type "number"
                 :value (:height selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :height (->> % .-target .-value int))}]]])
           (when (and (:x selected-shape) (:y selected-shape))
             [:div
              [:span "Position"]
              [:div.row-flex
               [:input#x.input-text
                {:placeholder "X"
                 :type "number"
                 :value (:x selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :x (->> % .-target .-value int))}]
               [:input#y.input-text
                {:placeholder "Y"
                 :type "number"
                 :value (:y selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :y (->> % .-target .-value int))}]]])

           (when (and (:cx selected-shape) (:cy selected-shape))
             [:div
              [:span "Position"]
              [:div.row-flex
               [:input#x.input-text
                {:placeholder "X"
                 :type "number"
                 :value (:cx selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :cx (->> % .-target .-value int))}]
               [:input#y.input-text
                {:placeholder "Y"
                 :type "number"
                 :value (:cy selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :cy (->> % .-target .-value int))}]]
               [:span "Width"]
               [:input#r.input-text
                {:placeholder "Width"
                 :type "number"
                 :value (:r selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :r (->> % .-target .-value int))}]])

           (when (and (:x1 selected-shape) (:y1 selected-shape) (:x2 selected-shape) (:y2 selected-shape))
             [:div
              [:span "Initial position"]
              [:div.row-flex
               [:input#x1.input-text
                {:placeholder "Initial X"
                 :type "number"
                 :value (:x1 selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :x1 (->> % .-target .-value int))}]
               [:input#y1.input-text
                {:placeholder "Initial Y"
                 :type "number"
                 :value (:y1 selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :y1 (->> % .-target .-value int))}]]
              [:span "End position"]
              [:div.row-flex
               [:input#x2.input-text
                {:placeholder "End X"
                 :type "number"
                 :value (:x2 selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :x2 (->> % .-target .-value int))}]
               [:input#y2.input-text
                {:placeholder "End Y"
                 :type "number"
                 :value (:y2 selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :y2 (->> % .-target .-value int))}]]])]]
         ;;ELEMENT FILL
         (if (or (and (:rx selected-shape) (:ry selected-shape)) (and (:cx selected-shape) (:cy selected-shape)) (:path selected-shape))
           [:div#fill.element-set
            {:class (when (not (= @show-element :fill)) "hide")}
            [:div.element-set-title "Fill color"]
            [:div.element-set-content
             [:span "Choose a color"]
             [:input#fill.input-text
              {:placeholder "Color"
               :type "text"
               :value (:fill selected-shape)
               :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :fill (->> % .-target .-value))}]
             [:span "Opacity"]
             [:input#element-opacity.input-text
              {:placeholder "%"
               :type "number"
               :value (:fill-opacity selected-shape)
               :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :fill-opacity (->> % .-target .-value))}]]])
         ;;ELEMENT STROKE
         [:div#element-stroke.element-set
          {:class (when (not (= @show-element :stroke)) "hide")}
          [:div.element-set-title "Stroke"]
          [:div.element-set-content
           [:div.row-flex
            [:span.half "Color"]
            [:span.half "Opacity"]]
           [:div.row-flex
            [:input#stroke.input-text
             {:placeholder "Color"
              :type "text"
              :value (:stroke selected-shape)
              :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :stroke (->> % .-target .-value))}]
            [:input#stroke-opacity.input-text
             {:placeholder "Opacity"
              :type "number"
              :value (:stroke-opacity selected-shape)
              :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :stroke-opacity (->> % .-target .-value))}]]
           [:div.row-flex
            [:span.half "Width"]]
           [:div.row-flex
            [:input#stroke-width.input-text
             {:placeholder "Width"
              :type "number"
              :value (:stroke-width selected-shape)
              :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :stroke-width (->> % .-target .-value int))}]]

           (if (and (:rx selected-shape) (:ry selected-shape))
             [:div
              [:span "Radius"]
              [:div.row-flex
               [:input#rx.input-text
                {:placeholder "rx"
                 :type "number"
                 :value (:rx selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :rx (->> % .-target .-value))}]
               [:input#ry.input-text
                {:placeholder "ry"
                 :type "number"
                 :value (:ry selected-shape)
                 :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid :ry (->> % .-target .-value))
                 }]]
            ])]]
         ;;ELEMENT TEXT
         [:div#element-text.element-set
          {:class (when (not (= @show-element :text)) "hide")}
          [:div.element-set-title "Text"]
          [:div.element-set-content]]
         ;;ELEMENT ACTIONS
         [:div#element-actions.element-set
          {:class (when (not (= @show-element :actions)) "hide")}
          [:div.element-set-title "Actions"]
          [:div.element-set-content
            [:span.half "Rotation"]
            [:input#stroke.input-text
             {:placeholder "Degrees"
              :type "number"
              :value (:rotate selected-shape)
              :on-change #(swap! db assoc-in [:page :shapes selected-uuid :rotate] (->> % .-target .-value))}]]
          [:div.element-set-content]]]))))

(defn tools [db]
  (let [{:keys [workspace]} @db]
   [:div#form-tools.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/window]
      [:span "Tools"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :tools)}
       icons/close]]
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
       icons/close]]
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
       icons/chat]]]])

(defn project-page
  [db page]
  (let [current-page (:page @db)
        current-project (:project @db)
        editing-pages (:editing-pages @db)
        page-uuid (:uuid page)]

    (if (contains? editing-pages page-uuid)
      [:input.input-text
       {:title "page-title"
        :auto-focus true
        :placeholder "Page title"
        :type "text"
        :value (get editing-pages page-uuid)
        :on-change #(swap! db assoc-in [:editing-pages page-uuid] (.-value (.-target %)))
        :on-key-up #(cond
                      (= (.-keyCode %) 13)
                        (when (not (empty? (str/trim (get editing-pages page-uuid))))
                          (change-page-title current-project page (get editing-pages page-uuid))
                          (swap! db update :editing-pages dissoc page-uuid))
                      (= (.-keyCode %) 27)
                        (swap! db update :editing-pages dissoc page-uuid))
        :key page-uuid}]

      [:li.single-page
       {:class (if (= page-uuid (:uuid current-page)) "current" "")
        :on-click #(when (not= page-uuid (:uuid current-page))
                     (actions/view-page (:uuid current-project) page-uuid))
        :key page-uuid}
       [:div.tree-icon icons/page]
       [:span (:title page)]
       [:div.options
        [:div
         {:on-click #(do (.stopPropagation %) (swap! db assoc-in [:editing-pages page-uuid] (:title page)))}
         icons/pencil]
        [:div {:on-click #(do (.stopPropagation %) (delete-page current-project page))} icons/trash]]])))

(defn clean-new-page!
  [db]
  (swap! db assoc :adding-new-page false
                  :new-page-title ""))

(defn new-page
  [db project]
  (if (:adding-new-page @db)
    [:input.input-text
     {:title "page-title"
      :auto-focus true
      :placeholder "Page title"
      :type "text"
      :value (:new-page-title @db)
      :on-change #(swap! db assoc :new-page-title (.-value (.-target %)))
      :on-key-up #(cond
                    (= (.-keyCode %) 13)
                    (when (not (empty? (str/trim (:new-page-title @db))))
                      (create-page project (:new-page-title @db))
                      (clean-new-page! db))
                    (= (.-keyCode %) 27)
                    (clean-new-page! db))}]
    [:button.btn-primary.btn-small
     {:on-click #(swap! db assoc :adding-new-page true)}
     "+ Add new page"]))

(defn projectbar
  [db]
  (let [project (:project @db)
        project-name (:name project)
        pages (:pages project)
        page-components (map (fn [p] [project-page db p]) (vals pages))]
    [:div#project-bar.project-bar
     (when (not (:visible-project-bar @db))
       {:class "toggle"})
     [:div.project-bar-inside
      [:span.project-name project-name]
      [:ul.tree-view
       page-components]
      [new-page db project]]]))

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
