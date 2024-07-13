FROM nginx:1.25

ARG nginx_conf_dir

COPY ${nginx_conf_dir}/nginx.conf /etc/nginx/conf.d/
COPY index.html styles.css cdn.png /usr/share/nginx/site/