import { Translations } from "../shared/translations";
import * as Views from "../shared/views";

export const LABELS = {
    quoteNoteForm: "quote-note-form"
};

const QUOTE_NOTES_SUMMARY_ID = "quote-notes-summary";

export function quotePage(params: {
    author: string,
    quote: string,
    notes: QuoteNoteView[],
    deletableNoteIds: number[],
    deleteQuoteNoteEndpoint: (quoteNoteId: number) => string,
    addQuoteNoteEndpoint: string,
    validateQuoteNoteEndpoint: string,
    validateQuoteAuthorEndpoint: string,
    renderFullPage: boolean,
    currentUser: string | null
}): string {
    const addNoteButtonId = "add-note-button";
    const addNoteFormId = "add-note-form";
    const addNoteFormSubmitId = "add-note-form-submit";
    const notesListId = "notes-list";

    const pageTranslations = Translations.defaultLocale.quotePage;

    const page = Views.wrappedInCenteredDiv(`<div class="py-16 px-8 w-full ${Views.PROPS.bgColorSecondary1} italic
        shadow-md rounded-b-xl ${Views.PROPS.shadowColorSecondary1}">
        <p class="text-2xl">"${params.quote}"</p>
        <p class="text-xl font-bold text-right ${Views.PROPS.txtColorSecondary1} mt-8">${params.author}</p>
    </div>
        <div class="py-4 px-4 lg:px-0">
            <div class="flex justify-between my-4">
                <div>
                    ${quoteNotesSummaryComponent(params.notes.length, false)}
                </div>
                <button class="${Views.BUTTON_LIKE_CLASSES} px-12" id="${addNoteButtonId}">${pageTranslations.addQuote}</button>
            </div>
            <form id="${addNoteFormId}" class="py-4 ${Views.HIDDEN_CLASS}"
                hx-post="${params.addQuoteNoteEndpoint}"
                hx-target="#${notesListId}"
                ${Views.FORM_LABEL}="${LABELS.quoteNoteForm}"
                ${Views.CONFIRMABLE_ELEMENT_TITLE_LABEL}="${pageTranslations.confirmAddQuoteNoteTitle}"
                ${Views.CONFIRMABLE_ELEMENT_CONTENT_LABEL}="${pageTranslations.confirmAddQuoteNoteContent}">
                ${Views.textAreaWithHiddenError('note', pageTranslations.notePlaceholder,
        params.validateQuoteNoteEndpoint)}
                <div class="flex justify-end">
                    <input id="${addNoteFormSubmitId}" 
                        class="${Views.BUTTON_LIKE_CLASSES} py-4 px-12 ${Views.DISABLED_CLASS}"
                        type="submit" value="${pageTranslations.addQuote}"
                        ${Views.SUBMIT_FORM_LABEL}="${LABELS.quoteNoteForm}" disabled>
                </div>
            </form>
            ${quoteNotesPage(params.notes, params.deletableNoteIds, params.deleteQuoteNoteEndpoint, false)}
            </div>
        </div>
    </div>
    ${Views.inlineJs(`
        const addNoteForm = document.getElementById("${addNoteFormId}");
        const addNoteSubmit = document.getElementById("${addNoteFormSubmitId}");

        document.getElementById("${addNoteButtonId}").onclick = () => {
            addNoteForm.classList.toggle("${Views.HIDDEN_CLASS}");
        };
    `)}
    `);

    return params.renderFullPage ? Views.wrappedInMainPage(page, params.currentUser) : page;
}

export function quoteNotesSummaryComponent(quoteNotes: number, withExternalSwap: boolean = true): string {
    return `<p id="${QUOTE_NOTES_SUMMARY_ID}" ${Views.oobSwapIf(withExternalSwap)}
        class="text-xl mt-4 mb-4">${Translations.defaultLocale.quotePage.notes} (${quoteNotes})</p>`;
}

export function quoteNotesPage(quoteNotes: QuoteNoteView[],
    deleteableQuoteNoteIds: number[],
    deleteQuoteNoteEndpoint: (noteId: number) => string,
    withSummaryToSwap: boolean) {

    const pageTranslations = Translations.defaultLocale.quotePage;

    return `
    ${withSummaryToSwap ? quoteNotesSummaryComponent(quoteNotes.length, true) : ""}
    <div id="notes-list" class="space-y-4">
            ${quoteNotes.map(qn => {
        const noteElementId = `notes-list-element-${qn.noteId}`;
        let deleteEl: string;
        if (deleteableQuoteNoteIds.includes(qn.noteId)) {
            deleteEl = `<span class="text-4xl absolute top-0 right-2 
                cursor-pointer ${Views.PROPS.hoverTxtColorSecondary2}"
                   hx-swap="delete"
                   hx-target="#${noteElementId}"
                   hx-delete="${deleteQuoteNoteEndpoint(qn.noteId)}"
                   ${Views.CONFIRMABLE_ELEMENT_TITLE_LABEL}="${pageTranslations.confirmDeleteQuoteNoteTitle}"
                   ${Views.CONFIRMABLE_ELEMENT_CONTENT_LABEL}="${pageTranslations.confirmDeleteQuoteNoteContent}">${Views.CLOSE_ICON}</span>`;
        } else {
            deleteEl = "";
        }
        return `<div id="${noteElementId}" class="relative rounded-lg shadow p-8 cursor-pointer border-2 
            ${Views.PROPS.borderColorSecondary1} ${Views.PROPS.shadowColorSecondary2}">
                    <p class="italic text-lg whitespace-pre-line">"${qn.note}"</p>
                    <p class="mt-4 text-right"><span class="font-bold">${qn.noteAuthor}</span> ${pageTranslations.on} ${qn.timestamp}</p>
                    ${deleteEl}
                </div>`})
            .join('\n')}
        </div>`.trim();
}

export class QuoteNoteView {
    constructor(
        readonly noteId: number,
        readonly quoteId: number,
        readonly note: string,
        readonly noteAuthor: string,
        readonly timestamp: string) { }
}