#!/bin/bash
set -euo pipefail

echo
echo "Setting up certbot and https cert on ${DOMAIN} domain..."
echo

remote_host="deploy@$DOMAIN"
domain_email=$DOMAIN_EMAIL

# Standalone mode: certbot will temporarily spin up a webserver on the machine
ssh $remote_host "
echo "Setting up certbot..."
sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot
echo
echo "Certbot configured, generating certs..."
sudo certbot certonly --standalone --domains "${DOMAIN}" \
    --non-interactive --email ${domain_email} --agree-tos -v"

echo
echo "Certs are setup"