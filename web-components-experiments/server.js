import bodyParser from "body-parser";
import express from "express";
import fs from "fs";
import path from "path";

const SERVER_PORT = process.env.SERVER_PORT || 8080;
const CSS_PATH = path.join("dist", "style.css");
const COMPONENTS_DIR = 'components';

const components = fs.readdirSync(COMPONENTS_DIR);

const INJECT_TEMPLATE_PATTERN = /<inject-template>(.*)<\/inject-template>/s;

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

app.get("/shoelace-data-size", async (req, res) => {
    const html = `
  <!DOCTYPE html>
<html lang="en">
<head>
  <title>Shoelace Data Size</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/themes/light.css">
</head>
<body>
  <h1>Empty Shoelace Page To Show Data Size</h1>    
  <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/button/button.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
  <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/icon/icon.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
  <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/qr-code/qr-code.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
  <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/input/input.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
  <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/select/select.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
  <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/option/option.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
        <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/divider/divider.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
   <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/dialog/dialog.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
   <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/checkbox/checkbox.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
   <script type="module" src="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn/components/textarea/textarea.js"
        data-shoelace="https://cdn.jsdelivr.net/npm/@shoelace-style/shoelace@2.17.0/cdn"></script>
</body>
</html> 
    `;
    returnHtml(res, html);
});

app.get("/htmx-shadow-dom", async (req, res) => {
    returnFullHtmlPage(res, "<htmx-shadow-dom></htmx-shadow-dom>", "htmx-shadow-dom.js");
});

app.post("/htmx-shadow-dom-button-trigger", async (req, res) => {
    returnHtml(res, `Seems to work: ${new Date()}`);
});

app.post("/change-options", async (req, res) => {
    returnHtml(res, selectOptionsHtml(selectOptions2));
});

app.get("/shoelace-playground", async (req, res) => {
    const js = `
    const selectedOptions = document.getElementById("selected-options");
    document.querySelector("sl-select").addEventListener('sl-change', e => {
        console.log("Select value has changed!", e.target.value);  
        selectedOptions.dispatchEvent(new CustomEvent("options-selected", { detail: e.target.value }));  
    });
    `;

    const html = `
    <div class="m-4">
      <div class="m-4" id="sl-button-output"></div>
      <sl-button hx-post="/htmx-shadow-dom-button-trigger" hx-target="#sl-button-output">Click me</sl-button>
      <sl-button variant="default">Default</sl-button>
      <sl-button variant="primary">Primary</sl-button>
      <sl-button variant="success">Success</sl-button>
      <sl-button variant="neutral">Neutral</sl-button>
      <sl-button variant="warning">Warning</sl-button>
      <sl-button variant="danger" size="large">Danger</sl-button>

      <sl-button variant="default" size="large" circle class="m-8 block">
        <sl-icon name="gear" label="Settings"></sl-icon>
      </sl-button>

      <sl-qr-code class="my-16" value="https://binaryigor.com"></sl-qr-code>
      <sl-input maxLength="255" clearable label="Value"></sl-input>

      <sl-select class="my-16" help-text="Make a choice!" pilled multiple clearable size="medium">
        <sl-option value="option-a">Option A</sl-option>
        <sl-option value="option-b">Option B</sl-option>
        <sl-option value="option-c">Option C</sl-option>
        <sl-option value="option-x">Option X</sl-option>
      </sl-select>

      <div id="selected-options" class="my-16" 
        hx-post="/selected-options" 
        hx-trigger="options-selected">
      </div>

      <script>${js}</script>
    </div>
    `;
    returnFullHtmlPage(res, html, "shoelace-playground.js");
});

app.post("/selected-option", async (req, res) => {
    returnHtml(res, req.body.value);
});

app.post("/trigger-error", async (req, res) => {
    returnText(res, `Something went seriously wrong at ${new Date().toISOString()}`, 400);
});

app.get("/shoelace-htmx", async (req, res) => {
    const style = `
    #name-error {
        font-size: var(--sl-input-help-text-font-size-medium);
        color: var(--sl-color-danger-700);
    }

    .inline-validation sl-input[data-user-invalid]::part(base) {
        border-color: var(--sl-color-danger-600);
    }

    .inline-validation [data-user-invalid]::part(form-control-label),
    .inline-validation [data-user-invalid]::part(form-control-help-text) {
        color: var(--sl-color-danger-700);
    }

    .inline-validation sl-input:focus-within[data-user-invalid]::part(base) {
        border-color: var(--sl-color-danger-600);
        box-shadow: 0 0 0 var(--sl-focus-ring-width) var(--sl-color-danger-300);
    }
    .
    `;

    const js = `
    const select = document.querySelector("sl-select");
    const selectedOption = document.getElementById("selected-option");
    const dialog = document.querySelector("sl-dialog");
    
    select.addEventListener('sl-change', e => {
        console.log("Select value has changed!", e.target.value);  
        selectedOption.dispatchEvent(new CustomEvent("option-selected", { detail: e.target.value }));  
    });

    document.addEventListener("htmx:responseError", e => {
        console.log("HTMX, response error!", e.detail.xhr.response);
        dialog.innerHTML = e.detail.xhr.response;
        dialog.show();
    });

    document.addEventListener("htmx:sendError", e => {
        console.log("HTMX, send error!", e.detail.xhr);
    });

    const form = document.querySelector("form");
    const nameInput = form.querySelector("sl-input");
    const nameError = document.querySelector("#name-error");

    form.addEventListener(
        'sl-invalid',
        e => {
            if (e.target == nameInput) {
                e.preventDefault();

                console.log(e.target);

                nameError.textContent = "Error: name is requried";
                nameError.hidden = false;
            }   
        },
        // sl-invalid doesn't bubble!
        { capture: true }
    );

    form.addEventListener("submit", e => {
        nameError.hidden = true;
        nameError.textContent = '';
    });

    `;

    const html = `
      <style>${style}</style>

      <sl-select class="m-8" help-text="Make a choice!" value="${selectOptions1[0].value}" clearable size="medium">
        ${selectOptionsHtml(selectOptions1)}
      </sl-select>

      <div id="selected-option" class="m-8" 
        hx-post="/selected-option" 
        hx-trigger="option-selected"
        hx-vals="js:{ value: event.detail }">
      </div>

      <sl-button class="my-8 mx-8"
        hx-post="/change-options"
        hx-target="previous sl-select">
        Change options using HTMX</sl-button>

      <sl-dialog label="Something went wrong...">
      </sl-dialog>

      <sl-button class="my-8 mx-8 block max-w-screen-sm"
        hx-post="/trigger-error">
        Trigger Error</sl-button>

      <div class="my-16"></div>

      <form class="m-8 input-validation" hx-post="/shoelace-form">
        <sl-input name="name" 
          label="Name"
          help-text="What would you like people to call you?"
          autocomplete="off"
          required>
        </sl-input>
        
        <div id="name-error" hidden></div>

        <br />

        <sl-select label="Favorite Animal" clearable required>
          <sl-option value="birds">Birds</sl-option>
          <sl-option value="cats">Cats</sl-option>
          <sl-option value="dogs">Dogs</sl-option>
          <sl-option value="other">Other</sl-option>
        </sl-select>
        <br />
        <sl-textarea name="comment" label="Comment" required></sl-textarea>
        <br />
        <sl-checkbox required>Check me before submitting</sl-checkbox>
        <br />
        <br />
        <sl-button type="submit" variant="primary">Submit</sl-button>
      </form>

      <script>${js}</script>
    `;
    returnFullHtmlPage(res, html);
});

function selectOptionsHtml(options) {
    return options.map(o => `<sl-option value="${o.value}">${o.text}</sl-option>`).join("\n");
}

app.get("/serverable-component", async (req, res) => {
    const componentName = "serverable-component.js";
    const component = await componentContent(componentName);

    const match = component.match(INJECT_TEMPLATE_PATTERN);
    const template = match[1];

    console.log(template);

    const renderedTemplate = template.replace("{title}", "New title").replace("{description}", new Date().toISOString());

    returnFullHtmlPage(res, `
        <serverable-component>
            ${renderedTemplate}
        </serverable-component>
        `, componentName);
});

app.get("/simple-list-container", async (req, res) => {
    returnFullHtmlPage(res,
        `<h1>Some Web Components List page</h1>
        <simple-list-container item-ids="1,2" items="A,B"></simple-list-container>
        <custom-button></custom-button>`,
        "simple-list-container.js", "custom-button.js");
});

app.get("/web-component-page", async (req, res) => {
    returnFullHtmlPage(res, `<web-component-page data-htmx-counter=${htmxCounter}></web-component-page>`,
        "web-component-page.js");
});

app.post("/decrease-counter", (req, res) => {
    htmxCounter--;
    returnCounterState(res);
});

function returnCounterState(res) {
    returnHtml(res, `HTMX Counter: ${htmxCounter}`);
}

app.post("/increase-counter", (req, res) => {
    htmxCounter++;
    returnCounterState(res);
});

app.get("/items", async (req, res) => {
    const html = `
    <list-component></list-component>
    <script>
    customElements.define('list-component', class extends HTMLElement {
        connectedCallback() {
            console.log("Dummy log...");
            this.innerHTML = \`
            <div>1</div>
            <div>2</div>
            \`;
        }
    });
    </script>
    `;
    returnHtml(res, html);
});

app.get("*", async (req, res) => {
    try {
        if (req.url.startsWith("/assets")) {
            const filePath = req.url.substring(1);
            await returnFile(res, filePath);
        } else if (req.url.includes(".css")) {
            await returnFile(res, CSS_PATH);
        } else if (req.url.includes("shoelace")) {
            const filePath = "node_modules/@shoelace-style" + req.url;
            console.log("Return file: " + filePath);
            await returnFile(res, filePath);
        } else if (req.url.includes(".js")) {
            const componentFile = components.find(c => req.url.endsWith(c));
            if (componentFile) {
                returnComponent(res, componentFile);
            } else {
                returnHtml(res, "<p>Unsupported path</p>", 404);
            }
        } else {
            returnFullHtmlPage(res,
                `<h1>Some Web Components Page</h1>
                <greetings-component></greetings-component>
                <button class="m-16" hx-get="/items" hx-target="#items">Get Items</button>
                <div id="items"></div>`,
                "greetings-component.js");
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