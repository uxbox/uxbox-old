(ns uxbox.workspace.views
  (:require [uxbox.user.views :refer [user]]
            [uxbox.workspace.icons :as icons]))


(defn header
  [db]
  [:header#workspace-bar.workspace-bar
    [:div.main-icon icons/logo]
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
     [:div.tool-window-close
      icons/close]]
    [:div.tool-window-content
     [:div.tool-btn.selected
      icons/box]
     [:div.tool-btn
      icons/circle]
     [:div.tool-btn
      icons/line]
     [:div.tool-btn
      icons/curve]
     [:div.tool-btn
      icons/text]
     [:div.tool-btn
      icons/arrow]]])

(defn layers
  [db]
  [:div#layers.tool-window
    [:div.tool-window-bar
     [:div.tool-window-icon
      icons/layers]
     [:span "Elements"]
     [:div.tool-window-close
      icons/close]]
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
  []
  [:div#tool-bar.tool-bar
    [:div.tool-bar-inside
     [:ul.main-tools
      [:li.current
       icons/shapes]
      [:li
       icons/puzzle]
      [:li
       icons/cluster]
      [:li
       icons/layers]
      [:li
       icons/chat]]]])

(defn settings
  [db]
  [:aside#settings-bar.settings-bar
    [:div.settings-bar-inside
     [tools db]
     [layers db]]])

(defn workspace
  [db]
  [:div
   [header db]
   [:main.main-content
    [:section.workspace-content
     [toolbar db]
     [:section.dashboard-grid [:div.dashboard-grid-content]]]
    [settings db]]])
