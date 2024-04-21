#!/bin/bash
app_url_file=$1

if [ -z "$app_url_file" ]; then
    echo "Expected to get current app url file path as first argument, but there was nothing!"
    exit 1;
fi

if [ ! -e "$app_url_file" ]; then
    echo "Current app url file doesn't exist, skipping!"
    exit 0;
fi

current_app_url=$(cat ${app_url_file})

export SKIP_RELOAD="true"
bash update_app_url.bash ${current_app_url}