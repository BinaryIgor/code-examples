# Some wisdom app e2e tests

E2E tests of some wisdom app (check out some wisdom htmx app dir).

Setup deps (should work with node 19 and higher):
```
npm ci
(will install needed browsers)
npx playwright install
```
If it doesn't work, you may need to run:
```
npx playwright install --with-deps
```
...to install additional dependencies.

To run all tests in a headless mode, simply execute:
```
npx playwright test
```
It will start some wisdom htmx app in the docker container, on port 8080 (see package.json build:run-app script) and execute all tests against this local instance.

Run tests from a single file:
```
npx playwright test home-page (file name/prefix)
```
Run a test with a specific title:
```
npx playwright test -g "should land on sign in page and see app title and header"
```

Run only on one, specific browser:
```
npx playwright test sign-in-page --project chromium
```

Run tests interactively in UI:
```
npx playwright test --ui
```
Run tests in a single browser, in the headed mode to see how Playwright interacts with our app:
```
npx playwright test home-page --headed --workers=1 --project chromium
```

