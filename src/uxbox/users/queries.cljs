(ns uxbox.users.queries)

(defn pull-current-user
  [_]
  {:user/fullname "Michael Buchannon"
   :user/avatar "/images/avatar.jpg"})
