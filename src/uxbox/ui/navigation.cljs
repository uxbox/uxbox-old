(ns uxbox.ui.navigation
  (:require
   [bidi.bidi :as bidi]
   [goog.events :as events])
  (:import [goog.history Html5History]
            goog.history.EventType))

;; Location

(defonce location (atom [:login]))

;; Routes

(def ^:private project-route [[bidi/uuid :project-uuid]])
(def ^:private page-route    [[bidi/uuid :project-uuid] "/" [bidi/uuid :page-uuid]])

(def routes
  ["/" {;; User
        ""                 :login
        "register"         :register
        "recover-password" :recover-password

        ;; Dashboard
        "dashboard"        :dashboard

        ;; Workspace
        "workspace/"
          {project-route   :project
           page-route      :page}}])


;; History

(def history
  (doto (Html5History.)
    (.setUseFragment false)
    (.setPathPrefix "")))

;; low-level

(defn set-uri!
  "Set the given uri as the current one, doesn't reload the page."
  [uri]
  (.setToken history uri))

;; Route resolution

(defn route-for
  "Given a location handler and optional parameter map, return the URI
  for such handler and parameters."
  ([location]
   (bidi/path-for routes location))
  ([location params]
   (apply bidi/path-for routes location (into []
                                              (mapcat (fn [[k v]] [k v]))
                                              params))))

;; Navigation

(defn dispatch!
  "Given a URI, match agains the routes and modify the location
  accordingly."
  [uri]
  (if-let [match (bidi/match-route routes uri)]
    (let [handler (:handler match)
          params (:route-params match)]
      (reset! location [handler params]))
    ;; TODO
    404))

(defn- navigate
  "Handle navigation event."
  [u]
  (let [token (.-token u)]
    (dispatch! token)))

(defn navigate!
  "Given a location and optional parameters, resolve the route for such
  location and paramaters and navigate to the URI."
  ([location]
   (navigate! location {}))
  ([location params]
   (if-let [uri (route-for location params)]
     (set-uri! uri)
     ;; TODO
     404)))

;; Components

(defn link
  "Given an href and a component, return a link component that will navigate
  to the given URI withour reloading the page."
  [href component]
  [:a
   {:href href
    :on-click #(do (.preventDefault %) (set-uri! href))}
   component])

;; Bootstrap

(defn start-history!
  "Start listening for history events and affecting the location atom accordingly."
  []
  (events/listen history EventType.NAVIGATE navigate)
  (.setEnabled history true)
  (dispatch! js/window.location.href))
