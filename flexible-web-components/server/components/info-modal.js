import express from "express";
import * as Web from '../shared/web.js';

export const NAME = "info-modal";
export const PATH = "/info-modal";

export const router = express.Router();

router.get("/", (req, res) => {
    const body = `
    <info-modal id="error-modal" class-title-add="text-red-500">
    </info-modal>

    <info-modal id="info-modal" 
        class-container="bg-black/80"
        class-content="bg-amber-300 border-solid border-4 border-black rounded-md m-auto mt-32 px-8 pt-8 pb-32 w-3/5"
        class-message="text-lg italic"
        close-icon="&#10006;"
        class-close="text-2xl p-4 cursor-pointer">
    </info-modal>

    <button hx-post="${PATH}/error" class="rounded border-2 border-bg-slate-200 p-2">
        Trigger Error Modal
    </button>

    <button hx-post="${PATH}/info" class="rounded border-2 border-bg-slate-200 p-2" hx-swap="none">
        Trigger Customized Info Modal
    </button>
    `;

    const script = `
        console.log("Launching some custom script");

        document.addEventListener("htmx:afterRequest", e => {
            console.log("After request", e);

            if (e.detail.successful) {
                window.dispatchEvent(new CustomEvent("show-info-modal", {
                    detail: {
                        targetId: "info-modal",
                        title: "Something was sent successfully...",
                        message: e.detail.xhr.response
                    }
                }));
            } else {
                window.dispatchEvent(new CustomEvent("show-info-modal", {
                    detail: {
                        targetId: "error-modal",
                        title: "Something went wrong...",
                        message: e.detail.xhr.response
                    }
                }));
            }
        });

        window.addEventListener("info-modal-hidden", e => {
            console.log("Info modal was hidden", e);
        });
    `;

    Web.returnHtml(res, Web.htmlPage(body, NAME, script));
});

router.post(`/error`, (req, res) => {
    Web.returnTextError(res, "Test error");
});

router.post(`/info`, (req, res) => {
    Web.returnText(res, "Test info");
});
