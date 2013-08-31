(ns stefon-datomic.crud-spec

  (:require [speclj.core :refer :all]
            [datomic.api :as datomic]
            [clojure.java.io :as io]

            [stefon.shell :as shell]
            [stefon.shell.plugin :as plugin]
            [stefon-datomic.plugin :as pluginD]
            [stefon-datomic.crud :as crud]))


(def config (load-string (slurp (io/resource "stefon-datomic.edn"))))
(def domain-schema {:assets
                    [{:name :id, :cardinality :one, :type :uuid}
                     {:name :name, :cardinality :one, :type :string}
                     {:name :type, :cardinality :one, :type :string}
                     {:name :asset, :cardinality :one, :type :string}],
                    :posts
                    [{:name :id, :cardinality :one, :type :uuid}
                     {:name :title, :cardinality :one, :type :string}
                     {:name :content, :cardinality :one, :type :string}
                     {:name :content-type, :cardinality :one, :type :string}
                     {:name :created-date, :cardinality :one, :type :instant}
                     {:name :modified-date, :cardinality :one, :type :instant}],
                    :tags
                    [{:name :id, :cardinality :one, :type :uuid}
                     {:name :name, :cardinality :one, :type :string}]})


(describe "Plugin should be able to capture and persist CRUD messages from a Stefon instance => "

          (before (datomic/delete-database (-> config :dev :url))
                  (shell/stop-system))


          ;; match incoming key against known actions
          #_(it "Should give an empty list if the action is not known"

              (let [result (crud/find-mapping :fubar)]

                (should-be-nil result)))


          ;; make CRUD functions from generated schema


          ;;  post(s)
          (it "Should save created post(s) to Datomic"


              {:title "t" :content "c" :content-type "c/t" :created-date "0000" :modified-date "1111"}

              )

          (it "Should retrieve a created post from Datomic"

              ;; create 3, then get anyone of them - the second
              2)

          (it "Should update a created post from Datomic"

              ;; create 3, then update anyone of them - the third
              3)

          (it "Should delete a created post from Datomic"

              ;; create 3, then delete anyone of them - the first
              5)

          (it "Should find by attributes: content-type & created-date"

              ;; create 4, 2 txt, and 2 md files; make one of them have a different created-date
              ;;   then find the md files... from the DB
              ;;   then find the one with a different created-date... from the DB
              6)

          (it "Should list created posts"

              ;; create 3, then list them out... from the DB
              7)

          ;;  asset(s) - binary data is in Fressian (https://github.com/Datomic/fressian)
          ;;  tag(s)
          ;;  find-by relationships
          ;;    posts > tags
          ;;    tags > posts
          ;;    assets > post

          )
