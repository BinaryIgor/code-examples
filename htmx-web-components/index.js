
console.log("HTMX with Web Components!");

window.addEventListener("item-element-clicked", e => {
    console.log(`Clicked on the item:`, e.detail);
});