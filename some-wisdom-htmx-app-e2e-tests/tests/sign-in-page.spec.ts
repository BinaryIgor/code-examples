import { test, expect } from '@playwright/test';
import * as PageExtensions from "./support/page-extensions";
import { SignInPage } from './sign-in-page';
import { nonexistentSignInUser, incorrectSignInUser } from "./support/test-data";

// Reset storage state for this file to avoid being authenticated
test.use({ storageState: { cookies: [], origins: [] } });

let signInPage: SignInPage;

test.beforeEach(async ({ page }) => {
    signInPage = new SignInPage(page);
    await signInPage.goto();
});

test('should land on sign in page and see app title and header', async ({ page }) => {
    await expect(page).toHaveTitle('Some Wisdom App');
    await expect(page.getByRole('heading', { name: "Let's get some wisdom", level: 1 })).toBeVisible();
});

test("should not allow to sign in without filling the form", async () => {
    await expect(signInPage.signInButton).toBeDisabled();
});

test("should show errors with invalid name and password keeping sign in disabled", async () => {
    await expect(signInPage.nameError).toBeHidden();
    await expect(signInPage.passwordError).toBeHidden();

    await signInPage.nameInput.fill("Ig");
    await signInPage.passwordInput.fill("Pas");

    await expect(signInPage.nameError).toBeVisible();
    await expect(signInPage.passwordError).toBeVisible();

    await expect(signInPage.signInButton).toBeDisabled();
});

test("should show error modal with name and password of nonexistent user", async ({ page }) => {
    await expect(PageExtensions.getErrorModal(page)).toBeHidden();

    await signInPage.signIn(nonexistentSignInUser);

    await expect(PageExtensions.getErrorModal(page)).toBeVisible();
    await expect(PageExtensions.getErrorModal(page)).toContainText("Given user doesn't exist");
});

test("should show error modal with incorrect name or password of existing user", async ({ page }) => {
    await expect(PageExtensions.getErrorModal(page)).toBeHidden();

    await signInPage.signIn(incorrectSignInUser);

    await expect(PageExtensions.getErrorModal(page)).toBeVisible();
    await expect(PageExtensions.getErrorModal(page)).toContainText('Incorrect password');
});