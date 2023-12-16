import express from "express";
import * as Web from '../shared/web.js';

export const NAME = "form-container";
export const PATH = "/form-container";

export const router = express.Router();

const MIN_ID_LEN = 4;
const MAX_ID_LEN = 40;

const MIN_NAME_LEN = 3;
const MAX_NAME_LEN = 30;

const MAX_DESCRIPTION_LEN = 150;

const SECRET_ERROR_ID = "secret-error";

const orders = [
    {
        id: crypto.randomUUID(),
        name: "Order name",
        description: "Order description",
        secret: "Order secret"
    }
];

router.get("/", (req, res) => {
    const body = `
    <info-modal id="error-modal" title:add:class="text-red-500" title="Something went wrong...">
    </info-modal>

    <form-container
        form:id="order-form"
        form:class="rounded bg-slate-200 p-2 max-w-screen-md"
        submit:class="py-2 rounded bg-slate-100 mt-4 w-full"
        submit:value="Add Order"
        form:hx-post="${PATH}/add"
        form:hx-target="#orders">

    <input-with-error 
        container:class="mb-2"
        input:add:class="w-full"
        input:name="id"
        input:placeholder="Order id"
        ${inputWithErrorHtmxAttributes(`${PATH}/validate-id`)}
        input:hx-include="#secret-input">
    </input-with-error>
    
    <input-with-error 
        container:class="mb-2"
        input:add:class="w-full"
        input:name="name"
        input:placeholder="Order name"
        ${inputWithErrorHtmxAttributes(`${PATH}/validate-name`)}>
    </input-with-error>

    <input-with-error 
        container:class="mb-2"
        input:add:class="w-full"
        input:name="description"
        input:placeholder="Order description"
        ${inputWithErrorHtmxAttributes(`${PATH}/validate-description`)}>
    </input-with-error>

    <input-with-error 
        container:class="mb-2"
        input:add:class="w-full"
        input:type="password"
        input:name="secret"
        input:id="secret-input"
        input:placeholder="Order secret, compatibility with id is required"
        input-error:id="${SECRET_ERROR_ID}"
        ${inputWithErrorHtmxAttributes(`${PATH}/validate-secret`)}>
    </input-with-error>

    </form-container>

    <!--button class="p-4 rounded-md bg-slate-100 border-4 border-bg-slate-200 mb-8 w-full max-w-screen-md"
        onclick="clearForm()">
    Clear Form</button-->

    <ul id="orders" class="space-y-2 max-w-screen-md">
        ${ordersHtml()}
    </ul>
    `;

    const script = `
        const errorModal = document.getElementById("error-modal");

        const formContainer = document.querySelector("form-container");

        function clearForm() {
            formContainer.clearInputs();
        }

        formContainer.addEventListener("htmx:afterRequest", e => {
            console.log("After htmx request...", e);
            const form = document.getElementById("order-form");
            if (e.srcElement == form) {
                const error = e.detail.failed ? e.detail.xhr.response : "";
                formContainer.afterSubmit({error: error});
                if (error) {
                    errorModal.show({message: error});
                }
            }
        });
    `;

    Web.returnHtml(res, Web.htmlPage(body, NAME, script, ["input-error", "input-with-error", "info-modal"]));
});

function inputWithErrorHtmxAttributes(validationPost) {
    return `
        input:hx-post="${validationPost}"
        input:hx-trigger="input changed delay:500ms"
        input:hx-swap="outerHTML"
        input:hx-target="next input-error"`;
}

function ordersHtml() {
    return orders.map(o =>
        `<div class="rounded bg-slate-100 p-2">
            <div>Id: ${o.id}</div>
            <div>Name: ${o.name}</div>
            <div>Description: ${o.description}</div>
            <div>Secret: ${o.secret}</div>
        </div>`)
        .join("\n");
}

router.post(`/add`, (req, res) => {
    const order = {
        id: req.body.id,
        name: req.body.name,
        description: req.body.description,
        secret: req.body.secret
    };

    const idValid = isOrderIdValid(order.id);
    const nameValid = isOrderNameValid(order.name);
    const descriptionValid = isOrderDescriptionValid(order.description);
    const secretValid = idValid && isOrderSecretValid(order.id, order.secret);

    if (idValid && nameValid && descriptionValid && secretValid) {
        orders.push(order);
        Web.returnHtml(res, ordersHtml());
    } else {
        const invalid = [idValid ? "" : "id",
        nameValid ? "" : "name",
        descriptionValid ? "" : "description",
        secretValid ? "" : "secret"]
            .filter(f => f)
            .join(", ");

        Web.returnTextError(res, `Something in the order was not valid: ${invalid}`);
    }
});

router.post(`/validate-id`, (req, res) => {
    const id = req.body.id;
    const secret = req.body.secret;

    let response;
    if (isOrderIdValid(id)) {
        const secretError = isOrderSecretValid(id, secret) ? "" : invalidOrderSecretMessage(id);
        response = `
        ${inputError()}
        ${inputError({ errorMessage: secretError, otherAttributes: `id="${SECRET_ERROR_ID}" hx-swap-oob="true"` })}
        `;
    } else {
        response = inputError({ errorMessage: `Order id needs to be between ${MIN_ID_LEN} and ${MAX_ID_LEN} characters` });
    }

    Web.returnHtml(res, response);
});

function returnInputErrorIf(res, inputValid, errorMessage, otherAttributes = "") {
    if (inputValid) {
        Web.returnHtml(res, inputError({ otherAttributes }));
    } else {
        Web.returnHtml(res, inputError({ errorMessage, otherAttributes }));
    }
}

function inputError({ errorMessage = "", otherAttributes = "" } = {}) {
    return `<input-error ${errorMessage ? `message="${errorMessage}"` : ""} ${otherAttributes}></input-error>`;
}

router.post(`/validate-name`, (req, res) => {
    const name = req.body.name;
    returnInputErrorIf(res, isOrderNameValid(name), `Order name needs to be between ${MIN_NAME_LEN} and ${MAX_NAME_LEN} characters`);
});

router.post(`/validate-description`, (req, res) => {
    const description = req.body.description;
    returnInputErrorIf(res, isOrderDescriptionValid(description), `Order description needs to be at most ${MAX_DESCRIPTION_LEN} characters, if defined`);
});

router.post(`/validate-secret`, (req, res) => {
    const id = req.body.id;
    if (isOrderIdValid(id)) {
        const secret = req.body.secret;
        returnInputErrorIf(res, isOrderSecretValid(id, secret), invalidOrderSecretMessage(id),
            `id=${SECRET_ERROR_ID}`);
    } else {
        returnInputErrorIf(res, false, "Valid id is required to validate order secret",
            `id=${SECRET_ERROR_ID}`);
    }
});

function invalidOrderSecretMessage(id) {
    return `Order secret needs to have id length, which is: ${id.length}`;
}

function isOrderIdValid(id) {
    return id && id.length >= MIN_ID_LEN && id.length <= MAX_ID_LEN;
}

function isOrderNameValid(name) {
    return name && name.length >= MIN_NAME_LEN && name.length <= MAX_NAME_LEN;
}

function isOrderDescriptionValid(description) {
    return !description || description.length <= MAX_DESCRIPTION_LEN;
}

function isOrderSecretValid(id, secret) {
    return secret && secret.length == id.length;
}
