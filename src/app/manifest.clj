(ns app.manifest)

(def manifest
  {:config {:db          {:host     (or (System/getenv "PGHOST") "localhost")
                          :port     (or (System/getenv "PGPORT") 5443)
                          :user     (or (System/getenv "PGUSER") "postgres")
                          :password (or (System/getenv "PGPASSWORD") "postgres")
                          :dbname   (or (System/getenv "PGDATABASE") "fhirbase")}
            :elastic     {:host     (or (System/getenv "ELASTICSEARCH_URL") "http://localhost:9200")}
            :json-schema {:schema   "another_schema.json"
                          :required [:identifier]}}
   :users [{:name     "admin"
            :role     "root"
            :password "YWRtaW4K"
            :email    "test@test.com"}]})

(def test-app
  {:config {:db {:host     (or (System/getenv "PGHOST") "localhost")
                 :port     (or (System/getenv "PGPORT") 5443)
                 :user     (or (System/getenv "PGUSER") "postgres")
                 :dbtype   "postgresql"
                 :password (or (System/getenv "PGPASSWORD") "postgres")
                 :dbname   (or "testbase")}}})
