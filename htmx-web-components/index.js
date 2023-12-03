import bodyParser from "body-parser";
import express from "express";
import fs from "fs";
import path from "path";

const APP_PORT = 8080;
const HTMX_SCRIPT = '<script src="https://unpkg.com/htmx.org@1.9.5" integrity="sha384-xcuj3WpfgjlKF+FXhSQFQ0ZNr39ln+hwjN3npfM9VBnUskLolQAcN80McRIVOPuO" crossorigin="anonymous"></script>';

const ERROR_CLASS = "error";

const CSS_PATH = path.join("dist", "style.css");

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
    console.log(req.path);
    returnIndexHtmlPage(res);
});

app.get("*", async (req, res) => {
    if (req.url.includes(".css")) {
        const css = await staticFileContentOfPath(CSS_PATH);
        returnCss(res, css);
    } else if (req.url.includes("htmx-components.js")) {
        const js = await staticFileContentOfPath("htmx-components.js");
        returnJs(res, js);
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

    const itemIsValid = isIdValid(newItem.id) && isNameValid(newItem.name);

    if (itemIsValid) {
        if (isIdTaken(newItem.id)) {
            returnError(res, `${newItem.id} is taken, it must be unique`);
        } else {
            items.push(newItem);
            returnItemsComponent(res, items);
        }
    } else {
        returnError(res, "Valid id and name are required");
    }
});

app.post("/add-item/validate-id", (req, res) => {
    const id = req.body.id;

    const idErrorCommponent = isIdValid(id) ? hiddenSingleInputErrorComponent() :
        singleInputErrorComponent("Id must be a valid, positive number");

    returnHtmlPage(res, idErrorCommponent);
});

function isIdValid(id) {
    try {
        const idAsNumber = parseInt(id);
        return !isNaN(idAsNumber) && idAsNumber > 0;
    } catch(e) {
        return false;
    }
}

function isIdTaken(id) {
    return items.some(i => i.id == id);
}

function singleInputErrorComponent(message, additionalErrorClasses = "mb-4") {
    return `<single-input-error error-class="${ERROR_CLASS} ${additionalErrorClasses}" message="${message}"></single-input-error>`
}

function hiddenSingleInputErrorComponent() {
    return singleInputErrorComponent("");
}

app.post("/add-item/validate-name", (req, res) => {
    const name = req.body.name;

    const nameErrorCommponent = isNameValid(name) ? hiddenSingleInputErrorComponent() :
        singleInputErrorComponent("Name can't be empty and it must be between 3 and 25 characters");

    returnHtmlPage(res, nameErrorCommponent);
});

function isNameValid(name) {
    return name && name.length >= 3 && name.length <= 25;
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
        return `<item-element class-item="border-2 rounded border-solid border-black p-2 ${itemMarginTop}" 
            item-id="${i.id}" item-name="${i.name}"></item-element>`;
    }).join("\n");

    return `
    <items-list class-container="px-2" id="items">${itemsHtml}</items-list>`;
}

function returnIndexHtmlPage(res) {
    returnHtmlPage(res, `
        <html>
            <head>
                <title>HTMX - Web Components</title>
                <link href="${CSS_PATH}" rel="stylesheet">
            </head>
            <body class="m-4">
              <h1 class="text-2xl">HTMX - Web Components</h1>
              
              <custom-form 
                class-container="m-2 p-2 bg-slate-100 rounded" 
                class-generic-error="error" 
                class-id-input="p-2 rounded"
                class-name-input="mt-4 p-2 rounded"
                class-submit-input="font-bold border-2 border-black rounded p-2 mt-2"
                _hx-post="/add-item" _hx-target="#items"
                _hx-swap="outerHTML"
                _hx-post-id-validation="/add-item/validate-id"
                _hx-post-name-validation="/add-item/validate-name">
              </custom-form>

             ${itemsComponent(items)}

              <script>
                console.log("HTMX with web components!");
              </script>
              ${HTMX_SCRIPT}
              <script src="/htmx-components.js"></script>
            </body>
        <html>
    `);
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

function returnError(res, error, status = 400) {
    returnText(res, error, status);
}

function returnCss(res, css, cacheControl) {
    res.contentType("text/css");
    if (cacheControl) {
        res.setHeader("cache-control", cacheControl);
    }
    res.send(css);
}


export function returnJs(res, js, cacheControl) {
    res.contentType("application/javascript");
    if (cacheControl) {
        res.setHeader("cache-control", cacheControl);
    }
    res.send(js);
}
