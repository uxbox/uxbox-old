(ns uxbox.users.queries)

(defn pull-current-user
  [_]
  {:user/uuid (random-uuid)
   :user/fullname "Michael Buchannon"
   :user/avatar "/images/avatar.jpg"})
