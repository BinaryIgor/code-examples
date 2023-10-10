import { test, expect } from '@playwright/test';

test.describe('sign in page', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto("http://localhost:8080");
    });

    test('has App title and the header', async ({ page }) => {
        await expect(page).toHaveTitle(/Some Wisdom/);
        await expect(page.getByRole('heading', { name: "let's get some wisdom", level: 1 })).toBeVisible();
    });

    test("can't sign-in without filling the form", async ({ page }) => {
        await expect(page.getByRole("button", { name: "sign in" })).toBeDisabled();
    });

    test("shows errors with invalid name and password", async ({page}) => {
        await page.locator("[name='name']").fill("Ig");
        await page.locator("[name='password']").fill("Pas");

        await expect(page.getByText(/Name should have/)).toBeVisible();
        await expect(page.getByText(/Password should have/)).toBeVisible();
    })
});