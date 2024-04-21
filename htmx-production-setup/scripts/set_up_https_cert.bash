#!/bin/bash

# Webroot mode: certbot will create challenge file in the specified webroot to verify domain.
# Make sure that given webroot-path is exposed via your http server!
#
# https://eff-certbot.readthedocs.io/en/stable/using.html#webroot
# The webroot plugin works by creating a temporary file for each of your requested domains in: 
# ${webroot-path}/.well-known/acme-challenge.
# Then the Let’s Encrypt validation server makes HTTP requests to validate that the DNS for each requested domain
# resolves to the server running certbot. An example request made to your web server would look like:
# 66.133.109.36 - - [05/Jan/2016:20:11:24 -0500] "GET /.well-known/acme-challenge/HGr8U1IeTW4kY_Z6UIyaakzOkyQgPr_7ArlLgtZE8SX HTTP/1.1" 200 87 "-" "Mozilla/5.0 (compatible; Let's Encrypt validation server; +https://www.letsencrypt.org)"

# You can check renewal by running: sudo certbot renew --dry-run.

set -euo pipefail

cwd=$PWD
cd ..
. config_prod.env

cd $cwd

skip_https_server_setup="${SKIP_HTTPS_SERVER_SETUP:-false}"
https_setup_app="nginx-https-setup"

if [ $skip_https_server_setup = "false" ]; then
  echo "Building and deploying nginx https setup..."
  export ENV=prod
  export APP=$https_setup_app
  bash build_and_package_app.bash
  echo
  echo "https setup nginx built, deploying it..."
  echo
  bash deploy_app.bash
else
  echo "Skipping https server setup and assuming that it is prepared and available!"
fi

echo
echo "Setting up certbot and https cert on ${DOMAIN} domain..."
echo

webroot_path=$STATIC_PATH
remote_host="$DEPLOY_USER@$DOMAIN"
domain_email=$DOMAIN_EMAIL

ssh $remote_host "
echo "Setting up certbot..."
sudo snap install --classic certbot
sudo ln -s /snap/bin/certbot /usr/bin/certbot
echo
echo "Certbot configured, generating certs..."
sudo certbot certonly --webroot --webroot-path "${webroot_path}" --domains "${DOMAIN}" \
    --non-interactive --email ${domain_email} --agree-tos -v"

echo
echo "Certbot set, stopping ${https_setup_app}..."

ssh $remote_host "docker stop htmx-production-setup-$https_setup_app"

echo "$https_setup_app stopped, https cert with auto renewal prepared!"