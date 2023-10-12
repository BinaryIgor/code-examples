import { test, expect, Page, Locator } from '@playwright/test';
import * as PageExtensions from "./page-extensions";
import { nonExistingSignInUser, incorrectSignInUser, authors, phrasesMatchingAuthors } from "./test-data";
import { signInUser } from './test-data';

let homePage: HomePage;

test.describe('home page', () => {
    test.beforeEach(async ({ page }) => {
        homePage = new HomePage(page);
        await homePage.goto();
    });

    test('should show home page for signed-in user', async ({ page }) => {
        await expect(page.getByText(`${signInUser.name}`)).toBeVisible();
        await expect(page.getByText(/What authors/)).toBeVisible();
    });

    test('should allow to search authors by a phrase', async () => {
        await expect(homePage.searchResults()).toBeHidden();
        await expect(homePage.searchResultsIndicator()).toBeHidden();

        for (const [phrase, mathes] of Object.entries(phrasesMatchingAuthors)) {
            await homePage.searchAuthorsInput().fill("");
            await homePage.searchAuthorsInput().pressSequentially(phrase);

            await expect(homePage.searchResultsIndicator()).toBeVisible();

            await homePage.expectSearchResults(mathes);
        }
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
        return this.page.getByTestId("search-results");
    }

    searchResultsItem(): Locator {
        return this.page.getByTestId("search-results-item");
    }

    noSearchResults(): Locator {
        return this.page.getByTestId("no-search-results");
    }

    searchResultsIndicator(): Locator {
        return this.page.locator("#search-results-indicator").filter({ hasText: "Searching..." });
    }

    async expectSearchResults(authors: string[]) {
        if (authors.length == 0) {
            await expect(this.noSearchResults()).toContainText('There are no authors');
            return;
        }

        await expect(this.searchResults()).toBeVisible();
        await expect(this.searchResultsItem()).toHaveCount(authors.length);
        await expect(this.searchResultsItem()).toContainText(authors);
    }
}