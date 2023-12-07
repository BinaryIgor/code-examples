function showCustomModal(itemId, itemName) {
    window.dispatchEvent(new CustomEvent("show-custom-modal",
        {
            detail: {
                title: "Custom title",
                message: `Custom message from item of ${itemId} id and ${itemName} name`
            }
        }));
}

console.log("HTMX with web components!");