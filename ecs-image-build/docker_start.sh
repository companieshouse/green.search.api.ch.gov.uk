#!/bin/bash
#
# Start script for green.search.api.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "green.search.api.ch.gov.uk.jar"
