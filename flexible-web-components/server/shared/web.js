import path from "path";

const HTMX_SCRIPT = '<script src="https://unpkg.com/htmx.org@1.9.9" integrity="sha384-QFjmbokDn2DjBjq+fM+8LUIVrAgqcNW2s0PjAxHETgRn9l4fvX31ZxDxvwQnyMOX" crossorigin="anonymous"></script>';
const CSS_PATH = path.join("dist", "style.css");
//TODO: fix this!

export function htmlPage(body, component, script="", additionalComponents=[]) {
    //TODO: remove unnecessary Tailwind classes
    return `
    <html>
        <head>
            <title>Flexible Web Components: ${component}</title>
            <link href="${CSS_PATH}" rel="stylesheet">
        </head>
        <body class="m-4">
            <h1 class="text-3xl font-bold mb-8">Flexible Web Components: ${component}</h1>

            ${body}

            ${HTMX_SCRIPT}
            <script type="module" src="${component}.js"></script>
            ${additionalComponents.map(c => `<script type="module" src="${c}.js"></script>`).join("\n")}
            <script>${script}</script>
        </body>
    <html>`;
}

export function returnHtml(res, html, status = 200) {
    res.contentType('text/html');
    res.status(status);
    res.send(html);
}

export function returnText(res, text, status = 200) {
    res.contentType('text/plain');
    res.status(status);
    res.send(text);
}

export function returnTextError(res, error, status = 400) {
    returnText(res, error, status);
}

export function returnCss(res, css) {
    res.contentType("text/css");
    res.send(css);
}

export function returnJs(res, js) {
    res.contentType("application/javascript");
    res.send(js);
}