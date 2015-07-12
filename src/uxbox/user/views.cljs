(ns uxbox.user.views
  (:require [uxbox.icons :refer [logo]]
            [uxbox.navigation :refer [link navigate! dashboard-route]]))


(defn user
  [usr]
  [:div.user-zone
   [:span (:fullname usr)]
   [:img {:border "0", :src (:avatar usr)}]])

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
       [link "" "Forgot your password?"]
       [link "" "Don't have an account?"]]]]])
