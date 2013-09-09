(ns stefon-datomic.crud
  (:require [clojure.string :as string]
            [datomic.api :as datomic]
            [stefon-datomic.config :as config]))


(defn find-mapping [mkey]

  (let [cfg (config/get-config)]
    (-> cfg :action-mappings mkey)))


(defn add-entity-ns [ekey datom-map]

  "Turns a datom-map like A into B, given an entity-key of :post

   A) {:title t :content c :content-type c/t :created-date 0000 :modified-date 1111}
   B) {:post/title t :post/content c :post/content-type c/t :post/created-date 0000 :post/modified-date 1111}"

  {:pre [(keyword? ekey)]}

  (let [one (seq datom-map)
        two (map (fn [inp] [(keyword (str (name ekey) "/" (name (first inp))))
                           (second inp)])
                 one)

        zkeys (map first two)
        zvals (map second two)

        final-entity (zipmap zkeys zvals)]

    final-entity))


(defn create [conn domain-key datom-map]

  {:pre [(keyword? domain-key)]}

  (let [
        one (str "plugin." (name domain-key) ".create")
        lookup-key (keyword one)

        ;; find the mapping
        mapping (find-mapping lookup-key)

        ;; insert mapped function & preamble
        mapped-fn (first mapping)
        mapped-preamble (second mapping)

        entity-w-ns (add-entity-ns :posts datom-map)

        ;; add namespace to map keys
        ;;adatom (merge entity-w-ns mapped-preamble)
        adatom (assoc entity-w-ns :db/id #db/id[:db.part/db])]

    ;; transact to Datomic
    (mapped-fn conn [adatom])))


(defn retrieve-entity [conn constraint-map]

  (let [constraints-w-ns (add-entity-ns :posts constraint-map)


        ;; We expect a structure like... ((:posts/title t) (:posts/content-type c/t))... at the end, we need to double-quote the name
        names-fn #(-> % first name (string/split #"/") first (->> (str "?")) symbol #_(->> (list (symbol 'quote))))
        param-names (map names-fn
                           (seq constraints-w-ns))
        param-values (map last (seq constraints-w-ns))


        ;; Should provide constraints that look like: [[?e :posts/title ?title] [?e :posts/content-type ?content-type]]
        constraints-final (->> constraints-w-ns
                               seq
                               (map (fn [inp]
                                      ['?e (first inp) (names-fn inp)] ))
                               (into []))


        ;;
        in-clause (->> param-names (cons '$) (into []))
        expression-intermediate-0 {:find ['?e]
                                   :in in-clause
                                   :where constraints-final}
        expression-intermediate (list (symbol 'quote) expression-intermediate-0)

        ;;
        db-conn (list (symbol 'quote) '(datomic.api/db conn))
        ;;db-conn (datomic.api/db conn)

        ;;
        expression-final `(datomic.api/q ~expression-intermediate ~db-conn ~@param-values)
        ;;expression-final `~expression-intermediate

        #_XX #_(reduce (fn [rslt ech]

                     (println "Ech > " rslt " > " ech)
                     (partial rslt ech))
                   datomic.api/q
                   (cons expression-intermediate (cons db-conn param-values)))
        ]

    (eval expression-final)

    #_(datomic.api/q expression-final (datomic.api/db conn) "t" "c/t")

    #_(println (cons expression-intermediate (cons db-conn param-values)))
    #_(apply datomic.api/q (cons expression-intermediate (cons db-conn param-values))) ))


#_(defn retrieve-entity [conn constraint-map]

  (let [constraints-w-ns (add-entity-ns :posts constraint-map)


        ;; We expect a structure like... ((:posts/title t) (:posts/content-type c/t))
        names-fn #(-> % first name (string/split #"/") first (->> (str "?")) name symbol)
        param-names (map names-fn
                         (seq constraints-w-ns))
        param-values (map last (seq constraints-w-ns))


        ;; Should provide constraints that look like: [[?e :posts/title ?title] [?e :posts/content-type ?content-type]]
        constraints-final (->> constraints-w-ns
                               seq
                               (map (fn [inp]
                                      ['?e (first inp) (names-fn inp)]))
                               (into []))

        expression-intermediate `[:find ~@(->> param-names (cons '$) (cons :in) (cons '?e)) :where ~@constraints-final]


        ;; Should provide an expression that looks like: [:find ?e :in $ ?title ?content-type :where [?e :posts/title ?title] [?e :posts/content-type ?content-type]]
        expression-final (eval `(quote ~expression-intermediate))

        XX `(datomic.api/q ~expression-final ~(datomic/db conn) ~@param-values)
        exec-fn (fn []
                  XX)]

    (eval XX)))


(defn retrieve [conn constraint-map]

  (let [id-set (retrieve-entity conn constraint-map)
        entity-set (map (fn [inp]
                          (datomic/entity (datomic/db conn) inp))
                        (first id-set))]

    (println ">> Entity-set 1 > " (datomic/entity (datomic/db conn) (ffirst id-set)))
    (println ">> Entity-set 2 > " (datomic/touch (datomic/entity (datomic/db conn) (ffirst id-set))))

    ))


#_expression-intermediate #_`[:find ~@(->> param-names (cons '$) (cons :in) (cons '?e)) :where ~@constraints-final]


;; Should provide an expression that looks like: [:find ?e :in $ ?title ?content-type :where [?e :posts/title ?title] [?e :posts/content-type ?content-type]]
#_expression-final #_(eval `(quote ~expression-intermediate))
