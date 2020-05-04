PGPORT=5443
PGHOST=localhost
PGUSER=postgres
PGDATABASE=fhirbase
PGPASSWORD=postgres
PGIMAGE?=victor13533/fhirbase-on-postgrespro
GF_SECURITY_ALLOW_EMBEDDING=true

.EXPORT_ALL_VARIABLES:
.PHONY: test build

repl:
	clj -A:dev:ui:test:nrepl

build:
	clojure -A:build
	mv target/app-1.0.0-SNAPSHOT-standalone.jar app.jar

run-jar:
	java -jar app.jar

test:
	clojure -A:test:runner

data-set:
	wget https://github.com/fhirbase/fhirbase/raw/master/demo/bundle.ndjson.gzip
	./tools/fhirbase --host localhost -p 5443 -d fhirbase -U postgres -W postgres --fhir=3.3.0 load -m insert ./bundle.ndjson.gzip
	rm bundle.ndjson.gzip

# Postgres
postgres-up:
	docker-compose up -d
postgres-down:
	docker-compose down
postgres-fhir:
	docker exec pof /fhirbase --host localhost -p 5432 -d fhirbase -U postgres -W postgres --fhir=3.3.0 init
logs-up:
	docker-compose -f docker-compose.log.yaml up -d
logs-down:
	docker-compose -f docker-compose.log.yaml down
