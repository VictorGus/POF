SHELL = bash

PGPORT     ?= 5443
PGHOST     ?= localhost
PGUSER     ?= postgres
PGDATABASE ?= fhirbase
PGPASSWORD ?= postgres
PGIMAGE    ?= victor13533/fhirbase-on-postgrespro:latest

GF_SECURITY_ALLOW_EMBEDDING = true

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
	./tools/fhirbase --host ${PGHOST} -p ${PGPORT} -d ${PGDATABASE} -U ${PGUSER} -W ${PGPASSWORD} --fhir=3.3.0 load -m insert ./bundle.ndjson.gzip
	rm bundle.ndjson.gzip

# Postgres
up:
	source .env.sh && docker-compose up -d
down:
	docker-compose down
#Logs
logs-up:
	docker-compose -f docker-compose.log.yaml up -d
logs-down:
	docker-compose -f docker-compose.log.yaml down
#fhirbase
fhirbase-ui:
	docker exec -d pof fhirbase web
