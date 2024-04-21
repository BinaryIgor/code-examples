# redirect stderr to stdout, so that Certbot doesn't complain
docker exec ${nginx_container} nginx -s reload 2>&1