(ns uxbox.data.schema)

(def project-schema
  {:project/uuid {:db/unique :db.unique/identity}
   :project/name {:db/cardinality :db.cardinality/one}
   :project/width {:db/cardinality :db.cardinality/one}
   :project/height {:db/cardinality :db.cardinality/one}
   ;; TODO: enum :layout
   :project/pages {:db/cardinality :db.cardinality/many}})

(def page-schema
  {:page/uuid {:db/unique :db.unique/identity}
   :page/title {:db/cardinality :db.cardinality/one}
   :page/width {:db/cardinality :db.cardinality/one}
   :page/height {:db/cardinality :db.cardinality/one}
   :page/project {:db/valueType :db.type/ref}})

(def shape-schema
  {:shape/uuid {:db/unique :db.unique/identity}
   :shape/project {:db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/one}
   :shape/page {:db/valueType :db.type/ref
                :db/cardinality :db.cardinality/one}
   :shape/data {:db/cardinality :db.cardinality/one}}) ;; TODO: type?

(def user-schema
  {:user/name {:db/cardinality :db.cardinality/one}
   :user/avatar {:db/cardinality :db.cardinality/one}})

(def schema
  {:uxbox/project project-schema
   :uxbox/page page-schema
   :uxbox/shape shape-schema
   :uxbox/user user-schema})
