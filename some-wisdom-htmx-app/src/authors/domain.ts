import { randomNumber } from "../shared/utils";

export class AuthorService {

    authorsWithRandomQuotes(authors: Author[]): AuthorWithRandomQuote[] {
        return authors.map(a => {
            let quote: Quote;
            if (a.quotes.length > 1) {
                quote = a.quotes[randomNumber(0, a.quotes.length - 1)];
            } else {
                quote = a.quotes[0];
            }
            return new AuthorWithRandomQuote(a.name, quote);
        })
    }
}

export interface AuthorRepository {

    create(author: Author): void

    search(query: string): Author[]

    ofName(name: string): Author | null

    random(size: number): Author[]
}

export interface QuoteRepository {
    ofId(id: number): Quote | null
}

export class Author {
    constructor(readonly name: string,
        readonly note: string,
        readonly quotes: Quote[]) { }
}

export class Quote {
    constructor(readonly id: number, readonly author: string, readonly content: string) { }
}

export class AuthorWithRandomQuote {
    constructor(readonly name: string, readonly quote: Quote) { }
}