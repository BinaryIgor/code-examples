import bodyParser from "body-parser";
import express from "express";
import fs from "fs";
import path from "path";

const SERVER_PORT = process.env.SERVER_PORT || 8080;
const CSS_PATH = path.join("dist", "style.css");
const COMPONENTS_DIR = 'components';

// const components = fs.readdirSync(COMPONENTS_DIR);

let htmxCounter = 0;

const SHOLEACE_COMPONENTS = ["button", "icon", "qr-code", "input", "select", "option", "divider",
    "dialog", "checkbox", "textarea"];

const selectOptions1 = [
    {
        value: "option-a",
        text: "Option A"
    },
    {
        value: "option-b",
        text: "Option B"
    },
    {
        value: "option-c",
        text: "Option C"
    }
];
const selectOptions2 = [
    {
        value: "option-1",
        text: "Option 1"
    },
    {
        value: "option-2",
        text: "Option 2"
    },
    {
        value: "option-3",
        text: "Option 3"
    }
];

const app = express();

app.use(bodyParser.urlencoded({ extended: true }));

app.get("*", async (req, res) => {
    try {
        if (req.url.startsWith("/assets")) {
            const filePath = req.url.substring(1);
            await returnFile(res, filePath);
        } else if (req.url.includes("dist")) {
            const filePath = req.url.substring(1);
            await returnFile(res, filePath);
        } else if (req.url.includes("node_modules")) {
            const filePath = req.url;
            console.log("Return file: " + filePath);
            await returnFile(res, filePath);
        }  else {
            await returnFile(res, "index.html");
        }
    } catch (e) {
        console.error("Failed to process path:", e);
        returnHtml(res, "<p>Internal Error</p>", 500);
    }
});

async function returnFullHtmlPage(res, body, ...components) {
    // const shoelaceScripts = `<script type="module" src="/shoelace/dist/shoelace.js"></script>;
    // const shoelaceScripts = `<script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.1/cdn/shoelace-autoloader.js"></script>`;
    const shoelaceScripts = SHOLEACE_COMPONENTS.map(c => shoelaceComponentScript(c)).join("\n");

    const embeddedComponentsPromises = components.map(c => componentContent(c));
    const embeddedComponents = (await Promise.all(embeddedComponentsPromises)).join("\n");

    returnHtml(res, `
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <title>Web Components Experiments</title>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/themes/light.css">
            <!-- link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/themes/dark.css" -->
            <link rel="stylesheet" href="dist/style.css">
        </head>
        <body>
            ${body}
        </body>
        <script src="https://unpkg.com/htmx.org@2.0.2"></script>
        ${shoelaceScripts}
        <script>
            ${embeddedComponents}
        </script>
        </html>    
        `);
}

function shoelaceComponentScript(component) {
    return `<script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/${component}/${component}.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>`
}

function returnHtml(res, html, status = 200) {
    res.contentType('text/html');
    res.status(status);
    res.send(html);
}

function returnText(res, text, status = 200) {
    res.contentType('text/plain');
    res.status(status);
    res.send(text);
}

function returnJson(res, object, status = 200) {
    res.status(status);
    res.send(object);
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

async function returnComponent(res, filename) {
    await returnFile(res, path.join(COMPONENTS_DIR, filename));
}

async function componentContent(component) {
    return await fs.promises.readFile(path.join(COMPONENTS_DIR, component), { encoding: 'utf8' });
}

app.listen(SERVER_PORT, () => {
    console.log(`Server has started on port ${SERVER_PORT}!`);
});