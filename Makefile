PGPORT=5443
PGHOST=localhost
PGUSER=postgres
PGDATABASE=fhirbase
PGPASSWORD=postgres
PGIMAGE?=victor13533/fhirbase-on-postgrespro

.EXPORT_ALL_VARIABLES:
.PHONY: test build

repl:
	clj -A:dev:ui:test:nrepl

build:
	clj -A:build
	mv target/app-1.0.0-SNAPSHOT-standalone.jar app.jar

run-jar:
	java -jar app.jar

test:
	clj -A:test:runner

# Postgres
postgres-up:
	docker-compose up -d
postgres-down:
	docker-compose down
logs-up:
	docker-compose -f docker-compose.log.yaml up -d
logs-down:
	docker-compose -f docker-compose.log.yaml down
postgres-fhir-init:
	pg_restore -d postgres://$(PGUSER):$(PGPASSWOR)@$(PGHOST):$(PGPORT)/$(PGDATABASE) resources/devbox_dump.bak
