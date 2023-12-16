import express from "express";
import * as Web from '../shared/web.js';

export const NAME = "confirmable-modal";
export const PATH = "/confirmable-modal";

export const router = express.Router();

router.get("/", (req, res) => {
    const body = `
    <confirmable-modal title="Delete confirmation" ok-text="Delete">
    </confirmable-modal>

    <button hx-delete="${PATH}/test"
        hx-confirm="Are you sure to delete this test entity?"
        hx-target="#delete-result">
        Try to confirm
    </button>

    <div id="delete-result" class="text-xl font-bold mt-8 text-red-400"></div>
    `;

    const script = `
        const confirmableModal = document.querySelector("confirmable-modal");

        document.addEventListener("htmx:confirm", e => {
            console.log("Let's confirm htmx request..", e);
            
            // do not issue htmx request
            e.preventDefault();

            confirmableModal.onOk = () => {
                e.detail.issueRequest(e);
                confirmableModal.hide();
            };

            confirmableModal.show({ message: e.detail.question });
        });
    `;

    Web.returnHtml(res, Web.htmlPage(body, NAME, script));
});

router.delete(`/test`, (req, res) => {
    Web.returnHtml(res, `Delete was confirmed: ${new Date().toISOString()}!`);
});
