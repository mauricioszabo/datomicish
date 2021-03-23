(ns datomicish.core
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as honey]))

(defn transact! [db datom]
  (let [[id attr value] datom]
    (jdbc/insert! db "txt_datoms" {:id id
                                   :attribute (str attr)
                                   :value  value
                                   :retract false})))

#_
(jdbc/with-db-transaction [db db]
  (doseq [n (range 10000)
          :let [id (java.util.UUID/randomUUID)
                name (str (gensym "name-"))
                age (str (gensym "age-"))
                child (str (gensym "child-"))]]
    (transact! db [id :person/name name])
    (transact! db [id :person/age age])
    (transact! db [id :person/child child])))

(defn- parse-where [sofar [id attr val]]
  (let [alias (-> attr name gensym keyword)]
    (-> sofar
        (update ["id" id] conj alias)
        (update ["value" val] conj alias)
        (assoc alias attr))))

(defn- into-query [q-where]
  (reduce (fn [sofar [k v]]
            (if (keyword? k)
              (-> sofar
                  (update :from conj [:txt_datoms k])
                  (update :where conj [:=
                                       (keyword (str (name k) ".attribute"))
                                       (str v)]))
              (let [[kind val] k
                    more-where (when-not (symbol? val)
                                 [[:= (keyword (str (name (first v)) "." kind)) val]])
                    wheres (map #(vector :=
                                         (keyword (str (name %1) "." kind))
                                         (keyword (str (name %2) "." kind)))
                                v (rest v))]
                (update sofar :where #(into % (concat wheres more-where))))))
          {:from [] :where [:and]}
          q-where))

(defn- into-select [q-where query]
  (mapv (fn [field]
          (if-let [[[kind] [alias]] (->> q-where
                                         (filter (fn [[k v]]
                                                   (and (coll? k)
                                                        (-> k second (= field)))))
                                         first)]
            [(keyword (str (name alias) "." kind))
             (keyword (str field))]
            field))
        (:find query)))

(defn q [db query]
  (let [q-where (->> query
                     :where
                     (reduce parse-where {}))
        honey (assoc (into-query q-where)
                     :select (into-select q-where query))]
    (jdbc/query db
                (honey/format honey :quoting :ansi))))


#_
(def db {:dbtype "postgresql"
         :dbname "datomicish"
         :host "localhost"
         :user "postgres"})
#_
(q db '{:find [?id ?name]
        :where [[?id :person/age "age-6104"]
                [?id :person/name ?name]]})
