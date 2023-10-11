export class SignInUser {
    constructor(readonly name: string, readonly password: string) { }
}

export const signInUser = new SignInUser("Igor", "password1");
export const incorrectSignInUser = new SignInUser("Igor", "password34");
export const nonExistingSignInUser = new SignInUser("User", "password34");