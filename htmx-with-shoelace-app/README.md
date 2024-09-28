# HTMX with Shoelace Books App

Requirements:
* bash
* node 20+

## Static

We have all things static here:
* Used sholeace components, assets and css - declared in the `index.js` file
* HTMX as dependency
* Tailwind CSS config

The goal here is to prepare all static dependencies that are need by the HTMX app, where all the logic resides.
In the dist, there will be a few bundled files that `app` makes use of. For details, see `returnFullHTMLPage` function in the `app/src/web.js` file.

In the production-ready setup, we would also need to add assets hashing to enable caching; it is not done here, for the sake of simplicity ;)

To install deps run (from `/static`):
```
npm ci
```

To build dist for the `app`, run:
```
npm run build
```

In the dist folder we now have a few bundles - Shoelace components, assets & CSS, HTMX - that `app` makes use of.

For the local development, it is preferable to use:
```
npm run build:watch:css
```
which first builds distributable and then keeps watching for CSS changes in the `app/src/` directory to generate needed Tailwind classes.


## App

Here is the HTMX app! Really simple, a few JS files with a node server (using express.js) which has a few endpoints to provide Books functionality and frontend assets.

To install deps (from `/app`):
```
npm ci
```
To run it:
```
npm start
```
It should start on port `8080`, which can be changed by the `SERVER_PORT` env variable.