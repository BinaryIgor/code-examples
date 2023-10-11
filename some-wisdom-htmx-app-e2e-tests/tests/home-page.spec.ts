import { test, expect, Page, Locator } from '@playwright/test';
import * as PageExtensions from "./page-extensions";
import { nonExistingSignInUser, incorrectSignInUser, authors, nonExistingAuthorPrefix } from "./test-data";
import { signInUser } from './test-data';

let homePage: HomePage;

test.describe('home page', () => {
    test.beforeEach(async ({ page }) => {
        homePage = new HomePage(page);
        await homePage.goto();
    });

    test('should show home page for signed-in user', async ({ page }) => {
        await expect(page.getByText(`${signInUser.name}`)).toBeVisible();
        page.getByText(/What authors/);
    });

    test('should allow to search authors', async ({ page }) => {
        await homePage.expectNoSearchResults();

        await homePage.searchAuthorsInput().fill(nonExistingAuthorPrefix);

        await homePage.expectNoSearchResults();
    });
});


class HomePage {
    readonly page: Page;

    constructor(page: Page) {
        this.page = page;
    }

    async goto() {
        await this.page.goto(PageExtensions.ROOT_URL);
    }

    searchAuthorsInput(): Locator {
        return PageExtensions.getByNameAttribute(this.page, "authors-search");
    }

    searchResults(): Locator {
        return this.page.locator("#search-results");
    }

    async expectNoSearchResults() {
        await expect(this.searchResults()).toHaveCount(1);
    }
}