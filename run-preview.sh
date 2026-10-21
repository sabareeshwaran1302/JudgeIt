#!/bin/sh
set -eu
cd "$(dirname "$0")"
exec sh ./mvnw spring-boot:run -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.profiles=preview "$@"
