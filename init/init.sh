#! /bin/bash
fhirbase -d fhirbase -U postgres -W postgres --fhir=3.3.0 init
# if [ $? -eq 0 ]; then
#     export PGDATABASE="fhirbase"
#     fhirbase web &
# else
#     echo Initialization is failed
# fi
