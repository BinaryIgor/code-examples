import express from "express";
import * as Web from '../shared/web.js';

export const NAME = "input-with-error";
export const PATH = "/input-with-error";

export const router = express.Router();

router.get("/", (req, res) => {
    const body = `
    <h2 class="text-lg my-4">HTMX style input:</h2>
    <input-with-error 
        type-input="text"
        name-input="message"
        placeholder-input="Input something..."
        hx-post-input="${PATH}/validate"
        hx-trigger-input="input changed delay:500ms"
        hx-swap-input="outerHTML"
        hx-target-input="next input-error">
    </input-with-error>

    <h2 class="text-lg mt-8 mb-4">Pure JS input:</h2>
    <input-with-error id="js-input"
        class-input="w-full focus:outline-indigo-400 rounded-md border-2 border-indigo-600 bg-indigo-800 p-4 text-slate-100"
        placeholder-input="Input some name between 2 and 10 characters..."
        class-error="italic text-lg text-red-600">
    </input-with-error>
    `;

    const script = `
    const inputWithError = document.getElementById("js-input");

    inputWithError.onInputChanged = (value) => {
        const error = validateName(value);
        inputWithError.onInputValidated(error);
    };


    function validateName(name) {
        if (name && name.length > 1 && name.length <= 10) {
            return "";
        }
        return "Name needs to be between 2 and 10 characters";
    }
    `;

    Web.returnHtml(res, Web.htmlPage(body, NAME, script, ["input-error"]));
});

router.post(`/validate`, (req, res) => {
    const message = req.body.message;

    if (isMessageValid(message)) {
        Web.returnHtml(res, `<input-error></input-error>`);
    } else {
        Web.returnHtml(res, `<input-error message="Message is not valid"></input-error>`);
    }
});

function isMessageValid(message) {
    return message && message.length > 2 && message.length <= 100;
}
