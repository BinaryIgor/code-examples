
import { Router, Request, Response } from "express";
import * as Web from "../shared/web";
import * as Views from "../shared/views";
import * as QuoteViews from "./views";
import { QuoteNoteView } from "./views";
import * as AuthWeb from "../auth/web";
import { Quote } from "../authors/domain";
import { FileQuoteNoteRepository } from "./repository";
import { NewQuoteNote, QuoteNoteService } from "./domain";
import * as Mapper from "./mapper";
import { User } from "../user/domain";

const QUOTES_ENDPOINT = "/quotes";
const QUOTE_NOTES_ENDPOINT_PART = "notes";
const QUOTES_NOTES_PREFIX = `${QUOTES_ENDPOINT}/${QUOTE_NOTES_ENDPOINT_PART}`
const QUOTE_NOTES_VALIDATE_NOTE_ENDPOINT = `${QUOTES_NOTES_PREFIX}/validate-note`;
const QUOTE_NOTES_VALIDATE_AUTHOR_ENDPOINT = `${QUOTES_NOTES_PREFIX}/validate-author`;

export function quoteEndpoint(quoteId: number): string {
    return `${QUOTES_ENDPOINT}/${quoteId}`;
}

export function build(
    quoteNotesDbFilePath: string,
    quoteOfId: (quoteId: number) => Quote | null,
    usersOfIds: (ids: number[]) => Map<number, User>): QuoteModule {

    const quoteNoteRepository = new FileQuoteNoteRepository(quoteNotesDbFilePath);
    const quoteNoteService = new QuoteNoteService(quoteNoteRepository);

    const router = Router()

    //TODO: fix it!
    router.get(`${QUOTES_ENDPOINT}/:id`,
        Web.asyncHandler(async (req: Request, res: Response) => {
            const quoteId = Web.numberPathParam(req, "id");

            const quote = quoteOfId(quoteId);
            if (quote) {
                const { notes, deleteableNoteIds } = await quoteNoteViews(AuthWeb.currentUserOrThrow(req).id, quoteId);
                Web.returnHtml(res, QuoteViews.quotePage({
                    author: quote.author,
                    quote: quote.content,
                    notes: notes,
                    deletableNoteIds: deleteableNoteIds,
                    deleteQuoteNoteEndpoint: deleteQuoteNoteEndpoint,
                    addQuoteNoteEndpoint: addQuoteNoteEndpoint(quoteId),
                    validateQuoteNoteEndpoint: QUOTE_NOTES_VALIDATE_NOTE_ENDPOINT,
                    validateQuoteAuthorEndpoint: QUOTE_NOTES_VALIDATE_AUTHOR_ENDPOINT,
                    renderFullPage: Web.shouldReturnFullPage(req),
                    currentUser: AuthWeb.currentUserName(req)
                }), Views.TRIGGERS.resetScroll);
            } else {
                Web.returnNotFound(res);
            }
        }));

    async function quoteNoteViews(currentUserId: number, quoteId: number): Promise<{ notes: QuoteNoteView[], deleteableNoteIds: number[] }> {
        const notes = await quoteNoteService.notesOfQuoteSortedByTimestamp(quoteId);
        const authorIds = notes.map(n => n.noteAuthorId);
        const authors = usersOfIds(authorIds);

        const deleteableNoteIds = notes.filter(n => n.noteAuthorId == currentUserId).map(n => n.noteId);

        return { notes: Mapper.toQuoteNoteViews(notes, authors), deleteableNoteIds }
    }

    function addQuoteNoteEndpoint(quoteId: number): string {
        return `${QUOTES_ENDPOINT}/${quoteId}/${QUOTE_NOTES_ENDPOINT_PART}`;
    }

    function deleteQuoteNoteEndpoint(noteId: number): string {
        return `${QUOTES_ENDPOINT}/${QUOTE_NOTES_ENDPOINT_PART}/${noteId}`;
    }

    router.post(`${QUOTES_ENDPOINT}/:id/${QUOTE_NOTES_ENDPOINT_PART}`,
        Web.asyncHandler(async (req: Request, res: Response) => {
            const quoteId = Web.numberPathParam(req, "id");

            const input = req.body as QuoteNoteInput;
            const author = AuthWeb.currentUserOrThrow(req);

            const note = new NewQuoteNote(quoteId, input.note, author.id, Date.now());

            await quoteNoteService.createNote(note);

            const { notes: newNotes, deleteableNoteIds } = await quoteNoteViews(author.id, quoteId);

            Web.returnHtml(res, QuoteViews.quoteNotesPage(newNotes, deleteableNoteIds, deleteQuoteNoteEndpoint, true),
                Views.resetFormTrigger(QuoteViews.LABELS.quoteNoteForm));
        }));


    router.post(QUOTE_NOTES_VALIDATE_NOTE_ENDPOINT, (req: Request, res: Response) => {
        const input = req.body as QuoteNoteInput;
        const noteError = quoteNoteService.validateQuoteNote(input.note);
        Web.returnHtml(res, Views.inputErrorIf(noteError),
            Views.formValidatedTrigger(QuoteViews.LABELS.quoteNoteForm,
                noteError == null));
    });

    router.delete(`${QUOTES_ENDPOINT}/${QUOTE_NOTES_ENDPOINT_PART}/:id`,
        Web.asyncHandler(async (req: Request, res: Response) => {
            const quoteNoteId = Web.numberPathParam(req, "id");

            const author = AuthWeb.currentUserOrThrow(req);

            const deletedNoteQuoteId = await quoteNoteService.deleteNote(quoteNoteId, author.id);

            const quoteNotesCount = await quoteNoteService.notesOfQuoteCount(deletedNoteQuoteId);

            Web.returnHtml(res, QuoteViews.quoteNotesSummaryComponent(quoteNotesCount));
        }));

    return new QuoteModule(router);
}

class QuoteNoteInput {
    constructor(readonly note: string) { }
}

export class QuoteModule {
    constructor(readonly router: Router) { }
}