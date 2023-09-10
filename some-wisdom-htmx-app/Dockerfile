# TODO: most likely not production ready!

FROM node:19 AS build

WORKDIR /build

COPY package.json tsconfig.json tailwind.config.js build_app.bash ./
COPY assets/ assets/
COPY src/ src/

RUN npm install
RUN npm run build

FROM node:19 AS run

WORKDIR /app

COPY package*.json ./
COPY --from=build /build/dist dist/
COPY run_app.bash ./

RUN npm ci --omit=dev

ENTRYPOINT exec bash run_app.bash
