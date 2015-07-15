(ns uxbox.user.views
  (:require [uxbox.icons :refer [logo]]
            [uxbox.icons :as icons]
            [uxbox.navigation :refer [link navigate! dashboard-route]]))

(defn user
  [db]
  (let [usr (:user @db)]
    [:div.user-zone {:on-mouse-enter #(swap! db assoc :open-user-menu true) :on-mouse-leave #(swap! db assoc :open-user-menu false)}
     [:span (:fullname usr)]
     [:img {:border "0", :src (:avatar usr)}]
     [:ul.dropdown {:class (if (:open-user-menu @db) "" "hide")}
      [:li
       icons/page
       [:span "Page settings"]]
      [:li
       icons/grid
       [:span "Grid settings"]]
      [:li
       icons/eye
       [:span "Preview"]]
      [:li
       icons/user
       [:span "Your account"]]
      [:li
       icons/exit
       [:span "Save & Exit"]]
      ]]))

(defn register
  []
  [:div.login
    [:div.login-body
     [link "/" logo]
     [:div.login-content
      [:input.input-text
        {:name "name"
         :placeholder "Name"
         :type "text"}]
      [:input.input-text
        {:name "email"
         :placeholder "Email"
         :type "email"}]
      [:input.input-text
       {:name "password", :placeholder "Password", :type "password"}]
      [:input.btn-primary
       {:name "login", :value "Continue", :type "submit", :on-click #(navigate! (dashboard-route))}]
      [:div.login-links
       [link "/" "You already have an account?"]]]]])

(defn recover-password
  []
  [:div.login
    [:div.login-body
     [link "/" logo]
     [:div.login-content
      [:input.input-text
        {:name "email"
         :placeholder "Email"
         :type "email"}]
      [:input.btn-primary
       {:name "login", :value "Continue", :type "submit", :on-click #(navigate! (dashboard-route))}]
      [:div.login-links
       [link "/" "Login"]
       [link "/register" "Don't have an account?"]]]]])

(defn login
  []
  [:div.login
    [:div.login-body
     logo
     [:div.login-content
      [:input.input-text
        {:name "email"
         :placeholder "Email"
         :type "email"}]
      [:input.input-text
       {:name "password", :placeholder "Password", :type "password"}]
      [:div.input-checkbox.check-primary
       [:input#checkbox1 {:value "1", :type "checkbox"}]
       [:label {:for "checkbox1"} "Keep Me Signed in"]]
      [:input.btn-primary
       {:name "login", :value "Continue", :type "submit", :on-click #(navigate! (dashboard-route))}]
      [:div.login-links
       [link "/recover-password" "Forgot your password?"]
       [link "/register" "Don't have an account?"]]]]])
