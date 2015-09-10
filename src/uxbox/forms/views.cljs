(ns uxbox.forms.views
  (:require
   rum
   [uxbox.keyboard :as k]
   [uxbox.forms.data :as data]
   [goog.events :as events])
  (:import goog.events.EventType))

(defmulti render-lightbox identity)
(defmethod render-lightbox :default [_] nil)

(defn close-lightbox-on-esc
  [e]
  (when (k/esc? e)
    (data/close-lightbox!)))

(def dismiss-on-esc
  {:will-mount (fn [state]
                 (events/listen js/document
                                EventType.KEYDOWN
                                close-lightbox-on-esc)
                 state)
   :will-unmount (fn [state]
                 (events/unlisten js/document
                                  EventType.KEYDOWN
                                  close-lightbox-on-esc)
                   state)})

(rum/defc lightbox < rum/reactive dismiss-on-esc
  []
  (let [kind (rum/react data/lightbox)]
    [:div.lightbox
     {:class (when (nil? kind)
               "hide")}
     (render-lightbox kind)]))
