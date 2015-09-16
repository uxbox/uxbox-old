(ns uxbox.ui.workspace
  (:require rum
            [cuerdas.core :as str]
            [uxbox.keyboard :as k]
            [uxbox.ui.icons :as icons]
            [uxbox.navigation :refer [link workspace-page-route navigate!]]
            [uxbox.ui.tools :as t]
            [uxbox.ui.users :refer [user]]
            [uxbox.ui.canvas :refer [canvas grid debug-coordinates]]
            [uxbox.ui.workspace.streams :as ws]
            [uxbox.geometry :as geo]
            [uxbox.shapes.protocols :as shapes]
            [uxbox.projects.queries :as q]
            [uxbox.projects.actions :refer [create-page change-page-title delete-page]]
            [uxbox.shapes.actions :as actions]
            [uxbox.shapes.queries :as sq]))

;; Actions

(defn- open-toolbox
  [open-toolboxes toolbox]
  (if (= toolbox :layers)
    (conj open-toolboxes toolbox)
    (clojure.set/intersection (conj open-toolboxes toolbox)
                              #{:layers toolbox})))

(defn- close-toolbox
  [open-toolboxes toolbox]
  (disj open-toolboxes toolbox))

(defn- toggle-toolbox
  [open-toolboxes toolbox]
  (if (contains? open-toolboxes toolbox)
    (close-toolbox open-toolboxes toolbox)
    (open-toolbox open-toolboxes toolbox)))

;; Constants

(def viewport-height  3000)
(def viewport-width 3000)

(def document-start-x 50)
(def document-start-y 50)

;; Views

(rum/defc project-tree < rum/cursored
  [page-title project-bar-visible?]
  [:div.project-tree-btn
   {:on-click #(swap! project-bar-visible? not)}
   icons/project-tree
   [:span page-title]])

(rum/defc header < rum/cursored
  [conn page grid? project-bar-visible?]
  (let [{page-title :page/title
         page-width :page/width
         page-height :page/height} page]
    [:header#workspace-bar.workspace-bar
     [:div.main-icon
      (link "/dashboard"
            icons/logo-icon)]
     (project-tree page-title project-bar-visible?)
     [:div.workspace-options
      [:ul.options-btn
       [:li.tooltip.tooltip-bottom {:alt "Undo (Ctrl + Z)"}
        icons/undo]
       [:li.tooltip.tooltip-bottom {:alt "Redo (Ctrl + Shift + Z)"}
        icons/redo]]
      [:ul.options-btn
       ;; TODO: refactor
       [:li.tooltip.tooltip-bottom
        {:alt "Export (Ctrl + E)"}
        ;; page-title
        [:a {:download (str page-title ".svg")
             :href "#"
             :on-click (fn [e]
                         (let [innerHTML (.-innerHTML (.getElementById js/document "page-layout"))
                               width page-width
                               height page-height
                               html (str "<svg width='" width  "' height='" height  "'>" innerHTML "</svg>")
                               data (js/Blob. #js [html] #js {:type "application/octet-stream"})
                               url (.createObjectURL (.-URL js/window) data)]
                           (set! (.-href (.-currentTarget e)) url)))}
         icons/export]]
       [:li.tooltip.tooltip-bottom
        {:alt "Image (Ctrl + I)"}
        icons/image]]
      [:ul.options-btn
       [:li.tooltip.tooltip-bottom
        {:alt "Ruler (Ctrl + R)"}
        icons/ruler]
       [:li.tooltip.tooltip-bottom
        {:alt "Grid (Ctrl + G)"
         :class (when @grid?
                  "selected")
         :on-click #(swap! grid? not)}
        icons/grid]
       [:li.tooltip.tooltip-bottom
        {:alt "Align (Ctrl + A)"}
        icons/alignment]
       [:li.tooltip.tooltip-bottom
        {:alt "Organize (Ctrl + O)"}
        icons/organize]]]
     (user conn)]))

(rum/defcs icon-sets < (rum/local (first (keys @t/icon-sets))
                                  :current-icon-set)
                                   rum/cursored rum/reactive
  [{:keys [current-icon-set]} open-toolboxes]
  [:div#form-figures.tool-window
   [:div.tool-window-bar
    [:div.tool-window-icon
     icons/window]
    [:span "Figures"]
    [:div.tool-window-close
     {:on-click #(swap! open-toolboxes close-toolbox :icon-sets)}
     icons/close]]
   [:div.tool-window-content
    [:div.figures-catalog
     ;; extract component: set selector
     [:select.input-select.small
      {:on-change #(reset! current-icon-set (keyword (.-value (.-target %))))}
      (for [[icon-set-key icon-set] (rum/react t/icon-sets)]
        [:option
         {:key icon-set-key
          :value icon-set-key}
         (:name icon-set)])]]
    ;; extract component: icon set
    (for [[icon-key icon] (get-in (rum/react t/icon-sets) [@current-icon-set :icons])]
      [:div.figure-btn
       {:key icon-key
        :class (when (= (rum/react ws/selected-tool)
                        [:icon icon])
                 "selected")
        :on-click #(ws/toggle-tool! [:icon icon])}
       [:svg (:svg icon)]])]])

(rum/defc components < rum/cursored rum/reactive
  [open-toolboxes comps]
  [:div#form-components.tool-window
    #_[:div.tool-window-bar
     [:div.tool-window-icon
      icons/window]
     [:span "Components"]
     [:div.tool-window-close
      {:on-click #(swap! open-toolboxes close-toolbox :components)}
      icons/close]]
   [:div.tool-window-content
    (for [tool (sort :order (vals (:components @comps)))]
      [:div.tool-btn.tooltip.tooltip-hover
       {:alt (:text tool)
        :class (when (= (rum/react ws/selected-tool)
                        (:key tool))
                 "selected")
        :key (:key tool)
        :on-click #(ws/toggle-tool! [(:key tool)])} (:icon tool)])]])


#_(rum/defcs element-options < (rum/local :options) rum/cursored
  [state conn page project zoom shapes]
  (let [show-element (:rum/local state)
        show-element-value @show-element
        selected-uuid (:selected @page)
        project-uuid (:uuid @project)
        page-uuid (:uuid @page)
        zoom @zoom
        selected-shape (get @shapes selected-uuid)

        [popup-x popup-y] (shapes/toolbar-coords selected-shape)]
    [:div#element-options.element-options
     {:style #js {:left (* popup-x zoom)
                  :top (* popup-y zoom)}}
     [:ul.element-icons
      (for [menu (shapes/menu-info selected-shape)]
        [:li#e-info
         {:on-click #(do (.stopPropagation %) (reset! show-element (:key menu)))
          :key (str "menu-" (:key menu))
          :class (when (= show-element-value (:key menu))
                   "selected")}
         (:icon menu)])]
     (for [menu (shapes/menu-info selected-shape)]
       [:div#element-basics.element-set
        {:key (:key menu)
         :class (when-not (= show-element-value (:key menu))
                  "hide")}
        [:div.element-set-title (:name menu)]
        [:div.element-set-content
         (for [option (:options menu)]
           [:div {:key (str (:key menu) "-" (:name option))}
            [:span (:name option)]
            [:div.row-flex
             (for [input (:inputs option)]
               (cond
                 (#{:number :text :color} (:type input))
                 [:input#width.input-text
                  {:placeholder (:name input)
                   :key (str (:key menu) "-" (:name option) "-" (:shape-key input))
                   :type (cond
                           (= (:type input) :number) "number"
                           (= (:type input) :text)   "text"
                           (= (:type input) :color)  "text")
                   :value (get selected-shape (:shape-key input))
                   :on-change #(actions/change-shape conn
                                                     selected-uuid
                                                     (:shape-key input)
                                                     ((:value-filter input) (->> % .-target .-value )))}]
                 (= :lock (:type input))
                 [:div.lock-size
                  {:key (str (:key menu) "-" (:name option) "-lock")}
                  icons/lock]))]])]])]))

(rum/defc tools < rum/cursored rum/reactive
  [open-toolboxes]
  [:div#form-tools.tool-window
     [:div.tool-window-bar
     [:div.tool-window-icon
      icons/window]
     [:span "Tools"]
     [:div.tool-window-close
      {:on-click #(swap! open-toolboxes close-toolbox :tools)}
      icons/close]]
    [:div.tool-window-content
     (for [tool (t/sorted-tools (rum/react t/drawing-tools))]
       [:div.tool-btn.tooltip.tooltip-hover
        {:alt (:text tool)
         :class (when (= (rum/react ws/selected-tool) (:key tool))
                  "selected")
         :key (:key tool)
         :on-click #(ws/toggle-tool! [(:key tool)])}
        (:icon tool)])]])

(rum/defc layers
  [conn open-toolboxes page shapes]
  [:div#layers.tool-window
    [:div.tool-window-bar
     [:div.tool-window-icon
      icons/layers]
     [:span "Elements"]
     [:div.tool-window-close
     {:on-click #(swap! open-toolboxes close-toolbox :layers)}
      icons/close]]
    [:div.tool-window-content
     [:ul.element-list
      (for [shape shapes]
        (let [{shape-id :shape/uuid
               selected? :shape/selected?
               locked? :shape/locked?
               visible? :shape/visible?
               raw-shape :shape/data} shape]
          [:li {:key shape-id
                :class (when selected? "selected")}
           [:div.toggle-element
            {:class (when visible? "selected")
             :on-click #(actions/toggle-shape-visibility conn shape-id)} icons/eye]
           [:div.block-element
            {:class (when locked? "selected")
             :on-click #(actions/toggle-shape-lock conn shape-id)} icons/lock]
           [:div.element-icon
            (shapes/icon raw-shape)]
           [:span (:name raw-shape)]]))]]])

(rum/defc toolbar < rum/reactive
  [open-toolboxes]
  (let [toolboxes @open-toolboxes]
    [:div#tool-bar.tool-bar
     [:div.tool-bar-inside
      [:ul.main-tools
       [:li.tooltip
        {:alt "Shapes (Ctrl + Shift + F)"
         :class (when (:tools toolboxes)
                  "current")
         :on-click #(swap! open-toolboxes toggle-toolbox :tools)}
        icons/shapes]
       #_[:li.tooltip
        {:alt "Components (Ctrl + Shift + C)"
         :class (when (:components toolboxes)
                  "current")
         :on-click #(swap! open-toolboxes toggle-toolbox :components)}
        icons/puzzle]
       [:li.tooltip
        {:alt "Icons (Ctrl + Shift + I)"
         :class (when (:icons toolboxes)
                  "current")
         :on-click #(swap! open-toolboxes toggle-toolbox :icons)}
        icons/icon-set]
       [:li.tooltip
        {:alt "Elements (Ctrl + Shift + L)"
         :class (when (:layers toolboxes)
                  "current")
         :on-click #(swap! open-toolboxes toggle-toolbox :layers)}
        icons/layers]
       [:li.tooltip
        {:alt "Feedback (Ctrl + Shift + M)"}
        icons/chat]]]]))

(rum/defcs project-page < (rum/local false :editing?) rum/static
  [{:keys [editing?]} conn page current-page project deletable?]
  (let [{page-uuid :page/uuid
         page-title :page/title} page
         {current-page-uuid :page/uuid} current-page]
    (if @editing?
      [:input.input-text
       {:title "page-title"
        :auto-focus true
        :placeholder "Page title"
        :default-value page-title
        :type "text"
        :on-key-up #(cond
                      (k/enter? %)
                      (when (not (empty? (str/trim (.-value (.-target %)))))
                        (change-page-title conn
                                           page
                                           (str/trim (.-value (.-target %))))
                        (reset! editing? false))

                      (k/esc? %)
                      (reset! editing? false))
        :key page-uuid}]
      [:li.single-page
       {:class (when (= page-uuid current-page-uuid)
                 "current")
        :on-click #(when (not= page-uuid current-page-uuid)
                     (navigate! (workspace-page-route {:project-uuid (:project/uuid project)
                                                       :page-uuid page-uuid})))
        :key page-uuid}
       [:div.tree-icon icons/page]
       [:span page-title]
       [:div.options
        [:div
         {:on-click #(do (.stopPropagation %) (reset! editing? true) %)}
         icons/pencil]
        [:div
         {:class (when-not deletable?
                   "hide")
          :on-click #(do (.stopPropagation %) (delete-page conn page))}
         icons/trash]]])))

(rum/defc project-pages < rum/static
  [conn project current-page pages]
  (let [deletable? (> (count pages) 1)]
    (vec
     (cons :ul.tree-view
           (map #(project-page conn % current-page project deletable?) pages)))))

(rum/defcs new-page < (rum/local {:adding-new? false
                                  :new-page-title ""})
                       rum/static
  [{local-state :rum/local} conn project]
  (let [{:keys [adding-new? new-page-title]} @local-state]
    (if adding-new?
      [:input.input-text
       {:title "page-title"
        :auto-focus true
        :placeholder "Page title"
        :type "text"
        :value new-page-title
        :on-change #(swap! local-state assoc :new-page-title (.-value (.-target %)))
        :on-key-up #(cond
                      (k/enter? %)
                      (when (not (empty? (str/trim new-page-title)))
                        (create-page conn project (str/trim new-page-title))
                        (reset! local-state {:adding-new? false
                                             :new-page-title ""}))
                      (k/esc? %)
                      (reset! local-state {:adding-new? false
                                           :new-page-title ""}))}]
      [:button.btn-primary.btn-small
       {:on-click #(swap! local-state assoc :adding-new? true)}
       "+ Add new page"])))

(rum/defc project-bar < rum/static
  [conn project page pages project-bar-visible?]
  (let [project-name (:project/name project)]
    [:div#project-bar.project-bar
     (when-not project-bar-visible?
       {:class "toggle"})
     [:div.project-bar-inside
      [:span.project-name project-name]
      (project-pages conn project page pages)
      (new-page conn project)]]))

(rum/defc aside < rum/cursored
  [conn open-toolboxes page shapes]
  (let [open-setting-boxes @open-toolboxes]
    [:aside#settings-bar.settings-bar
     [:div.settings-bar-inside
      (when (:tools open-setting-boxes)
        (tools open-toolboxes))

      (when (:icons open-setting-boxes)
        (icon-sets open-toolboxes))

      #_(when (:components open-setting-boxes)
        (components open-toolboxes components))

      (when (:layers open-setting-boxes)
        (layers conn open-toolboxes page shapes))]]))

(rum/defc vertical-rule < rum/reactive rum/static
  [zoom]
  (let [height viewport-height
        start-height document-start-y
        top (rum/react ws/scroll-top)
        padding 20
        big-ticks-mod (/ 100 zoom)
        mid-ticks-mod (/ 50 zoom)
        step-size 10
        ticks (concat (range (- padding start-height) 0 step-size) (range 0 (- height start-height padding) step-size))

        lines (fn
                [position value padding]
                (cond
                  (< (mod value big-ticks-mod) step-size)
                    [:g {:key position}
                     [:line {:y1 position :y2 position :x1 5 :x2 padding :stroke "#7f7f7f"}]
                     [:text {:y position :x 5 :transform (str/format "rotate(90 0 %s)" position) :fill "#7f7f7f" :style #js {:fontSize "12px"}} value]]
                  (< (mod value mid-ticks-mod) step-size)
                    [:line {:key position :y1 position :y2 position :x1 10 :x2 padding :stroke "#7f7f7f"}]
                  :else
                    [:line {:key position :y1 position :y2 position :x1 15 :x2 padding :stroke "#7f7f7f"}]))]
   [:svg.vertical-rule
    {:width 3000
     :height 3000
     :style {:top (str (- top) "px")}}
    [:g
     [:rect {:x 0 :y padding :height height :width padding :fill "#bab7b7"}]
     (map #(lines (* (+ %1 start-height) zoom) %1 padding) ticks)]]))

(rum/defc horizontal-rule < rum/reactive rum/static
  [zoom]
  (let [left (rum/react ws/scroll-left)
        width viewport-width
        start-width document-start-x
        padding 20
        big-ticks-mod (/ 100 zoom)
        mid-ticks-mod (/ 50 zoom)
        step-size 10
        ticks (concat (range (- padding start-width) 0 step-size) (range 0 (- width start-width padding) step-size))
        lines (fn
                [position value padding]
                (cond
                  (< (mod value big-ticks-mod) step-size)
                    [:g {:key position}
                     [:line {:x1 position :x2 position :y1 5 :y2 padding :stroke "#7f7f7f"}]
                     [:text {:x (+ position 2) :y 13 :fill "#7f7f7f" :style #js {:fontSize "12px"}} value]]
                  (< (mod value mid-ticks-mod) step-size)
                    [:line {:key position :x1 position :x2 position :y1 10 :y2 padding :stroke "#7f7f7f"}]
                  :else
                  [:line {:key position :x1 position :x2 position :y1 15 :y2 padding :stroke "#7f7f7f"}]))]
    [:svg.horizontal-rule
      {:width 3000
       :height 3000
       :style {:left (str (- (- left 50)) "px")}}
      [:g
       [:rect {:x padding
               :y 0
               :width width
               :height padding
               :fill "#bab7b7"}]
       [:rect {:x 0
               :y 0
               :width padding
               :height padding
               :fill "#bab7b7"}]
       (map #(lines (* (+ %1 start-width) zoom) %1 padding) ticks)]]))

(rum/defc viewport < rum/static
  [conn page shapes zoom grid?]
  [:svg#viewport
   {:width viewport-height
    :height viewport-width}
   [:g.zoom
    {:transform (str "scale(" zoom ", " zoom ")")}
    (canvas conn
            page
            shapes
            {:viewport-height viewport-height
             :viewport-width viewport-width
             :document-start-x document-start-x
             :document-start-y document-start-y})
    (when grid?
      (grid viewport-width
            viewport-height
            document-start-x
            document-start-y
            zoom))]])

(rum/defc working-area < rum/static
  [conn
   open-setting-boxes
   page
   project
   shapes
   zoom
   grid?]
  [:section.workspace-canvas
    {:class (when (empty? open-setting-boxes)
              "no-tool-bar")
     :on-scroll ws/on-workspace-scroll}
    #_(when (:selected page)
      (element-options conn
                       page-cursor
                       project-cursor
                       zoom-cursor
                       shapes-cursor))
    (debug-coordinates)
    (viewport conn page shapes zoom grid?)])

;; TODO: reset local state when project changes!
(rum/defcs workspace < (rum/local {:open-toolboxes #{:tools :layers}
                                   :grid? false
                                   :zoom 1
                                   :project-bar-visible? false})
                       rum/cursored-watch
  [{local-state :rum/local} conn [project-uuid page-uuid]]
  (let [page-uuid (or page-uuid (q/first-page-id-by-project-id project-uuid @conn))

        open-toolboxes (rum/cursor local-state [:open-toolboxes])
        grid? (rum/cursor local-state [:grid?])
        zoom (rum/cursor local-state [:zoom]) ;; FIXME
        project-bar-visible? (rum/cursor local-state [:project-bar-visible?])

        project (q/pull-project-by-id project-uuid @conn)
        pages (q/pull-pages-by-project-id project-uuid @conn)
        page (q/pull-page-by-id page-uuid @conn)
        shapes (sq/pull-shapes-by-page-id page-uuid @conn)]
    [:div
     (header conn page grid? project-bar-visible?)
     [:main.main-content
      [:section.workspace-content
       ;; Toolbar
       (toolbar open-toolboxes)
       ;; Project bar
       (project-bar conn project page pages @project-bar-visible?)
       ;; Rules
       (horizontal-rule @zoom)
       (vertical-rule @zoom)
       ;; Working area
       (working-area conn @open-toolboxes page project shapes @zoom @grid?)
       ;; Aside
       (when-not (empty? @open-toolboxes)
         (aside conn open-toolboxes page shapes))]]]))
