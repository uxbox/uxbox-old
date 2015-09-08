(ns uxbox.data.schema)

(def project-schema
  {:project/uuid {:db/unique :db.unique/identity}
   :project/name {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/string}
   :project/width {:db/cardinality :db.cardinality/one
                   :db/valueType   :db.type/long}
   :project/height {:db/cardinality :db.cardinality/one
                   :db/valueType    :db.type/long}
   :project/layout {:db/cardinality :db.cardinality/one
                    :db/valueType   :db.type/keyword}})

(def page-schema
  {:page/uuid {:db/unique :db.unique/identity}
   :page/title {:db/cardinality :db.cardinality/one
                :db/valueType   :db.type/string}
   :page/width {:db/cardinality :db.cardinality/one
                :db/valueType   :db.type/long}
   :page/height {:db/cardinality :db.cardinality/one
                 :db/valueType   :db.type/long}
   :page/project {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/ref}})

(def shape-schema
  {:shape/uuid {:db/unique :db.unique/identity}
   :shape/project {:db/cardinality :db.cardinality/one
                   :db/valueType   :db.type/ref}
   :shape/page {:db/cardinality :db.cardinality/one
                :db/valueType   :db.type/ref}
   :shape/data {:db/cardinality :db.cardinality/one}})

(def user-schema
  {:user/uuid {:db/unique :db.unique/identity}
   :user/fullname {:db/cardinality :db.cardinality/one
                   :db/valueType :db.type/string}
   :user/avatar {:db/cardinality :db.cardinality/one
                 :db/valueType :db.type/string}})

(def schema
  {:uxbox/project project-schema
   :uxbox/page page-schema
   :uxbox/shape shape-schema
   :uxbox/user user-schema})
