import { Page, Locator } from '@playwright/test';
import * as PageExtensions from "./support/page-extensions";

export class SignInPage {

    readonly page: Page;
    readonly nameInput: Locator;
    readonly nameError: Locator;
    readonly passwordInput: Locator;
    readonly passwordError: Locator;
    readonly signInButton: Locator;

    constructor(page: Page) {
        this.page = page;

        this.nameInput = PageExtensions.getByNameAttribute(this.page, "name");
        this.nameError = this.page.getByText(/Name should have (.+) characters/);

        this.passwordInput = PageExtensions.getByNameAttribute(this.page, "password");
        this.passwordError = this.page.getByText(/Password should have (.+) characters/);

        this.signInButton = page.getByRole("button", { name: "Sign In" });
    }

    async goto() {
        await this.page.goto("/");
    }

    async signIn(user: { name: string, password: string }) {
        await this.nameInput.fill(user.name);
        await this.passwordInput.fill(user.password);
        await this.signInButton.click();
    }
}