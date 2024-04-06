#!/bin/bash
echo "Removing previous container...."
docker rm ${app}

echo
echo "Creating new ${app} version..."
echo

${create_cmd}