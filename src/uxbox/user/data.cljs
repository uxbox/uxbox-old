(ns uxbox.user.data)

(defonce user (atom {:user/uuid (random-uuid)
                     :user/fullname "Michael Buchannon"
                     :user/avatar "/images/avatar.jpg"}))
