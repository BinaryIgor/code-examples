import bodyParser from "body-parser";
import express from "express";
import cors from 'cors';

import * as Api from './api.js';
import * as HtmxApp from './htmx-app.js';

const SERVER_PORT = process.env.SERVER_PORT || 8080;

const app = express();

app.use(cors());

app.use("/api", Api.router);
app.use("/", HtmxApp.router);

app.use((err, req, res, next) => {
  console.error(err.stack);
  if (err instanceof Api.ValidationError) {
    res.status(400)
      .send({
        error: "ValidationError",
        message: err.message
      });
  } else {
    res.status(500)
      .send({
        error: "Unknown",
        message: "Internal error"
      });
  }
});

const server = app.listen(SERVER_PORT, () => {
  console.log(`Server has started on port ${SERVER_PORT}!`);
});

const randomizerTaskId = Api.scheduleDataRandomizer();

process.on('SIGTERM', () => {
  console.log('SIGTERM signal received: closing HTTP server');
  onShutdown();
});

process.on('SIGINT', () => {
  console.log('SIGINT signal received: closing HTTP server');
  onShutdown();
});

function onShutdown() {
  clearInterval(randomizerTaskId);
  server.close(() => {
    console.log('HTTP server closed');
  });
  server.closeIdleConnections();
  setTimeout(() => {
    console.log("Closing all remaining connections...");
    server.closeAllConnections();
  }, 1000);
}