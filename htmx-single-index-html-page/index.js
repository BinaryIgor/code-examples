const express = require("express");
const bodyParser = require("body-parser");

const APP_PORT = 8080;
const HTMX_SCRIPT = '<script src="https://unpkg.com/htmx.org@1.9.5" integrity="sha384-xcuj3WpfgjlKF+FXhSQFQ0ZNr39ln+hwjN3npfM9VBnUskLolQAcN80McRIVOPuO" crossorigin="anonymous"></script>';

const items = [
    {
        name: "First item",
        value: 1
    },
    {
        name: "Second item",
        value: 2
    }
];

const app = express();
app.use(bodyParser.urlencoded({ extended: true }));

app.get("/", (req, res) => {
    returnIndexHtmlPage(res);
});

app.post("/reverse-items", (req, res) => {
    items.reverse();
    returnHtmlPage(res, itemsComponent());
});

app.post("/add-item", (req, res) => {
    const newItem = {
        name: req.body.name,
        value: req.body.value
    };

    if (newItem.name && newItem.value) {
        items.push(newItem);
        returnHtmlPage(res, itemsComponent(), 201);
    } else {
        returnHtmlPage(res, "<p>Item name and value are required!", 400);
    }
});

function returnIndexHtmlPage(res) {
    returnHtmlPage(res, `
        <html>
            <head>
                <title>HTMX - single index.html page</title>
            </head>
            <body>
              <h1>HTMX - single index.html page</h1>
              <h2>Items</h2>
              <div id="items">
                ${itemsComponent()}
              </div>
              <button hx-post="/reverse-items" hx-target="#items">Reverse items</button>
              <h2>Add item</h2>
              <div style="color: red;" id="errors-container"></div>
              <form hx-post="/add-item" hx-target="#items">
                <input name="name" placeholder="Item name...">
                <br>
                <input name="value" placeholder="Item value...">
                <br>
                <input type="submit" value="Add item">
              </form>
              <script>
                const errorsContainer = document.getElementById("errors-container");
                document.addEventListener("htmx:afterRequest", e => {
                  console.log("After request we have", e);
                  if (e.detail.failed) {
                    errorsContainer.innerHTML = e.detail.xhr.response;
                  } else {
                    errorsContainer.innerHTML = "";
                  }
                });
              </script>
              ${HTMX_SCRIPT}
            </body>
        <html>
    `);
}

function returnHtmlPage(res, page, status = 200) {
    res.setHeader('Content-Type', "text/html");
    res.status(status);
    res.send(page);
}

function itemsComponent() {
    return `
        <ul>
            ${items.map(i => `<li>${i.name}: ${i.value}</li>`).join("\n")}
        </ul>`;
}

app.listen(APP_PORT, () => {
    console.log("App has started!");
});