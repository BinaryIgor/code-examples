import { test, expect, Page, Locator } from '@playwright/test';
import * as TestData from './test-data';

let indexPage: IndexPage;

test.beforeEach(async ({ page, request }) => {
    await request.post('/_import/items', {
        data: TestData.items
    });

    indexPage = new IndexPage(page);
    await indexPage.goto();
});

test('should have title', async ({ page }) => {
    await expect(page).toHaveTitle('HTMX - single index.html page');
});

test('should show items in the list', async () => {
    await expect(indexPage.itemsHeader).toBeVisible();

    await expect(indexPage.item).toHaveCount(TestData.items.length);
    await expect(indexPage.item).toHaveText(TestData.stringifyItems(TestData.items));
});

test('should allow to reverse items in the list', async () => {
    await indexPage.reverseItemsButton.click();

    const reversedItems = [...TestData.items];
    reversedItems.reverse();

    await expect(indexPage.item).toHaveCount(reversedItems.length);
    await expect(indexPage.item).toHaveText(TestData.stringifyItems(reversedItems));
});

test('should not allow to add item without required fields', async () => {
    await expect(indexPage.addItemErrors).toHaveCount(0);

    await indexPage.addItemButton.click();

    await expect(indexPage.addItemErrors).toBeVisible();

    await indexPage.addItemNameInput.fill("Some name");

    await indexPage.addItemButton.click();

    await expect(indexPage.addItemErrors).toBeVisible();
});

test('should allow to add items to the list', async () => {
    await expect(indexPage.addItemHeader).toBeVisible();

    const items = [...TestData.stringifyItems(TestData.items)];

    await expect(indexPage.item).toHaveText(items);

    const toAddItem1 = TestData.toAddItems[0];

    await indexPage.addItem(toAddItem1);

    items.push(toAddItem1.toString());

    await expect(indexPage.item).toHaveText(items);

    const toAddItem2 = TestData.toAddItems[1];

    await indexPage.addItem(toAddItem2);

    items.push(toAddItem2.toString());

    await expect(indexPage.item).toHaveText(items);
});

class IndexPage {

    readonly page: Page;
    readonly itemsHeader: Locator;
    readonly item: Locator;
    readonly reverseItemsButton: Locator;
    readonly addItemHeader: Locator;
    readonly addItemErrors: Locator;
    readonly addItemNameInput: Locator;
    readonly addItemValueInput: Locator;
    readonly addItemButton: Locator;

    constructor(page: Page) {
        this.page = page;

        this.itemsHeader = page.getByRole("heading", { level: 2, name: "items" });
        this.item = page.getByTestId("item");
        this.reverseItemsButton = page.getByRole("button", { name: "reverse items" });

        this.addItemHeader = page.getByRole("heading", { level: 2, name: "Add item" });
        this.addItemErrors = page.locator("#errors-container").filter({ hasText: "Item name and value are required!" });
        this.addItemNameInput = page.locator("[name='name']");
        this.addItemValueInput = page.locator("[name='value']");
        this.addItemButton = page.getByRole("button", { name: "add item" });
    }

    async goto() {
        await this.page.goto("");
    }

    async addItem(item: TestData.Item) {
        await this.addItemNameInput.fill(item.name);
        await this.addItemValueInput.fill(item.value);
        await this.addItemButton.click();
    }
}