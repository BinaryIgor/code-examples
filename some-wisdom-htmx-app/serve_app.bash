#!/bin/bash
logs_output="/tmp"
tailwind_logs_output="$logs_output/tailwind.txt"
app_logs_output="$logs_output/some-wisdom-app.txt"

export ASSETS_STYLES_PATH="${PWD}/dist/assets" 

echo "Starting live-reloading tailwind styles, logging to $tailwind_logs_output..."
nohup npx tailwindcss -i ./assets/style.css -o ./dist/assets/style.css --watch=always > $tailwind_logs_output &
tailwind_pid=$!

echo "Starting live-reloading some-wisdom app, logging to $app_logs_output..."
nohup npx nodemon --ignore '**/assets/db/**' src/app.ts > $app_logs_output &
app_pid=$!

echo "App pid: $app_pid, tailwind: $tailwind_pid"

cleanup() {
    echo "Closing processes..."

    kill $tailwind_pid
    kill $app_pid

    echo "Processes closed, see you next time!"

    exit
}

trap cleanup INT

tail -f $app_logs_output