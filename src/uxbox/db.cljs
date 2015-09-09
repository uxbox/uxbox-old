(ns uxbox.db)



(def initial-state {:project-layouts {
                      :mobile {:name "Mobile" :width 320 :height 480}
                      :tablet {:name "Tablet" :width 1024 :height 768}
                      :notebook {:name "Notebook" :width 1366 :height 768}
                      :desktop {:name "Desktop" :width 1920 :height 1080}
                    }
                    :new-project-defaults {
                      :name ""
                      :width 1920
                      :height 1080
                      :layout :desktop
                    }

                    ;; Activity
                    :activity []

                    ;; Lightbox
                    :lightbox nil})

(defonce app-state (atom initial-state))
