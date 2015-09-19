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
                    :db/valueType   :db.type/keyword}
   :project/last-updated {:db/cardinality :db.cardinality/one}
   :project/created {:db/cardinality :db.cardinality/one}})

(def page-schema
  {:page/uuid {:db/unique :db.unique/identity}
   :page/title {:db/cardinality :db.cardinality/one
                :db/valueType   :db.type/string}
   :page/width {:db/cardinality :db.cardinality/one
                :db/valueType   :db.type/long}
   :page/height {:db/cardinality :db.cardinality/one
                 :db/valueType   :db.type/long}
   :page/project {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/ref}
   :page/created {:db/cardinality :db.cardinality/one}
   :page/last-updated {:db/cardinality :db.cardinality/one}})

(def shape-schema
  {:shape/uuid {:db/unique :db.unique/identity}
   :shape/name {:db/cardinality :db.cardinality/one}
   :shape/page {:db/cardinality :db.cardinality/one
                :db/valueType   :db.type/ref}
   :shape/data {:db/cardinality :db.cardinality/one}
   :shape/created {:db/cardinality :db.cardinality/one}
   :shape/locked? {:db/cardinality :db.cardinality/one}
   :shape/visible? {:db/cardinality :db.cardinality/one}})

(def user-schema
  {:user/fullname {:db/cardinality :db.cardinality/one
                   :db/valueType :db.type/string}
   :user/avatar {:db/cardinality :db.cardinality/one
                 :db/valueType :db.type/string}})

(def event-schema
  {:event/type {:db/cardinality :db.cardinality/one}
   :event/payload {:db/cardinality :db.cardinality/one}
   :event/timestamp {:db/cardinality :db.cardinality/one}
   :event/user {:db/cardinality :db.cardinality/one}})

(def schema
  {:project project-schema
   :page page-schema
   :shape shape-schema
   :user user-schema
   :yevent event-schema})
