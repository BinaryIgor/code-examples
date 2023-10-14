#!/bin/bash
set -e

export DB_PATH="${PWD}/dist/assets/db"
export E2E_TESTS_DB_PATH="${PWD}/dist/assets/e2e-tests-db"
export ASSETS_PATH="${PWD}/dist/assets"

assets_hash=$(cat dist/assets_hash.txt)

export ASSETS_STYLES_SRC="/style-${assets_hash}.css";
export ASSETS_INDEX_JS_SRC="/index-${assets_hash}.js";

export ASSETS_CACHE_CONTROL="max-age=31536000, public, immutable";

exec node dist/app.js