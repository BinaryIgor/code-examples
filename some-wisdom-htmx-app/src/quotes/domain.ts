import { Errors, AppError, OptionalErrorCode } from "../shared/errors";
import * as Validator from "../shared/validator";

const MIN_NOTE_LENGTH = 3;
const MAX_NOTE_LENGTH = 1000;

export class QuoteNoteService {

    constructor(private readonly repository: QuoteNoteRepository) { }

    //TODO: better validation!
    async createNote(quoteNote: NewQuoteNote) {
        const quoteError = this.validateQuoteNote(quoteNote.note);
        AppError.throwIfThereAreErrors(quoteError);

        await this.repository.create(quoteNote);
    }

    validateQuoteNote(note: string): OptionalErrorCode {
        return Validator.hasAnyContent(note) && Validator.hasLength(note, MIN_NOTE_LENGTH, MAX_NOTE_LENGTH) ?
            null : Errors.INVALID_QUOTE_NOTE_CONTENT;
    }

    async notesOfQuoteSortedByTimestamp(quoteId: number, ascending: boolean = false): Promise<QuoteNote[]> {
        function ascendingSort(a: QuoteNote, b: QuoteNote): number {
            if (a.timestamp > b.timestamp) {
                return 1;
            }
            if (b.timestamp > a.timestamp) {
                return -1;
            }
            return 0;
        }

        function descendingSort(a: QuoteNote, b: QuoteNote): number {
            if (a.timestamp > b.timestamp) {
                return -1;
            }
            if (b.timestamp > a.timestamp) {
                return 1;
            }
            return 0;
        }

        const sort = ascending ? ascendingSort : descendingSort;

        return (await this.repository.allOfQuote(quoteId)).sort(sort);
    }

    async notesOfQuoteCount(quoteId: number): Promise<number> {
        return (await this.repository.allOfQuote(quoteId)).length;
    }

    async deleteNote(noteId: number, userId: number): Promise<number> {
        const note = await this.repository.ofId(noteId);
        if (!note) {
            throw AppError.ofSingleError(Errors.QUOTE_NOTE_DOES_NOT_EXIST);
        }

        if (note.noteAuthorId != userId) {
            throw AppError.ofSingleError(Errors.NOT_USER_QUOTE_NOTE);
        }

        await this.repository.delete(noteId);

        return note.quoteId;
    }
}

export interface QuoteNoteRepository {

    create(newNote: NewQuoteNote): Promise<number>

    ofId(noteId: number): Promise<QuoteNote | null>

    allOfQuote(quoteId: number): Promise<QuoteNote[]>

    delete(noteId: number): Promise<void>
}

export class NewQuoteNote {
    constructor(readonly quoteId: number,
        readonly note: string,
        readonly noteAuthorId: number,
        readonly timestamp: number) { }
}

export class QuoteNote {
    constructor(readonly noteId: number,
        readonly quoteId: number,
        readonly note: string,
        readonly noteAuthorId: number,
        readonly timestamp: number) { }
}