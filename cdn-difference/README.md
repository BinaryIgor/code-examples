# Content Delivery Network (CDN) - what difference does it make?

Various Content Delivery Networks spread static assets of our web applications (mostly html, js and css files)
geographically,
to multiple servers located in multiple geographical regions.
They do this, so that every user that connects to our app gets assets from nearest located server, geographically,
which is optimal, because the path that network packets needs to travel is the shortest possible once.

It's true in theory, but curiosity naturally draws to ask the question:
> ok, there must be a difference between this approach and serving files from single server,
> located in only one area - but what's the difference exactly? Is it worth the trouble?

## What we are about to do

We are about to deploy a simple frontend application with a few assets to multiple regions. We will use DigitalOcean as
our infrastructure provider, but obviously you can also use something else.
We will have the following regions:

* FRA - Frankfurt, Germany
* LON - London, England
* NYC - New York, USA
* SYD - Sydney, Australia
* AMS - Amsterdam, Netherlands

Then, in every region we will have two droplets (virtual machines).
We will test the best and the worst cases for CDN and compare the results!
The cases will be:

* on machine1-FRA get assets from machine2-FRA - the best possible case for CDN, since they are both in the same
  geographical region
* do the same scenario - two machines in the same region - for another region, to have objective comparison
* make requests from FRA to LON, from LON to NYC etc. - all kinds of combinations to compare different possible
  cases, based on different distances between data centers and check latency difference

Approximate distances between some locations, in a straight line:

* Frankfurt - London: 635 km
* Frankfurt - New York: 11 000 km
* Frankfurt - Sydney: 16 000 km
* Frankfurt - Amsterdam: 360 km
* London - New York: 5 000 km
* London - Sydney: 17 000 km
* New York - Sydney: 15 000 km

## How we are about to do it

* infra script
* nginx + let's encrypt certs
* performance tool