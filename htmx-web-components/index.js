import bodyParser from "body-parser";
import express from "express";
import fs from "fs";
import path from "path";

const APP_PORT = 8080;
const HTMX_SCRIPT = '<script src="https://unpkg.com/htmx.org@1.9.5" integrity="sha384-xcuj3WpfgjlKF+FXhSQFQ0ZNr39ln+hwjN3npfM9VBnUskLolQAcN80McRIVOPuO" crossorigin="anonymous"></script>';

const app = express();
app.use(bodyParser.urlencoded({ extended: true }));

app.get("/", (req, res) => {
    console.log(req.path);
    returnIndexHtmlPage(res);
});

app.get("*", async (req, res) => {
    if (req.url.includes(".css")) {
        const css = await staticFileContentOfPath("style.css");
        returnCss(res, css);
    }
});

function staticFileContentOfPath(path) {
    return fs.promises.readFile(path, 'utf-8');
}

app.post("/add-item", (req, res) => {
    const newItem = {
        name: req.body.name,
        value: req.body.value
    };

    //TODO: do something!
});

app.listen(APP_PORT, () => {
    console.log(`App has started on port ${APP_PORT}!`);
});

function returnIndexHtmlPage(res) {
    returnHtmlPage(res, `
        <html>
            <head>
                <title>HTMX - Web Components</title>
                <link href="/style.css" rel="stylesheet">
            </head>
            <body>
              <h1>HTMX - Web Components</h1>
              <script>
                console.log("HTMX with web components!");
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

function returnCss(res, css, cacheControl) {
    res.contentType("text/css");
    if (cacheControl) {
        res.setHeader("cache-control", cacheControl);
    }
    res.send(css);
}
