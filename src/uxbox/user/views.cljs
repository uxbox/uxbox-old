(ns uxbox.user.views
  (:require rum
            [uxbox.user.data :as data]
            [uxbox.icons :refer [logo]]
            [uxbox.icons :as icons]
            [uxbox.navigation :as nav :refer [link navigate!]]))

(rum/defc user-menu < rum/static
  [open?]
  [:ul.dropdown {:class (when-not open?
                          "hide")}
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
       [:span "Save & Exit"]]])

(rum/defcs user < (rum/local false :menu-open?) rum/reactive
  [{:keys [menu-open?]}]
  (let [usr (rum/react data/user)]
    [:div.user-zone {:on-mouse-enter #(reset! menu-open? true)
                     :on-mouse-leave #(reset! menu-open? false)}
     [:span (:fullname usr)]
     [:img {:border "0"
            :src (:avatar usr)}]
     (user-menu @menu-open?)]))

(rum/defc register-form < rum/static
  []
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
    {:name "password"
     :placeholder "Password"
     :type "password"}]
   [:input.btn-primary
    {:name "login"
     :value "Continue"
     :type "submit"
     :on-click #(navigate! (nav/dashboard-route))}]
   [:div.login-links
    [:a
     {:on-click #(navigate! (nav/login-route))}
     "You already have an account?"]]])

(rum/defc register < rum/static
  []
  [:div.login
   [:div.login-body
    [:a logo]
    (register-form)]])

(rum/defc recover-password-form < rum/static
  []
  [:div.login-content
   [:input.input-text
     {:name "email"
      :placeholder "Email"
      :type "email"}]
   [:input.btn-primary
    {:name "login"
     :value "Continue"
     :type "submit"
     :on-click #(navigate! (nav/dashboard-route))}]
   [:div.login-links
    [:a
     {:on-click #(navigate! (nav/login-route))}
     "You have rememered your password?"]
    [:a
     {:on-click #(navigate! (nav/register-route))}
     "Don't have an account?"]]])

(rum/defc recover-password < rum/static
  []
  [:div.login
    [:div.login-body
     [:a logo]
     (recover-password-form)]])

(rum/defc login-form < rum/static
  []
  [:div.login-content
   [:input.input-text
     {:name "email"
      :placeholder "Email"
      :type "email"}]
   [:input.input-text
    {:name "password"
     :placeholder "Password"
     :type "password"}]
   [:div.input-checkbox.check-primary
    [:input#checkbox1 {:value "1"
                       :type "checkbox"}]
    [:label {:for "checkbox1"} "Keep Me Signed in"]]
   [:input.btn-primary
    {:name "login"
     :value "Continue"
     :type "submit"
     :on-click #(navigate! (nav/dashboard-route))}]
   [:div.login-links
    [:a {:on-click #(navigate! (nav/recover-password-route))} "Forgot your password?"]
    [:a {:on-click #(navigate! (nav/register-route))} "Don't have an account?"]]])

(rum/defc login
  []
  [:div.login
    [:div.login-body
     [:a logo]
     (login-form)]])
