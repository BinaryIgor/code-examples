# HTMX Production Setup

What we have here:
* app - HTMX + Java 21 app
* db - Postgres 16 database for the app
* nginx - nginx doing as a reverse proxy, requests rate limiter and also helping with zero downtime deployments

What is needed:
* Docker for builds and deploys
* Java 21 + compatible maven for local development
* DigitalOcean account for running deployment example or just a virtual machine with Docker + ssh access

Let's go!

## Local development

There is no good production setup without great local development experience. What do we need?

For css, we use tailwind. I wanted to avoid having package.json, therefore, in the repo we have tailwind binary downloaded according to the offficial docs: https://tailwindcss.com/blog/standalone-cli

In the repo, we have *tailwindcss-linux-x64* version.

In general, in the *app/static* dir we have static resources (mostly js and css files). Security rules (see *SecurityRules.java*) are configured to make them public. What do we have here?
* in lib folder, minifed version of HTMX library https://htmx.org/

