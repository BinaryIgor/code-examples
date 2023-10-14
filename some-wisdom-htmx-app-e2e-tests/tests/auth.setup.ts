import { test as setup } from '@playwright/test';
import { SignInPage } from './sign-in-page';
import { signInUser } from "./support/test-data";

const AUTH_FILE = "playwright/.auth/user.json";

setup('authenticate', async ({ page }) => {
    const signInPage = new SignInPage(page);

    await signInPage.goto();

    await page.waitForURL(/user\/sign-in/);

    await signInPage.signIn(signInUser);

    await page.waitForURL("/");

    await page.context().storageState({ path: AUTH_FILE });
});