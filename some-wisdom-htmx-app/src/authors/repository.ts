import { randomNumber } from "../shared/utils";
import { Author, AuthorRepository, Quote, QuoteRepository } from "./domain";

export class InMemoryAuthorRepository implements AuthorRepository {

    private readonly authors: Author[] = [];
    private readonly quoteRepository: InMemoryQuoteRepository;

    constructor(quoteRepository: InMemoryQuoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    create(author: Author) {
        this.authors.push(author);
        author.quotes.forEach(q => this.quoteRepository.create(q));
    }

    search(query: string): Author[] {
        const loweredQuery = query.toLowerCase();
        return this.authors.filter(a => a.name.toLowerCase().includes(loweredQuery));
    }

    ofName(name: string): Author | null {
        for (let a of this.authors) {
            if (a.name == name) {
                return a;
            }
        }
        return null;
    }

    random(size: number): Author[] {
        if (size >= this.authors.length) {
            return this.authors;
        }

        const maxStartIdx = this.authors.length - size;
        const startIdx = randomNumber(0, maxStartIdx);

        return this.authors.slice(startIdx, startIdx + size);
    }
}

export class InMemoryQuoteRepository implements QuoteRepository {

    private readonly quotes = new Map<number, Quote>();

    create(quote: Quote) {
        this.quotes.set(quote.id, quote);
    }

    ofId(id: number): Quote | null {
        return this.quotes.get(id) ?? null;
    }
}