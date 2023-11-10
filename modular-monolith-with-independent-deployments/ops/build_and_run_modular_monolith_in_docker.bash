#!/bin/bash
set -e

echo "Building Independent & Modular Monolith in docker..."

bash build_modular_monolith_in_docker.bash

echo
echo "Modular & Independent One is built, running it!"
echo

bash run_modular_monolith_in_docker.bash


