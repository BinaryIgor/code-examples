import { Page, Locator} from '@playwright/test';

export function getByNameAttribute(page: Page, name: string): Locator {
    return page.locator(`[name='${name}']`);
}

export function getErrorModal(page: Page): Locator {
    return page.locator('#error-modal-content').filter({hasText: 'Something went wrong'});
}