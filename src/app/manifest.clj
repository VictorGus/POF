(ns app.manifest)

(def manifest
  {:config {:db      {:port (or (System/getenv "PGPORT") 5443)
                      :user (or (System/getenv "PGUSER") "postgres")
                      :password (or (System/getenv "PGPASSWORD") "postgres")
                      :dbname (or (System/getenv "PGDATABASE") "fhirbase")}
            :elastic {:host (or (System/getenv "ELASTICSEARCH_URL") "http://localhost:9200")}}})
