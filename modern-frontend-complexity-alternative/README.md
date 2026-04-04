# Modern Frontend Complexity Alternative

A simpler approach to build web apps with all the expected features:
* instant load times
* native-like, smooth transitions between pages
* high degree of interactivity; most user actions should feel fast
* real-time validation and hints; especially for complex forms and processes
* great developer experience - ability to quickly see and validate UI changes
* possibility of creating, sharing and reusing configurable UI components
* testability - how do we know whether it works?
* easy to introduce translations & internationalization

For local development we need:
* Java 25
* node (TailwindCSS management)

For building & packing, Java is not required but Docker must be present.

For local development, run:
```
./mvnw spring-boot:run
```
To have reloadable backend; in other terminal run
```
npm ci

cd ops
./live-css-gen.sh

≈ tailwindcss v4.2.2

Done in 93ms
Done in 169µs
Done in 4ms
```
To have CSS classes (thanks to the use of Tailwind) to be live-generated.

