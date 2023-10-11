import { test, expect, Page, Locator } from '@playwright/test';
import * as PageExtensions from "./page-extensions";
import { nonExistingSignInUser, incorrectSignInUser } from "./test-data";
import { signInUser } from './test-data';

let homePage: HomePage;

test.describe('home page', () => {
    test.beforeEach(async ({ page }) => {
        homePage = new HomePage(page);
        await homePage.goto();
    });

    test('should show home page for signed-in user', async ({ page }) => {
        page.getByText(`${signInUser.name}`);
        page.getByText(/What authors/);
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
}