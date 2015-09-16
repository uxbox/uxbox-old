(ns uxbox.ui.lightbox
  (:require
   rum
   [uxbox.keyboard :as k]
   [goog.events :as events])
  (:import goog.events.EventType))

;; ================================================================================
;; State

(defonce current-lightbox (atom nil))

(defn set-lightbox!
  [kind]
  (reset! current-lightbox kind))

(defn close-lightbox!
  []
  (reset! current-lightbox nil))

;; ================================================================================
;; UI

(defmulti render-lightbox (fn [lightbox conn] lightbox))
(defmethod render-lightbox :default [_] nil)

(defn close-lightbox-on-esc
  [e]
  (when (k/esc? e)
    (close-lightbox!)))

(def ^{:doc "A mixin for closing the lightbox when pressing the ESC key."}
  dismiss-on-esc
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
  [conn]
  (let [lb (rum/react current-lightbox)]
    [:div.lightbox
     {:class (when (nil? lb)
               "hide")}
     (render-lightbox lb conn)]))
