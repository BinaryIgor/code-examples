import { User } from "../user/domain";
import { QuoteNote } from "./domain";
import { QuoteNoteView } from "./views";

export function toQuoteNoteViews(quoteNotes: QuoteNote[], authors: Map<number, User>): QuoteNoteView[] {
    return quoteNotes.map(q => {
        const author = authors.get(q.noteAuthorId)?.name ?? "Anonymous";
        //TODO: prettier date format!
        const timestamp = new Date(q.timestamp);
        return new QuoteNoteView(q.noteId, q.quoteId, q.note, author, formattedDateTime(timestamp));
    });
}

function formattedDateTime(date: Date): string {
    let displayedHour: string | number = date.getHours();
    displayedHour = displayedHour < 10 ? '0' + displayedHour : displayedHour;
    let displayedMinute: string | number = date.getMinutes();
    displayedMinute = displayedMinute < 10 ? '0' + displayedMinute : displayedMinute;
    let displayedDay: string | number = date.getDate();
    displayedDay = displayedDay < 10 ? '0' + displayedDay : displayedDay;
    let displayedMonth: string | number = date.getMonth() + 1;
    displayedMonth = displayedMonth < 10 ? '0' + displayedMonth : displayedMonth;

    return `${displayedDay}.${displayedMonth}.${date.getFullYear()}, ${displayedHour}:${displayedMinute}`;
}