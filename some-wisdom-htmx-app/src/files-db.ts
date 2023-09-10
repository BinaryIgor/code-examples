import { Author, Quote } from "./authors/domain";
import { User } from "./user/domain";
import { UserClient } from "./user/module";
import { AuthorClient } from "./authors/module";

class AuthorToImport {
    constructor(readonly name: string,
        readonly note: string[],
        readonly quotes: string[]) { }
}

export function importAuthors(dbJson: string, client: AuthorClient) {
    const authorsFromDb = JSON.parse(dbJson);

    console.log(`Db loaded, we have ${authorsFromDb.length} authors!`);

    let nextQuoteId = 1;

    for (let a of authorsFromDb) {
        const toImport = a as AuthorToImport;

        const quotes =  toImport.quotes.map(q => {
            const quote = new Quote(nextQuoteId, toImport.name, q);
            nextQuoteId++;
            return quote;
        });

        client.create(new Author(toImport.name, toImport.note.join(""), quotes));
    }
}

export function importUsers(dbJson: string, client: UserClient) {
    const usersFromDb = JSON.parse(dbJson);

    console.log(`Db loaded, we have ${usersFromDb.length} users!`);

    for (let u of usersFromDb) {
        client.create(u as User);
    }
}