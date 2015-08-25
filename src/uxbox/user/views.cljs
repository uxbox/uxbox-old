(ns uxbox.user.views
  (:require rum
            [uxbox.icons :refer [logo]]
            [uxbox.icons :as icons]
            [uxbox.navigation :refer [link navigate! dashboard-route]]))

(defn- open-user-menu
  [state]
  (assoc state :user-menu-open? true))

(defn- close-user-menu
  [state]
  (assoc state :user-menu-open? false))

(defn- user-menu-open?
  [state]
  (:user-menu-open? state))

(rum/defc user
  [db]
  (let [usr (:user @db)]
    [:div.user-zone {:on-mouse-enter #(swap! db open-user-menu)
                     :on-mouse-leave #(swap! db close-user-menu)}
     [:span (:fullname usr)]
     [:img {:border "0"
            :src (:avatar usr)}]
     [:ul.dropdown {:class (when-not (user-menu-open? @db)
                             "hide")}
      ;; FIXME: keys
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
       [:span "Save & Exit"]]]]))

(defn- set-user-form
  [state form]
  (assoc state :user-form form))

(rum/defc register-form < rum/static
  [db]
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
     :on-click #(navigate! (dashboard-route))}]
   [:div.login-links
    [:a
     {:on-click #(swap! db set-user-form :login)}
     "You already have an account?"]]])

(rum/defc recover-form < rum/static
  [db]
  [:div.login-content
   [:input.input-text
     {:name "email"
      :placeholder "Email"
      :type "email"}]
   [:input.btn-primary
    {:name "login"
     :value "Continue"
     :type "submit"
     :on-click #(navigate! (dashboard-route))}]
   [:div.login-links
    [:a
     {:on-click #(swap! db set-user-form :login)}
     "You have rememered your password?"]
    [:a
     {:on-click #(swap! db set-user-form :register)}
     "Don't have an account?"]]])

(rum/defc login-form < rum/static
  [db]
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
     :on-click #(navigate! (dashboard-route))}]
   [:div.login-links
    [:a {:on-click #(swap! db set-user-form :recover)} "Forgot your password?"]
    [:a {:on-click #(swap! db set-user-form :register)} "Don't have an account?"]]])

(rum/defc login
  [db]
  [:div.login
    [:div.login-body
     [:a
      {:on-click #(swap! db set-user-form :login)}
      logo]
     (case (:user-form @db)
       :login (login-form db)
       :register (register-form db)
       :recover (recover-form db))]])
