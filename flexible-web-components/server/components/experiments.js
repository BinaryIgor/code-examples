import express from "express";
import * as Web from '../shared/web.js';

export const NAME = "experiments";
export const PATH = "/experiments";

export const router = express.Router();

router.get("/", (req, res) => {
    const body = `
    <div id="custom-message-container">
      <custom-message category="Curiosities" message="Some interesting message">
      </custom-message>
    </div>
    `;

    const script = `
        const customMessage = document.querySelector("custom-message");
        const customMessageContainer = document.getElementById("custom-message-container");
        
        setTimeout(() => {
            customMessage.setAttribute("category", "Another category");
            customMessage.setAttribute("message", "Another message");
        }, 1000);

        setTimeout(() => {
            customMessageContainer.innerHTML = "Gone";
        }, 2000);
    `;

    Web.returnHtml(res, Web.htmlPage(body, NAME, script));
});
