(ns uxbox.workspace.views
  (:require [reagent.core :as reagent :refer [atom]]
            [cuerdas.core :as str]
            [uxbox.user.views :refer [user]]
            [uxbox.icons :as icons]
            [uxbox.navigation :refer [link workspace-page-route navigate!]]
            [uxbox.projects.actions :refer [create-simple-page change-page-title delete-page]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.canvas.views :refer [canvas]]
            [uxbox.geometry :as geo]
            [uxbox.shapes.core :as shapes]
            [uxbox.pubsub :as pubsub]))


(defn project-tree
  [db]
  (let [title (get-in @db [:page :title])]
    [:div.project-tree-btn
     {:on-click #(swap! db update :project-bar-visible? not)}
     icons/project-tree
     [:span title]]))

(defn header
  [db]
  [:header#workspace-bar.workspace-bar
    [:div.main-icon
     [link "/dashboard"
      icons/logo-icon]]
    [project-tree db]
    [:div.workspace-options
     [:ul.options-btn
      [:li.tooltip.tooltip-bottom {:alt "Undo (Ctrl + Z)"}
       icons/undo]
      [:li.tooltip.tooltip-bottom {:alt "Redo (Ctrl + Shift + Z)"}
       icons/redo]]
     [:ul.options-btn
      [:li.tooltip.tooltip-bottom {:alt "Export (Ctrl + E)"}
       [:a {:download (str (get-in @db [:page :title]) ".svg")
            :href "#"
            :on-click (fn [e]
                        (let [innerHTML (.-innerHTML (.getElementById js/document "page-layout"))
                              width (get-in @db [:page :width])
                              height (get-in @db [:page :height])
                              html (str "<svg width='" width  "' height='" height  "'>" innerHTML "</svg>")
                              data (js/Blob. #js [html] #js {:type "application/octet-stream"})
                              url (.createObjectURL (.-URL js/window) data)]
                          (set! (.-href (.-currentTarget e)) url) ))}
        icons/export]]
      [:li.tooltip.tooltip-bottom {:alt "Image (Ctrl + I)"}
       icons/image]]
     [:ul.options-btn
      [:li.tooltip.tooltip-bottom {:alt "Ruler (Ctrl + R)"}
       icons/ruler]
      [:li.tooltip.tooltip-bottom
       {:alt "Grid (Ctrl + G)"
        :class (when (:grid? (:workspace @db))
                 "selected")
        :on-click #(actions/toggle-grid)}
       icons/grid]
      [:li.tooltip.tooltip-bottom {:alt "Align (Ctrl + A)"}
       icons/alignment]
      [:li.tooltip.tooltip-bottom {:alt "Organize (Ctrl + O)"}
       icons/organize]]]
   [user db]])

(defn icons-sets
  [db]
  (let [{:keys [workspace current-icons-set components]} @db]
   [:div#form-figures.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/window]
      [:span "Figures"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :icons-sets)}
       icons/close]]
     [:div.tool-window-content
      [:div.figures-catalog
       [:select.input-select.small {:on-change #(actions/set-icons-set (keyword (.-value (.-target %))))}
        (for [[icons-set-key icons-set] (seq (:icons-sets components))]
          [:option {:key icons-set-key :value icons-set-key} (:name icons-set)])]]
      (for [[icon-key icon] (seq (get-in components [:icons-sets current-icons-set :icons]))]
        [:div.figure-btn {:key icon-key
                          :class (if (= (:selected-tool workspace) [:icon current-icons-set icon-key]) "selected" "")
                          :on-click #(actions/set-tool [:icon current-icons-set icon-key])}
          [:svg (:svg icon)]])]]))

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
      (for [tool (sort :order (vals (get-in @db [:components :components])))]
        [:div.tool-btn.tooltip.tooltip-hover {:alt (:text tool) :class (if (= (:selected-tool workspace) (:key tool)) "selected" "")
                                              :key (:key tool) :on-click #(actions/set-tool (:key tool))} (:icon tool)])]]))

(defn elementoptions
  [db]
  (let [show-element (atom :options)]
    (fn []
      (let [selected-uuid (get-in @db [:page :selected])
            project-uuid (get-in @db [:project :uuid])
            page-uuid (get-in @db [:page :uuid])
            zoom (get-in @db [:workspace :zoom])
            selected-shape (get-in @db [:shapes selected-uuid])
            [popup-x popup-y] (shapes/toolbar-coords selected-shape)
            show-element-value @show-element]
        [:div#element-options.element-options
         {:style #js {:left (* popup-x zoom) :top (* popup-y zoom)}}
         [:ul.element-icons
          (for [menu (shapes/menu-info selected-shape)]
            [:li#e-info {:on-click (fn [e] (reset! show-element (:key menu)))
                         :key (str "menu-" (:key menu))
                         :class (when (= show-element-value (:key menu)) "selected")} (:icon menu)])]
         (for [menu (shapes/menu-info selected-shape)]
           [:div#element-basics.element-set
            {:key (:key menu)
             :class (when (not (= show-element-value (:key menu))) "hide")}
            [:div.element-set-title (:name menu)]
            [:div.element-set-content
             (for [option (:options menu)]
               [:div {:key (str (:key menu) "-" (:name option))}
                [:span (:name option)]
                [:div.row-flex
                 (for [input (:inputs option)]
                   (cond
                     (or (= :number (:type input)) (= :text (:type input)) (= :color (:type input)))
                       [:input#width.input-text
                        {:placeholder (:name input)
                         :key (str (:key menu) "-" (:name option) "-" (:shape-key input))
                         :type (cond (= (:type input) :number) "number" (= (:type input) :text) "text" (= (:type input) :color) "text")
                         :value (get selected-shape (:shape-key input))
                         :on-change #(actions/change-shape-attr project-uuid page-uuid selected-uuid (:shape-key input) ((:value-filter input) (->> % .-target .-value )))}]
                     (= :lock (:type input))
                       [:div.lock-size
                        {:key (str (:key menu) "-" (:name option) "-lock")}
                          icons/lock]))]])]])]))))

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
      (for [tool (sort :order (vals (get-in @db [:components :tools])))]
        [:div.tool-btn.tooltip.tooltip-hover {:alt (:text tool) :class (if (= (:selected-tool workspace) (:key tool)) "selected" "")
                                              :key (:key tool) :on-click #(actions/set-tool (:key tool))} (:icon tool)])]]))

(defn layers
  [db]
  (let [{:keys [page workspace groups]} @db]
   [:div#layers.tool-window
     [:div.tool-window-bar
      [:div.tool-window-icon
       icons/layers]
      [:span "Elements"]
      [:div.tool-window-close {:on-click #(actions/close-setting-box :layers)}
       icons/close]]
     [:div.tool-window-content
      [:ul.element-list
       (for [[group-id group] (sort-by #(:order (second %)) (seq groups))]
           [:li {:key group-id
                 :class (if (contains? (:selected-groups workspace) group-id) "selected" "")}
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
         ]]]))

(defn toolbar
  [db]
  [:div#tool-bar.tool-bar
    [:div.tool-bar-inside
     [:ul.main-tools
      [:li.tooltip
       {:alt "Shapes (Ctrl + Shift + F)"
        :class (when (:tools (:open-setting-boxes @db))
                 "current")
        :on-click #(actions/toggle-setting-box :tools)}
       icons/shapes]
      [:li.tooltip
       {:alt "Components (Ctrl + Shift + C)"
        :class (when (:components (:open-setting-boxes @db))
                 "current")
        :on-click #(actions/toggle-setting-box :components)}
       icons/puzzle]
      [:li.tooltip
       {:alt "Icons (Ctrl + Shift + I)"
        :class (when (:icons (:open-setting-boxes @db))
                 "current")
        :on-click #(actions/toggle-setting-box :icons)}
       icons/icon-set]
      [:li.tooltip
       {:alt "Elements (Ctrl + Shift + L)"
        :class (when (:layers (:open-setting-boxes @db))
                 "current")
        :on-click #(actions/toggle-setting-box :layers)}
       icons/layers]
      [:li.tooltip
       {:alt "Feedback (Ctrl + Shift + M)"}
       icons/chat]]]])

(defn project-pages
  [db pages]
  (let [current-page (:page @db)
        current-project (:project @db)
        editing-pages (:editing-pages @db)]
    [:ul.tree-view
      (for [page (vals pages)]
        (if (contains? editing-pages (:uuid page))
          [:input.input-text
           {:title "page-title"
            :auto-focus true
            :placeholder "Page title"
            :type "text"
            :value (get editing-pages (:uuid page))
            :on-change #(swap! db assoc-in [:editing-pages (:uuid page)] (.-value (.-target %)))
            :on-key-up #(cond
                          (= (.-keyCode %) 13)
                            (when (not (empty? (str/trim (get editing-pages (:uuid page)))))
                              (change-page-title current-project page (get editing-pages (:uuid page)))
                              (swap! db update :editing-pages dissoc (:uuid page)))
                          (= (.-keyCode %) 27)
                            (swap! db update :editing-pages dissoc (:uuid page)))
            :key (:uuid page)}]

          [:li.single-page
           {:class (when (= (:uuid page) (:uuid current-page)) "current")
            :on-click #(when (not= (:uuid page) (:uuid current-page))
                         (navigate! (workspace-page-route {:project-uuid (:uuid current-project) :page-uuid (:uuid page)})))
            :key (:uuid page)}
           [:div.tree-icon icons/page]
           [:span (:title page)]
           [:div.options
            [:div
             {:on-click #(do (.stopPropagation %) (swap! db assoc-in [:editing-pages (:uuid page)] (:title page)))}
             icons/pencil]
            [:div {:on-click #(do (.stopPropagation %) (delete-page current-project page))} icons/trash]]]))]))

(defn clean-new-page!
  [db]
  (swap! db assoc :adding-new-page? false
                  :new-page-title ""))

(defn new-page
  [db project]
  (if (:adding-new-page? @db)
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
                      (create-simple-page project (:new-page-title @db))
                      (clean-new-page! db))
                    (= (.-keyCode %) 27)
                    (clean-new-page! db))}]
    [:button.btn-primary.btn-small
     {:on-click #(swap! db assoc :adding-new-page? true)}
     "+ Add new page"]))

(defn projectbar
  [db]
  (let [project (:project @db)
        project-name (:name project)
        pages (:project-pages @db)]
    [:div#project-bar.project-bar
     (when-not (:project-bar-visible? @db)
       {:class "toggle"})
     [:div.project-bar-inside
      [:span.project-name project-name]
      [project-pages db pages]
      [new-page db project]]]))

(defn settings
  [db]
  [:aside#settings-bar.settings-bar
    [:div.settings-bar-inside
     (if (:tools (:open-setting-boxes @db))
      [tools db])
     (if (:icons (:open-setting-boxes @db))
      [icons-sets db])
     (if (:components (:open-setting-boxes @db))
      [components db])
     (if (:layers (:open-setting-boxes @db))
      [layers db])]])

(defn vertical-rule
  [db height start-height zoom]
  (let [padding 20
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
                    [:line {:key position :y1 position :y2 position :x1 15 :x2 padding :stroke "#7f7f7f"}]))
        ]
    [:g
     [:rect {:x 0 :y padding :height height :width padding :fill "#bab7b7"}]
     (map #(lines (* (+ %1 start-height) zoom) %1 padding) ticks)]))

(defn horizontal-rule
  [db width start-width zoom]
  (let [padding 20
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
                    [:line {:key position :x1 position :x2 position :y1 15 :y2 padding :stroke "#7f7f7f"}]))
        ]
    [:g
     [:rect {:x padding :y 0 :width width :height padding :fill "#bab7b7"}]
     [:rect {:x 0 :y 0 :width padding :height padding :fill "#bab7b7"}]
     (map #(lines (* (+ %1 start-width) zoom) %1 padding) ticks)]))

(defn workspace
  [db]
  (let [zoom (get-in @db [:workspace :zoom])
        on-event (fn [event-type]
         (fn [e]
           (pubsub/publish! [event-type {:top (.-scrollTop (.-target e)) :left (.-scrollLeft (.-target e))}])
           (.preventDefault e)))]
    [:div
     [header db]
     [:main.main-content
      [:section.workspace-content
       [toolbar db]
       [projectbar db]
       [:svg.horizontal-rule {:width 3000 :height 3000 :style {:left (str (- (- (get-in @db [:scroll :left]) 50)) "px")}}
        [horizontal-rule db 3000 50 zoom]]
       [:svg.vertical-rule {:width 3000 :height 3000 :style {:top (str (- (get-in @db [:scroll :top])) "px")}}
        [vertical-rule db 3000 50 zoom]]
       [:section.workspace-canvas {:class (if (empty? (:open-setting-boxes @db)) "no-tool-bar" "")  :on-scroll (on-event :viewport-scroll)}
        (when (get-in @db [:page :selected])
          [elementoptions db])
        [canvas db]]]
      (if (not (empty? (:open-setting-boxes @db)))
       [settings db])]]))
