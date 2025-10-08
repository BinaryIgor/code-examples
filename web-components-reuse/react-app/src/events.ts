export function showErrorModal(error: string) {
    document.dispatchEvent(new CustomEvent('events.show-error-modal', {
        detail: { error }
    }));
}