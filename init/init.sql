SELECT 'CREATE DATABASE testbase TEMPLATE fhirbase' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'testbase')\gexec

CREATE EXTENSION IF NOT EXISTS jsquery

CREATE table IF NOT EXISTS public_user as (select * from patient) with no data
