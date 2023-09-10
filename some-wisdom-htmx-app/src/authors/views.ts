import { Translations } from "../shared/translations";
import * as Views from "../shared/views";
import { Author, AuthorWithRandomQuote } from "./domain";

export const AUTHORS_SEARCH_INPUT = "authors-search";
const AUTHOR_QUOTE_PREVIEW_MAX_LENGTH = 300;

const REFRESH_ICON = "&#8635;";

export function homePage(
    suggestedAuthors: string[],
    searchAuthorsEndpoint: string,
    currentUser: string | null,
    withSwappedNavigation: boolean,
    renderFullPage: boolean): string {
    const homePageTranslations = Translations.defaultLocale.homePage;

    const authorsList = suggestedAuthors.map(a => `<li class="ml-4 font-medium">${a}</li>`).join("\n");

    const searchResultsId = "search-results";
    const searchAuthorsInputId = "search-authors-input";

    const page = Views.wrappedInToLeftDiv(`${withSwappedNavigation ? Views.navigationComponent(currentUser, true): ""}
    <h1 class="m-2 text-xl">${homePageTranslations.header}</h1>

    <div class="m-4">
        ${homePageTranslations.suggestion}
        <ul class="m-2 list-disc">
            ${authorsList}
        </ul>
    </div>

    <div class="w-full p-4">
        <div class="relative">
            <input id="${searchAuthorsInputId}" 
                class="${Views.INPUT_LIKE_CLASSES} w-full" name="${AUTHORS_SEARCH_INPUT}" 
                placeholder="${homePageTranslations.searchPlaceholder}"
                hx-trigger="keyup changed delay:500ms" 
                hx-post="${searchAuthorsEndpoint}" 
                hx-target="#${searchResultsId}"
                hx-indicator="#search-results-indicator">
            <div class="text-2xl absolute top-2 right-2 cursor-pointer" 
                hx-post="${searchAuthorsEndpoint}" 
                hx-target="#${searchResultsId}"
                hx-include="#${searchAuthorsInputId}"
                hx-indicator="#search-results-indicator"
                hx-trigger="click">${REFRESH_ICON}</div>
        </div>
        <div id="search-results-indicator" class="load-indicator text-xl">
            ${homePageTranslations.searchLoader}
        </div>
        <div class="mt-2" id="${searchResultsId}"></div>
    </div>
    </div>`);
    return renderFullPage ? Views.wrappedInMainPage(page, currentUser) : page;
}

export function authorsSearchResult(result: AuthorWithRandomQuote[], authorEndpoint: (name: string) => string): string {
    const resultList = result.map(a => authorsSearchResultElement(a, authorEndpoint(a.name))).join('\n');

    let results;
    if (resultList) {
        results = resultList;
    } else {
        results = `<div class="px-4">${Translations.defaultLocale.homePage.noAuthors}</div>`;
    }

    return `<div class="space-y-4">${results}</div>`
}

function authorsSearchResultElement(author: AuthorWithRandomQuote, authorEndpoint: string): string {
    let quotePreview = author.quote.content;
    if (quotePreview.length > AUTHOR_QUOTE_PREVIEW_MAX_LENGTH) {
        quotePreview = `${quotePreview.substring(0, AUTHOR_QUOTE_PREVIEW_MAX_LENGTH)}...`;
    }

    return `<div class="rounded-lg shadow-md p-4 cursor-pointer border-2 
        ${Views.PROPS.borderColorSecondary2} ${Views.PROPS.shadowColorSecondary2}"
    hx-target="#${Views.ROOT_ID}" hx-get="${authorEndpoint}" hx-push-url="true">
        <div class="text-xl">${author.name}</div>
        <div class="${Views.PROPS.txtColorSecondary1} italic mt-2">"${quotePreview}"</div>
    </div>`
}

export function authorPage(author: Author, quoteEndpoint: (quoteId: number) => string,
    renderFullPage: boolean, currentUser: string | null): string {
    const quotes = author.quotes.map(q => `
        <div class="rounded-lg shadow p-8 cursor-pointer border-2 
        ${Views.PROPS.borderColorSecondary1} ${Views.PROPS.shadowColorSecondary2} 
        italic text-lg"
            hx-push-url="true" hx-target="#${Views.ROOT_ID}" hx-get="${quoteEndpoint(q.id)}">
            "${q.content}"
        </div>`)
        .join('\n');

    const page = Views.wrappedInToLeftDiv(`<div class="p-4">
        <h1 class="text-2xl">${author.name}</h1>
        <div class="p-4 my-4 rounded-md shadow-md ${Views.PROPS.shadowColorSecondary2} w-full
            ${Views.PROPS.bgColorSecondary1} ${Views.PROPS.txtColorSecondary1} 
            whitespace-pre-line">${author.note}</div>
        <h1 class="text-xl mt-8 mb-4">${Translations.defaultLocale.authorPage.quotes} (${author.quotes.length})</h1>
        <div class="space-y-4">
            ${quotes}
        </div>
    </div>
    `);

    return renderFullPage ? Views.wrappedInMainPage(page, currentUser) : page;
}