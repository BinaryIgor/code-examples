#!/bin/bash
certs_path="${PWD}"
openssl req -x509 -nodes -days 3333 -newkey rsa:2048 \
  -keyout "${certs_path}"/privkey.pem \
  -out "${certs_path}"/fullchain.pem