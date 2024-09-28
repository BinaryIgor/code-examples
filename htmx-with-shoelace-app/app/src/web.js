
import fs from "fs";
import path from "path";

const ASSETS_HASH = process.env.ASSETS_HASH || "";

const ERROR_TITLE = "Something went wrong...";
const INIT_JS = `
const ComponentsState = {
    _state: new Map(),

    set(key, state) {
        this._state.set(key, state);
    },

    get(key) {
        return this._state.get(key);
    }
}

const scrollPositions = {
    restorablePaths: ["/books/"],
    positions: new Map()
};

let lastPath = null;
let restorablePath = false;

// Needed only if hx-history=false
document.addEventListener("scroll", () => {
    if (window.scrollY == 0) {
        return;
    }
    const currentPath = location.pathname;

    if (currentPath == lastPath && restorablePath) {
        scrollPositions.positions.set(currentPath, window.scrollY);
        return;
    }

    lastPath = currentPath;
    restorablePath = false;

    for (let r of scrollPositions.restorablePaths) {
        if (currentPath.startsWith(r)) {
            scrollPositions.positions.set(currentPath, window.scrollY);
            restorablePath = true;
            break;
        }
    }
});

document.addEventListener("htmx:historyRestore", e => {
    const savedScroll = scrollPositions.positions.get(location.pathname);

    if (savedScroll && savedScroll > 0) {
        window.scrollTo({
            top: savedScroll,
            left: 0
        });

        scrollPositions.positions.delete(location.pathname);
    }
});

function errorDialog() {
    if (!this._errorDialog) {
        this._errorDialog = document.getElementById("error-dialog");
        this._errorDialog.body = document.getElementById("error-dialog-body");
        document.getElementById("error-dialog-close-button").onclick = () => _errorDialog.hide();
    }
    return this._errorDialog;
}

document.addEventListener("htmx:responseError", e => {
    errorDialog().body.innerHTML = e.detail.xhr.response;
    errorDialog().show();
});

document.addEventListener("htmx:sendError", e => {
    console.log("HTMX, send error!", e.detail.xhr);
});
`;

export const asyncHandler = (fn) => (req, res, next) => {
    return Promise.resolve(fn(req, res, next)).catch(next);
}

export function returnFullOrPartialHTML(req, res, body, status = 200) {
    if (isHTMXRequest(req)) {
        returnHTML(res, body, status);
    } else {
        returnFullHTMLPage(res, body, status);
    }
}

export function returnFullOrPartialErrorHTML(req, res, body, status) {
    if (isHTMXRequest(req)) {
        returnHTML(res, body, status);
    } else {
        const fullErrorBody = `
        <div class="m-4">
            <h1 class="text-2xl text-red-600 my-4">${ERROR_TITLE}<h1>
            <div class="max-w-screen-md">
                ${body}
            </div>
        </div>
        `;
        returnFullHTMLPage(res, fullErrorBody, status);
    }
}

export function returnFullHTMLPage(res, body, status = 200) {
    returnHTML(res, `
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <title>Books</title>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <link rel="stylesheet" href="${distAssetPath("shoelace.css")}">
          <link rel="stylesheet" href="${distAssetPath("style.css")}">
          <link rel="preload" as="font" type="font/ttf" crossorigin="anonymous"
            href="${assetPath('fonts/Kalam/Kalam-Regular.ttf')}">
          <script>${INIT_JS}</script>
        </head>
        <body class="bg-background-50 text-800">
          <div hx-history="false" hx-history-elt id="page">${body}</div>
          <sl-dialog id="error-dialog" no-header>
            <div class="mb-4 relative">
                <div class="text-2xl font-bold mr-6 text-red-600">${ERROR_TITLE}</div>
                <span id="error-dialog-close-button"
                    class="absolute top-0 right-0 cursor-pointer text-3xl text-600-hover">X</span>
            </div>
            <div id="error-dialog-body"></div>
          </sl-dialog>
          <script src="${distAssetPath("index.js")}" defer></script>
          <script src="${distAssetPath("htmx/dist/htmx.min.js")}"></script>
        </body>
        </html>    
        `, status);
}

export function scopedScript(script) {
    return `<script>(function(){ ${script} })()</script>`
}

function distAssetPath(asset) {
    // TODO: support assets hashing
    const assetName = ASSETS_HASH ? `${asset}.${ASSETS_HASH}` : asset;
    return "/" + path.join("dist", assetName);
}

function assetPath(asset) {
    return "/" + path.join("assets", asset);
}

export function isHTMXRequest(req) {
    return req.get("hx-request") == 'true';
}

export function returnHTML(res, html, status = 200) {
    res.contentType('text/html');
    res.status(status);
    res.send(html);
}

export function returnText(res, text, status = 200) {
    res.contentType('text/plain');
    res.status(status);
    res.send(text);
}

export function returnJson(res, object, status = 200) {
    res.status(status);
    res.send(object);
}

export async function returnFile(res, filePath, contentType = null) {
    const file = await fs.promises.readFile(filePath);
    const resolvedContentType = contentType ? contentType : contentTypeFromFilePath(filePath);
    if (resolvedContentType) {
        res.contentType(resolvedContentType);
    }
    res.send(file);
}

export function contentTypeFromFilePath(filePath) {
    if (filePath.endsWith("png")) {
        return "image/png";
    }
    if (filePath.endsWith("jpg") || filePath.endsWith("jpeg")) {
        return "image/jpeg";
    }
    if (filePath.endsWith("svg")) {
        return "image/svg+xml";
    }
    if (filePath.endsWith("html")) {
        return "text/html";
    }
    if (filePath.endsWith("css")) {
        return "text/css";
    }
    if (filePath.endsWith("js")) {
        return "text/javascript";
    }
    return null;
}