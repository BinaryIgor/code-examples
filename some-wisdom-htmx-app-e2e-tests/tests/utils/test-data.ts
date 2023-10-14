export class SignInUser {
    constructor(readonly name: string, readonly password: string) { }
}

export const signInUser = new SignInUser("Igor", "password1");

export const incorrectSignInUser = new SignInUser("Igor", "password34");
export const nonExistingSignInUser = new SignInUser("User", "password34");

export const authors = [
    "Friedrich Nietzsche",
    "Jordan Peterson",
    "Saifedean Ammous",
    "Ayn Rand",
    "Marcus Aurelius"
];
export const phrasesMatchingAuthors = {
    "f": [authors[0], authors[2]],
    "jordan peterson": [authors[1]],
    "AYN": [authors[3]],
    "Aur": [authors[4]],
    "NonExisting": []
};
