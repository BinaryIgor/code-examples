const en = {
    appTitle: "Some Wisdom App",
    signIn: "Sign In",
    signInPage: {
        namePlaceholder: "Your name...",
        passwordPlaceholder: "Your password...",
        signInButton: "Sign In"
    },
    navigation: {
        profile: "Profile",
        signOut: "Sign Out"
    },
    homePage: {
        header: "What authors you are looking for?",
        suggestion: "If in doubt, some suggestions:",
        searchPlaceholder: "Search for interesting authors by their name...",
        searchLoader: "Searching...",
        noAuthors: "There are no authors maching your query, try something else!"
    },
    authorPage: {
        quotes: "Quotes"
    },
    quotePage: {
        confirmAddQuoteNoteTitle: "Quote note confirmation",
        confirmAddQuoteNoteContent: "Are you sure that you want to add this note to the quote?",
        notes: "Notes",
        addQuote: "Add",
        notePlaceholder: "Your note...",
        confirmDeleteQuoteNoteTitle: "Quote note confirmation",
        confirmDeleteQuoteNoteContent: "Are you sure that you want to delete this note from the quote?",
        on: "on"
    },
    confirmableModal: {
        cancel: "Cancel",
        ok: "Ok"
    },
    errorsModal: {
        header: "Something went wrong..."
    },
    errors: {
        INVALID_USER_NAME: "Name should have 3 - 30 characters",
        INVALID_USER_PASSWORD: "Password should have 8 - 50 characters",
        USER_DOES_NOT_EXIST: "Given user doesn't exist",
        INCORRECT_USER_PASSWORD: "Incorrect password",
        INVALID_QUOTE_NOTE_CONTENT: "Note can't be empty and needs to have 3 - 1000 characters",
        INVALID_QUOTE_NOTE_AUTHOR: "Note author can't be empty and needs to have 3 - 50 characters"
    }
};

export const Translations = {
    en: en,
    defaultLocale: en
};