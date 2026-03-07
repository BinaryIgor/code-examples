import express from "express";

import path from "path";
import fs from "fs";

const CSS_PATH = path.join("assets", "output.css");
const WEB_COMPONENTS_PATH = path.join("assets", "web-components.js");

const HTMX_SCRIPT = '<script src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.7/dist/htmx.js" integrity="sha384-yWakaGAFicqusuwOYEmoRjLNOC+6OFsdmwC2lbGQaRELtuVEqNzt11c2J711DeCZ" crossorigin="anonymous" defer></script>';
const WEB_COMPONENTS_SCRIPT = `<script src="${WEB_COMPONENTS_PATH}" type="module"></script>`;

export const router = express.Router();

router.get("/assets/*any", async (req, res) => {
  if (req.url.includes(".css")) {
    await returnFile(res, CSS_PATH);
  } else {
    const filePath = req.url.substring(1);
    await returnFile(res, filePath);
  }
});

router.get("/", (req, res) => {
  returnHomePage(res);
});

function returnHomePage(res) {
  // TODO: does HTMX script must be in the head?
  const html = `
  <!doctype html>
  <html lang="en">
    <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>Markets</title>
      <link href="${CSS_PATH}" rel="stylesheet">
    </head>
    <body>
      <div id="app" class="max-w-5xl">
        ${marketsHeaderHTML()}
        ${assetsAndCurrenciesHTML()}
        ${marketsProjectionsHTML()}
      </div>
      <error-modal></error-modal>
      ${HTMX_SCRIPT}
      ${WEB_COMPONENTS_SCRIPT}
    </body>
  </html>
  `;
  returnHtml(res, html);
}

// TODO: params for all
function marketsHeaderHTML() {
  return `
  <markets-header>
  </markets-header>`;
}

function assetsAndCurrenciesHTML() {
  return `
  <asssets-and-currencies>
  </assets-and-currencies>
  `;
}

function marketsProjectionsHTML() {
  return `
  <markets-projections>
  </markets-projections>
  `;
}

function returnHtml(res, html, status = 200) {
  res.contentType('text/html')
    .status(status)
    .send(html);
}

function returnText(res, text, status = 200) {
  res.contentType('text/plain')
    .status(status)
    .send(text);
}

function returnJson(res, object, status = 200) {
  res.status(status).send(object);
}

function returnTextError(res, error, status = 400) {
  returnText(res, error, status);
}

function returnJs(res, js) {
  res.contentType("text/javascript").send(js);
}

async function returnFile(res, filePath, contentType = null) {
  const file = await fs.promises.readFile(filePath);
  const resolvedContentType = contentType ? contentType : contentTypeFromFilePath(filePath);
  if (resolvedContentType) {
    res.contentType(resolvedContentType);
  }
  res.send(file);
}

function contentTypeFromFilePath(filePath) {
  if (filePath.endsWith("png")) {
    return "image/png";
  }
  if (filePath.endsWith("jpg") || filePath.endsWith("jpeg")) {
    return "image/jpeg";
  }
  if (filePath.endsWith("svg")) {
    return "image/svg+xml";
  }
  if (filePath.endsWith("css")) {
    return "text/css";
  }
  if (filePath.endsWith("js")) {
    return "text/javascript";
  }
  return null;
}