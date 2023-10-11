import { test, expect, Page, Locator } from '@playwright/test';
import * as PageExtensions from "./page-extensions";
import { nonExistingSignInUser, incorrectSignInUser } from "./test-data";

let signInPage: SignInPage;

test.describe('sign in page', () => {
    test.beforeEach(async ({ page }) => {
        signInPage = new SignInPage(page);
        await signInPage.goto();
    });

    test('should have App title and the header', async ({ page }) => {
        await expect(page).toHaveTitle(/Some Wisdom/);
        await expect(page.getByRole('heading', { name: "let's get some wisdom", level: 1 })).toContainText("");
    });

    test("should not allow to sign in without filling the form", async () => {
        await expect(signInPage.signInButton()).toBeDisabled();
    });

    test("should show errors with invalid name and password keeping sign in disabled", async () => {
        await expect(signInPage.nameError()).toBeHidden();
        await expect(signInPage.passwordError()).toBeHidden();

        await signInPage.nameInput().fill("Ig");
        await signInPage.passwordInput().fill("Pas");

        await expect(signInPage.nameError()).toBeVisible();
        await expect(signInPage.passwordError()).toBeVisible();

        await expect(signInPage.signInButton()).toBeDisabled();
    });

    test("should show error modal with name and password of non-existing user", async ({ page }) => {
        await expect(PageExtensions.getErrorModal(page)).toBeHidden();

        await signInPage.nameInput().fill(nonExistingSignInUser.name);
        await signInPage.passwordInput().fill(nonExistingSignInUser.password);

        await signInPage.signInButton().click();

        await expect(PageExtensions.getErrorModal(page)).toBeVisible();
        await expect(PageExtensions.getErrorModal(page)).toContainText(/Given user doesn't exist/);
    });

    test("should show error modal with incorrect name or password of existing user", async ({ page }) => {
        await expect(PageExtensions.getErrorModal(page)).toBeHidden();

        await signInPage.nameInput().fill(incorrectSignInUser.name);
        await signInPage.passwordInput().fill(incorrectSignInUser.password);

        await signInPage.signInButton().click();

        await expect(PageExtensions.getErrorModal(page)).toBeVisible();
        await expect(PageExtensions.getErrorModal(page)).toContainText(/Incorrect password/);
    });
});

class SignInPage {
    readonly page: Page;

    constructor(page: Page) {
        this.page = page;
    }

    async goto() {
        await this.page.goto("http://localhost:8080");
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
        return this.page.getByText(/Name should have/);
    }

    passwordError(): Locator {
        return this.page.getByText(/Password should have/);
    }
}