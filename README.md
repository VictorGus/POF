# Patients on FHIR
[![Build Status](https://travis-ci.org/VictorGus/POF.svg?branch=master)](https://travis-ci.org/VictorGus/POF)

## Getting started

Pull docker image and run container out of it
```bash
make postgres-up
```
Initialize db schema
```bash
make postgres-fhir
```
Import data set
```bash
make data-set
```
Create jsuery extension via query listed below
```sql
CREATE EXTENSION jsquery;
```
Run repl
```bash
make repl
```
For logs
```bash
make logs-up
```
