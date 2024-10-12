import bodyParser from "body-parser";
import express from "express";
import path from "path";
import * as Web from "./web.js";

import * as Books from "./books.js";

const SERVER_PORT = process.env.SERVER_PORT || 8080;
const DIST_DIR = process.env.DIST_DIR || path.join("..", "static");
const ASSETS_DIR = process.env.ASSETS_DIR || "../static";

const app = express();

app.use(bodyParser.urlencoded({ extended: true }));

app.use(Books.build());

app.get("*", Web.asyncHandler(async (req, res) => {
  if (req.url.includes("dist")) {
    const filePath = path.join(DIST_DIR, req.url.substring(1));
    await Web.returnFile(res, filePath);
  } else if (req.url.includes("assets")) {
    const filePath = path.join(ASSETS_DIR, req.url.substring(1));
    await Web.returnFile(res, filePath);
  } else {
    returnMainPage(req, res);
  }
}));

app.use((error, req, res, next) => {
  console.error("Something went wrong...", error);
  Web.returnFullOrPartialErrorHTML(req, res, "<div>Unknown Error</div>", 500);
});

function returnMainPage(req, res) {
  const categoriesHtml = Books.CATEGORIES.map(c => `<sl-option value="${c.value}">${c.name}</sl-option>`).join("\n");
  const pageHtml = `
    <div class="w-full h-full">
      <div class="w-full px-4 max-w-3xl absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
        <h1 class="text-2xl my-4 font-bold text">Books Category</h1>
        <sl-select>
          ${categoriesHtml}
        </sl-select>
        <sl-button class="my-8 hidden w-full" hx-target="#page">See Books</sl-button>
      </div>
      ${Web.scopedScript(`
        const categorySelect = document.querySelector("sl-select");
        const goToCategoryButton = document.querySelector("sl-button");
        let chosenCategory = ComponentsState.get("chosen-category");

        categorySelect.addEventListener("sl-change", e => {
          onChosenCategory(e.target.value);
        });

        function onChosenCategory(category) {
          goToCategoryButton.classList.remove("hidden");
          chosenCategory = category;
          const booksOfCategoryUrl = "/books/" + chosenCategory;
          goToCategoryButton.setAttribute("hx-push-url", true);
          goToCategoryButton.setAttribute("hx-get", booksOfCategoryUrl);
          htmx.process(goToCategoryButton);
          ComponentsState.set("chosen-category", chosenCategory);
        }
        
        if (chosenCategory) {
           categorySelect.value = chosenCategory;
           onChosenCategory(chosenCategory);
        }
        `)}
    </div>
    `;
  Web.returnFullOrPartialHTML(req, res, pageHtml);
}

app.listen(SERVER_PORT, () => {
  console.log(`Server has started on port ${SERVER_PORT}!`);
});

process.on('SIGTERM', () => {
  console.log("Received SIGTERM signal, exiting...");
  process.exit();
});

process.on('SIGINT', () => {
  console.log("Received SIGINT signal, exiting...");
  process.exit();
});