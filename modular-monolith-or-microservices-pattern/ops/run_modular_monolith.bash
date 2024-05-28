#!/bin/bash

. local.env

cd ..
cd modular-monolith

exec java -jar target/modular-monolith-1.0-SNAPSHOT.jar