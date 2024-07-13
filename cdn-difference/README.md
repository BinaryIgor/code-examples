# Content Delivery Network (CDN) - what difference does it really make?

Content Delivery Network is a system of distributed servers that deliver content to users/clients based on their
geographic location - requests are handled by the closest server.

What are the benefits?
We can reduce latency and improve the speed and performance by caching content at various locations around the world,
which are as close to the users as possible.

It's true and makes sense in theory but curiosity naturally draws to ask the question:
> ok, there must be a difference between this approach and serving files from a single server,
> located in only one area - but what's the difference exactly? Is it worth the trouble?

## What we are about to do

We are about to deploy a simple frontend application (`static-app`) with a few assets to multiple regions. We will use
DigitalOcean as
our infrastructure provider, but obviously you can also use something else.
We will have the following regions:

* **fra** - Frankfurt, Germany
* **lon** - London, England
* **tor** - Toronto, Canada
* **syd** - Sydney, Australia

Then, we will have the following droplets (virtual machines):

* **static-fra-droplet**
* **test-fra-droplet**
* **static-lon-droplet**
* **static-tor-droplet**
* **static-syd-droplet**

Then, to each *static* droplet we will deploy the `static-app` that serves a few static assets using Nginx.
On **test-fra-droplet** `load-test` will be running; we will use it to make lots of requests to droplets in all regions
and compare the results to see *what difference CDN makes*.

Approximate distances between locations, in a straight line:

* Frankfurt - Frankfurt: ~ as close as it gets on the public Internet, the best possible case for CDN
* Frankfurt - London: ~ 637 km
* Frankfurt - Toronto: ~ 6 333 km
* Frankfurt - Sydney: ~ 16 500 km

## Let's do it!

### Requirements

* Python 3.10+ & Pip to create infra automatically from the script
* ssh key and ability to use ssh - needed to automate various operations on virtual machines (droplets)
* domain - we will create multiple descriptive subdomains for `static-app` deployed to various regions; we will also prepare https certificates
  to resemble real-world scenarios more; the ability to create DNS records is required
* DigitalOcean account - we will create infra there
* Docker to build images of `static-app` and `load-test`, and to deploy them to various machines
* Bash shell to execute build, deploy, tests and other scripts

All scripts need to be executed directly from the `scripts` directory;
they work on Linux-based machines, should mostly work on Apple devices too.

### Prepare infra

```
# create venv, install deps
bash init_python_env.bash
# activate Python env
source venv/bin/activate

export DO_API_TOKEN="<your DigitalOcean api key>"
export SSH_KEY_FINGERPRINT="<fingerprint of your ssh key, uploaded to DigitalOcean>"
python prepare_infra.py
```

This will create a few droplets with a basic firewall, in described above regions:

```
Needed droplets:
['static-fra-droplet', 'test-fra-droplet', 'static-lon-droplet', 'static-tor-droplet', 'static-syd-droplet']

...

Everything should be ready!

Get your machine addresses from DigitalOcean UI and start experimenting!
```

### Setup domains & HTTPS

In config.env change:

```
export ROOT_DOMAIN="some-domain.com"
export DOMAINS_EMAIL="igor@some-domain.com"
```

...to controlled by you domain and email accordingly.
Email is required by [Let's Encrypt](https://letsencrypt.org/) to generate certificates.

After modifying it, take IP addresses of freshly created droplets and sign in to your domain provider service - we need
to create a few `DNS A` records.

We must have the following `DNS A` records:

```
static-fra.$ROOT_DOMAIN set to static-fra-droplet IP
test-fra.$ROOT_DOMAIN set to test-fra-droplet IP
static-lon.$ROOT_DOMAIN set to static-lon-droplet IP
static-tor.$ROOT_DOMAIN set to static-tor-droplet IP
static-syd.$ROOT_DOMAIN set to static-syd-droplet IP
```

After that, we can generate HTTPS certs for our static machines by executing a single script:

```
bash set_up_https_certs.bash
```

After a minute or two, we should have 4 https certs, one for each static domain, ready to be used.

### Build & deploy apps

Build all (4) static-apps - just Nginx serving a few static assets:

```
bash build_and_package_static_apps.bash
```

Build `load-test` - custom Java script that makes multiple http requests and print out the results:

```
bash build_and_package_load_test.bash
```

All artifacts are ready to be deployed, let's do that! Static apps:

```
bash deploy_static_apps.bash
```

load test:

```
bash deploy_load_test.bash
```

## Run tests & get results

In scripts, we have various variations:

```
bash run_load_test__fra_fra.bash
bash run_load_test__fra_lon.bash
bash run_load_test__fra_tor.bash
bash run_load_test__fra_syd.bash
```

Run them one by one, not in parallel - results will not be objective! 
Each of them gives hints where to find results to compare and analyze:

```
Running load test for https://static-fra.some-domain.com on test-fra.some-domain.com host...

load test on test-fra.some-domain.com is running! To check out results, do (after a few seconds):
ssh deploy@test-fra.some-domain.com cat /home/deploy/load-test-results/test_results-static-fra.some-domain.com.txt
```

Have fun experimenting and learning ;)
