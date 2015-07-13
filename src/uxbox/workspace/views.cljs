(ns uxbox.workspace.views
  (:require [uxbox.user.views :refer [user]]
            [uxbox.icons :refer [chat close page folder trash pencil]]
            [uxbox.navigation :refer [link]]
            [uxbox.workspace.actions :as actions]
            [uxbox.workspace.icons :as icons]
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
      [:li
       icons/grid]
      [:li
       icons/alignment]
      [:li
       icons/organize]]]
   [user (:user @db)]])

(defn figures
  [db]
  [:div#form-figures.tool-window
    [:div.tool-window-bar
     [:div.tool-window-icon
      icons/window]
     [:span "Figures"]
     [:div.tool-window-close {:on-click #(actions/close-setting-box :figures)}
      close]]])

(defn components
  [db]
  [:div#form-components.tool-window
    [:div.tool-window-bar
     [:div.tool-window-icon
      icons/window]
     [:span "Components"]
     [:div.tool-window-close {:on-click #(actions/close-setting-box :components)}
      close]]])

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

(defn projectbar
  [db]
  [:div#project-bar.project-bar
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
     [:section.workspace-canvas
      [canvas db]]]
    (if (not (empty? (:open-setting-boxes @db)))
     [settings db])]])
