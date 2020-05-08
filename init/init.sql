SELECT 'CREATE DATABASE testbase TEMPLATE fhirbase' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'testbase')\gexec

CREATE EXTENSION IF NOT EXISTS jsquery;

CREATE TABLE public_user (
  id text PRIMARY KEY,
  txid bigint NOT NULL,
  ts timestamp with time zone default current_timestamp,
  resource_type text default 'public_user'::text,
  status resource_status NOT NULL,
  resource jsonb NOT NULL);
