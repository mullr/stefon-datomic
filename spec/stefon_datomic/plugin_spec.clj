(ns stefon-datomic.plugin-spec

  (:require [speclj.core :refer :all]
            [datomic.api :as datomic]
            [clojure.java.io :as io]

            [stefon.shell :as shell]
            [stefon.shell.plugin :as plugin]
            [stefon-datomic.plugin :as pluginD]))


(def config (load-string (slurp (io/resource "stefon-datomic.edn"))))

(describe "Plugin should be able to attach to a running Stefon instance => "

          (before (datomic/delete-database (-> config :dev :url)))

          (it "Should attach to a running Stefon instance"

              (let [sys1 (shell/create-system)
                    sys2 (shell/start-system sys1)

                    handler-fn (fn [message] (println "Handler message, after :stefon.domain > " message))
                    sender-fn (plugin/attach-plugin @sys2 handler-fn)

                    result-promise (sender-fn {:stefon.domain {:parameters nil}})]

                (should-not-be-nil @result-promise)
                (should= {:posts [], :assets [], :tags []} @result-promise)

                (should= 1 1)))

          (it "Should return a list of domain schema"

              (let [sys1 (shell/create-system)
                    sys2 (shell/start-system sys1)

                    handler-fn (fn [message] (println "Handler message, after :stefon.domain.schema > " message))
                    sender-fn (plugin/attach-plugin @sys2 handler-fn)

                    result-promise (sender-fn {:stefon.domain.schema {:parameters nil}})]

                (println ">> " @result-promise)
                (should-not-be-nil @result-promise)
                (should= '(:posts :assets :tags) (keys @result-promise))
                (should= {:posts [{:name :id, :cardinality :one, :type :uuid} {:name :title, :cardinality :one, :type :string} {:name :content, :cardinality :one, :type :string} {:name :content-type, :cardinality :one, :type :string} {:name :created-date, :cardinality :one, :type :date} {:name :modified-date, :cardinality :one, :type :date}], :assets [{:name :id, :cardinality :one, :type :uuid} {:name :name, :cardinality :one, :type :string} {:name :type, :cardinality :one, :type :string} {:name :asset, :cardinality :one, :type :string}], :tags [{:name :id, :cardinality :one, :type :uuid} {:name :name, :cardinality :one, :type :string}]}
                         @result-promise)))


          (it "Should get the plugin's configuration"

              (let [config (pluginD/get-config)]
                (should-not-be-nil config)
                (should= '(:dev :prod) (keys config))))

          (it "Should throw an exception if DB has not been created, and we connect to DB"

              (should-throw Exception (pluginD/connect-to-db :dev)))

          (it "Should be able to create a DB"

              (let [rvalue (pluginD/create-db :dev)]

                (should-not-be-nil rvalue)
                (should rvalue)))

          (it "Should be able to Generate a DB Schema from a Domain Schema"

              (let [domain-schema {:assets
                                   [{:name :id, :cardinality :one, :type :uuid}
                                    {:name :name, :cardinality :one, :type :string}
                                    {:name :type, :cardinality :one, :type :string}
                                    {:name :asset, :cardinality :one, :type :string}],
                                   :posts
                                   [{:name :id, :cardinality :one, :type :uuid}
                                    {:name :title, :cardinality :one, :type :string}
                                    {:name :content, :cardinality :one, :type :string}
                                    {:name :content-type, :cardinality :one, :type :string}
                                    {:name :created-date, :cardinality :one, :type :date}
                                    {:name :modified-date, :cardinality :one, :type :date}],
                                   :tags
                                   [{:name :id, :cardinality :one, :type :uuid}
                                    {:name :name, :cardinality :one, :type :string}]}


                    db-schema (pluginD/generate-db-schema domain-schema)]

                (should-not-be-nil db-schema)))


          ;; check that kernel / shell is running

          ;; attach itself to kernel

          ;; check if configured DB exists
          ;;   i. if not, generate schema
          ;;   ii. create DB w/ schema




          ;; make CRUD functions from generated schema
          ;;  post(s)
          ;;  asset(s)
          ;;  tag(s)
          ;;  find-by relationships
          ;;    posts > tags
          ;;    tags > posts
          ;;    assets > post

          )
