import { Page, Locator } from '@playwright/test';
import * as PageExtensions from "./page-extensions";

export class SignInPage {
    readonly page: Page;

    constructor(page: Page) {
        this.page = page;
    }

    async goto() {
        await this.page.goto("/");
    }

    signInButton(): Locator {
        return this.page.getByRole("button", { name: "sign in" });
    }

    nameInput(): Locator {
        return PageExtensions.getByNameAttribute(this.page, "name");
    }

    passwordInput(): Locator {
        return PageExtensions.getByNameAttribute(this.page, "password");
    }

    nameError(): Locator {
        return this.page.getByText('Name should have');
    }

    passwordError(): Locator {
        return this.page.getByText('Password should have');
    }
}