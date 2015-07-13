(ns uxbox.workspace.views
  (:require [uxbox.user.views :refer [user]]
            [uxbox.icons :refer [chat close]]
            [uxbox.navigation :refer [link]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.icons :as icons]))

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
      [:li
       icons/grid]
      [:li
       icons/alignment]
      [:li
       icons/organize]]]
   [user (:user @db)]])

(defn tools
  [db]
  [:div#form-tools.tool-window
    [:div.tool-window-bar
     [:div.tool-window-icon
      icons/window]
     [:span "Tools"]
     [:div.tool-window-close {:on-click #(actions/close-setting-box :tools)}
      close]]
    [:div.tool-window-content
     (if (= (:selected-tool (:workspace @db)) :rect)
       [:div.tool-btn.selected icons/box]
       [:div.tool-btn {:on-click #(actions/set-tool :rect)} icons/box])
     (if (= (:selected-tool (:workspace @db)) :circle)
       [:div.tool-btn.selected icons/circle]
       [:div.tool-btn {:on-click #(actions/set-tool :circle)} icons/circle])
     (if (= (:selected-tool (:workspace @db)) :line)
       [:div.tool-btn.selected icons/line]
       [:div.tool-btn {:on-click #(actions/set-tool :line)} icons/line])
     (if (= (:selected-tool (:workspace @db)) :curve)
       [:div.tool-btn.selected icons/curve]
       [:div.tool-btn {:on-click #(actions/set-tool :curve)} icons/curve])
     (if (= (:selected-tool (:workspace @db)) :text)
       [:div.tool-btn.selected icons/text]
       [:div.tool-btn {:on-click #(actions/set-tool :text)} icons/text])
     (if (= (:selected-tool (:workspace @db)) :arrow)
       [:div.tool-btn.selected icons/arrow]
       [:div.tool-btn {:on-click #(actions/set-tool :arrow)} icons/arrow])]])

(defn layers
  [db]
  [:div#layers.tool-window
    [:div.tool-window-bar
     [:div.tool-window-icon
      icons/layers]
     [:span "Elements"]
     [:div.tool-window-close {:on-click #(actions/close-setting-box :layers)}
      close]]
    [:div.tool-window-content
     [:ul.element-list
      [:li.selected
       [:div.toggle-element
        icons/eye]
       [:div.block-element
        icons/lock]
       [:div.element-icon
        icons/box]
       [:span "Box 1"]]
      [:li
       [:div.toggle-element.selected
        icons/eye]
       [:div.block-element.selected
        icons/lock]
       [:div.element-icon
        icons/circle]
       [:span "Circle 1"]]
      [:li
       [:div.toggle-element
        icons/eye]
       [:div.block-element.selected
        icons/lock]
       [:div.element-icon
        icons/line]
       [:span "Line 1"]]
      [:li
       [:div.toggle-element
        icons/eye]
       [:div.block-element
        icons/lock]
       [:div.element-icon
        icons/box]
       [:span "Box 2"]]]]])

(defn toolbar
  [db]
  [:div#tool-bar.tool-bar
    [:div.tool-bar-inside
     [:ul.main-tools
      (if (:tools (:open-setting-boxes @db))
        [:li.current {:on-click #(actions/close-setting-box :tools)} icons/shapes]
        [:li {:on-click #(actions/open-setting-box :tools)} icons/shapes])
      [:li
       icons/puzzle]
      [:li
       icons/icon-set]
      (if (:layers (:open-setting-boxes @db))
        [:li.current {:on-click #(actions/close-setting-box :layers)} icons/layers]
        [:li {:on-click #(actions/open-setting-box :layers)} icons/layers])
      [:li
       icons/layers]
      [:li
       chat]]]])

(defn settings
  [db]
  [:aside#settings-bar.settings-bar
    [:div.settings-bar-inside
     (if (:tools (:open-setting-boxes @db))
      [tools db])
     (if (:layers (:open-setting-boxes @db))
      [layers db])]])

(defn workspace
  [db]
  [:div
   [header db]
   [:main.main-content
    [:section.workspace-content
     [toolbar db]
     [:section.dashboard-grid [:div.dashboard-grid-content]]]
    [settings db]]])
