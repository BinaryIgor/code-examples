import { test as setup } from '@playwright/test';
import { SignInPage } from './sign-in-page';
import { signInUser } from "./utils/test-data";

const AUTH_FILE = "playwright/.auth/user.json";

setup('authenticate', async ({ page }) => {
    const signInPage = new SignInPage(page);

    await signInPage.goto();

    await page.waitForURL(/user\/sign-in/);

    await signInPage.nameInput().fill(signInUser.name);
    await signInPage.passwordInput().fill(signInUser.password);
    await signInPage.signInButton().click();

    await page.getByText("Some Wisdom App").waitFor();

    await page.context().storageState({ path: AUTH_FILE });
});