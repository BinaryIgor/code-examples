import bodyParser from "body-parser";
import express from "express";
import fs from "fs";
import path from "path";

const APP_PORT = process.env.APP_PORT || 8080;
const HTMX_SCRIPT = '<script src="https://unpkg.com/htmx.org@1.9.5" integrity="sha384-xcuj3WpfgjlKF+FXhSQFQ0ZNr39ln+hwjN3npfM9VBnUskLolQAcN80McRIVOPuO" crossorigin="anonymous"></script>';
const CSS_PATH = path.join("dist", "style.css");
const JS_PATH = "index.js";
const COMPONENTS_PATH = "htmx-components.js";

const MIN_NAME_LENGTH = 3;
const MAX_NAME_LENGTH = 25;

const items = [
    {
        id: 1,
        name: "first item"
    },
    {
        id: 2,
        name: "second item"
    }
];

const app = express();
app.use(bodyParser.urlencoded({ extended: true }));

app.get("/", (req, res) => {
    returnHtmlPage(res, indexHtmlPage());
});

app.get("*", async (req, res) => {
    if (req.url.includes(".css")) {
        returnCss(res, await staticFileContentOfPath(CSS_PATH));
    } else if (req.url.includes(COMPONENTS_PATH)) {
        returnJs(res, await staticFileContentOfPath(COMPONENTS_PATH));
    } else if (req.url.includes(JS_PATH)) {
        returnJs(res, await staticFileContentOfPath(JS_PATH));
    } else {
        returnHtmlPage(res, "<p>Unsupported path</p>", 404);
    }
});

function staticFileContentOfPath(path) {
    return fs.promises.readFile(path, 'utf-8');
}

app.post("/add-item", (req, res) => {
    const newItem = {
        id: req.body.id,
        name: req.body.name
    };

    if (isIdValid(newItem.id) && isNameValid(newItem.name)) {
        if (isIdTaken(newItem.id)) {
            returnHtmlPage(res, "Id needs to be unique", 400);
        } else {
            items.push(newItem);
            returnItemsComponent(res, items);
        }
    } else {
        returnHtmlPage(res, "Valid id and name is required", 400);
    }
});

app.post("/add-item/validate-id", (req, res) => {
    const id = req.body.id;

    if (isIdValid(id)) {
        returnHtmlPage(res, hiddenInputErrorComponent());
    } else {
        returnHtmlPage(res, inputErrorComponent("Id needs to be a positive number"));
    }
});

function isIdValid(id) {
    try {
        return !isNaN(id) && parseInt(id) > 0;
    } catch (e) {
        return false;
    }
}

function inputErrorComponent(message, additionalErrorClasses = "mb-2") {
    return `<input-error class-error="error ${additionalErrorClasses}" message="${message}"></input-error>`;
}

function hiddenInputErrorComponent() {
    return inputErrorComponent("");
}

function isIdTaken(id) {
    return items.some(i => i.id == id);
}

app.post("/add-item/validate-name", (req, res) => {
    const name = req.body.name;

    if (isNameValid(name)) {
        returnHtmlPage(res, hiddenInputErrorComponent());
    } else {
        returnHtmlPage(res, inputErrorComponent(`Name needs to have between ${MIN_NAME_LENGTH} and ${MAX_NAME_LENGTH} characters`));
    }
});

function isNameValid(name) {
    return name && name.length >= MIN_NAME_LENGTH && name.length <= MAX_NAME_LENGTH;
}

app.listen(APP_PORT, () => {
    console.log(`App has started on port ${APP_PORT}!`);
});

function returnItemsComponent(res, items) {
    returnHtmlPage(res, itemsComponent(items));
}

function itemsComponent(items) {
    const itemsHtml = items.map((i, idx) => {
        const itemMarginTop = idx > 0 ? "mt-2" : "";
        return `<item-element class-item="cursor-pointer rounded bg-slate-100 p-4 border-2 border-solid border-slate-200 ${itemMarginTop}" 
            item-id="${i.id}" item-name="${i.name}"></item-element>`;
    }).join("\n");

    return `<items-list class-container="mt-4" id="items">${itemsHtml}</items-list>`;
}

function indexHtmlPage() {
    return `
    <html>
        <head>
            <title>HTMX + Web Components</title>
            <link href="${CSS_PATH}" rel="stylesheet">
        </head>
        <body class="m-4">
            <h1 class="text-3xl font-bold mb-8">HTMX + Web Components</h1>

            <item-form
                class-container="bg-slate-100 rounded p-4"
                class-generic-error="error"
                class-id-input="p-2 rounded"
                class-name-input="p-2 rounded mt-4"
                class-submit="px-8 py-2 border-solid border-slate-300 border-2 rounded mt-4"
                hx-post-form="/add-item"
                hx-target-form="#items"
                hx-post-id-input="/add-item/validate-id"
                hx-post-name-input="/add-item/validate-name">
            </item-form>

            ${itemsComponent(items)}

            ${HTMX_SCRIPT}
            <script src="${COMPONENTS_PATH}"></script>
            <script src="${JS_PATH}"></script>
        </body>
    <html>`;
}

function returnHtmlPage(res, page, status = 200) {
    res.setHeader('Content-Type', "text/html");
    res.status(status);
    res.send(page);
}

function returnText(res, text, status = 200) {
    res.setHeader('Content-Type', "text/plain");
    res.status(status);
    res.send(text);
}

function returnCss(res, css) {
    res.contentType("text/css");
    res.send(css);
}

function returnJs(res, js) {
    res.contentType("application/javascript");
    res.send(js);
}
