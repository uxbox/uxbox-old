(ns uxbox.db)



(def initial-state {;; Activity
                    :activity []

                    ;; Lightbox
                    :lightbox nil})

(defonce app-state (atom initial-state))
