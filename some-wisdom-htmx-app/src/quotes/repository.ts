import { NewQuoteNote, QuoteNote, QuoteNoteRepository } from "./domain";
import * as Files from "../shared/files";

export class FileQuoteNoteRepository implements QuoteNoteRepository {

    constructor(private readonly dbFilePath: string) { }

    async create(newNote: NewQuoteNote): Promise<number> {
        const quoteNotes = await this.allQuoteNotesFromDb();
        
        let nextQuoteNoteId: number;
        if (quoteNotes.length > 0) {
            nextQuoteNoteId = Math.max(...quoteNotes.map(q => q.noteId)) + 1;
        } else {
            nextQuoteNoteId = 1;
        }

        const note = new QuoteNote(nextQuoteNoteId, 
            newNote.quoteId, newNote.note, newNote.noteAuthorId, newNote.timestamp);

        quoteNotes.push(note);

        await Files.writeTextFileContent(this.dbFilePath, JSON.stringify(quoteNotes));

        return nextQuoteNoteId;
    }

    private async allQuoteNotesFromDb(): Promise<QuoteNote[]> {
        const dbExists = await Files.fileExists(this.dbFilePath);
        if (!dbExists) {
            return [];
        }
        try {
            return JSON.parse(await Files.textFileContent(this.dbFilePath));
        } catch(e) {
            console.error("Problem while reading quotes db...", e);
            throw e;
        }
    }

    async ofId(noteId: number): Promise<QuoteNote | null> {
        const notes = await this.allQuoteNotesFromDb();
        for (const n of notes) {
            if (n.noteId == noteId) {
                return n;
            }
        }
        return null;
    }

    async allOfQuote(quoteId: number): Promise<QuoteNote[]> {
       return (await this.allQuoteNotesFromDb()).filter(q => q.quoteId == quoteId);

    }
    
    async delete(noteId: number): Promise<void> {
        const quoteNotes = await this.allQuoteNotesFromDb();
        
        const quoteNotesWithoutRemovedOne = quoteNotes.filter(q => q.noteId != noteId);

        if (quoteNotesWithoutRemovedOne.length < quoteNotes.length) {
            await Files.writeTextFileContent(this.dbFilePath, JSON.stringify(quoteNotesWithoutRemovedOne));
        }
    }
    
}