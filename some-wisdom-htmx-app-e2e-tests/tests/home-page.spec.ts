import { test, expect, Page, Locator } from '@playwright/test';
import * as PageExtensions from "./support/page-extensions";
import { authors, phrasesMatchingAuthors, signInUser } from "./support/test-data";

let homePage: HomePage;

test.beforeEach(async ({ page }) => {
    homePage = new HomePage(page);
    await homePage.goto();
});

test('should show home page for signed-in user', async ({ page }) => {
    await expect(page.getByText(signInUser.name)).toBeVisible();
    await expect(page.getByText('What authors you are looking for?')).toBeVisible();
    await expect(page.getByText('If in doubt, some suggestions')).toBeVisible();
});

test('should allow to search authors by a phrase', async () => {
    await expect(homePage.searchResults).toBeHidden();
    await expect(homePage.searchResultsIndicator).toBeHidden();

    for (const [phrase, matches] of Object.entries(phrasesMatchingAuthors)) {
        await homePage.searchAuthorsInput.fill(phrase);

        await expect(homePage.searchResultsIndicator).toBeVisible();

        await homePage.expectSearchResults(matches);
    }
});

test('should find and navigate to an author page', async ({ page }) => {
    const author = authors[0];

    await homePage.searchAuthorsInput.fill(author);

    const authorItem = homePage.searchResultsItem;

    await expect(authorItem).toHaveCount(1);
    await expect(authorItem).toContainText(author);

    await authorItem.click();

    await page.waitForURL(`/authors/${encodeURIComponent(author)}`);

    await expect(page.getByRole('heading').filter({ hasText: author })).toBeVisible();
});

class HomePage {

    readonly page: Page;
    readonly searchAuthorsInput: Locator;
    readonly searchResults: Locator;
    readonly searchResultsIndicator: Locator;
    readonly noSearchResults: Locator;
    readonly searchResultsItem: Locator;

    constructor(page: Page) {
        this.page = page;

        this.searchAuthorsInput = PageExtensions.getByNameAttribute(this.page, "authors-search");
        this.searchResults = this.page.getByTestId("search-results");

        this.searchResultsIndicator = this.page.locator("#search-results-indicator").filter({ hasText: "Searching..." });
        this.noSearchResults = this.page.getByTestId("no-search-results");

        this.searchResultsItem = this.page.getByTestId("search-results-item");
    }

    async goto() {
        await this.page.goto("/");
    }

    async expectSearchResults(authors: string[]) {
        if (authors.length == 0) {
            await expect(this.noSearchResults).toContainText('There are no authors');
            return;
        }

        await expect(this.searchResults).toBeVisible();
        await expect(this.searchResultsItem).toHaveCount(authors.length);
        await expect(this.searchResultsItem).toContainText(authors);
    }
}