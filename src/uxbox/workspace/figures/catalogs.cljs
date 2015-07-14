(ns uxbox.workspace.figures.catalogs
  (:require [uxbox.workspace.figures.material-design-actions :refer [material-design-actions]]
            [uxbox.workspace.figures.material-design-alert :refer [material-design-alert]]
            [uxbox.workspace.figures.material-design-av :refer [material-design-av]]
            [uxbox.workspace.figures.material-design-communication :refer [material-design-communication]]
            [uxbox.workspace.figures.material-design-content :refer [material-design-content]]
            [uxbox.workspace.figures.material-design-device :refer [material-design-device]]
            [uxbox.workspace.figures.material-design-editor :refer [material-design-editor]]
            [uxbox.workspace.figures.material-design-file :refer [material-design-file]]
            [uxbox.workspace.figures.material-design-hardware :refer [material-design-hardware]]
            [uxbox.workspace.figures.material-design-image :refer [material-design-image]]
            [uxbox.workspace.figures.material-design-maps :refer [material-design-maps]]
            [uxbox.workspace.figures.material-design-navigation :refer [material-design-navigation]]
            [uxbox.workspace.figures.material-design-notification :refer [material-design-notification]]
            [uxbox.workspace.figures.material-design-social :refer [material-design-social]]
            [uxbox.workspace.figures.material-design-toggle :refer [material-design-toggle]]))

(def catalogs (sorted-map
  :material-design-actions material-design-actions
  :material-design-alert material-design-alert
  :material-design-av material-design-av
  :material-design-communication material-design-communication
  :material-design-content material-design-content
  :material-design-device material-design-device
  :material-design-editor material-design-editor
  :material-design-file material-design-file
  :material-design-hardware material-design-hardware
  :material-design-image material-design-image
  :material-design-maps material-design-maps
  :material-design-navigation material-design-navigation
  :material-design-notification material-design-notification
  :material-design-social material-design-social
  :material-design-toggle material-design-toggle
))
