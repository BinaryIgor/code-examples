customElements.define("simple-list-container", class extends HTMLElement {
    connectedCallback() {
        const itemIds = (this.getAttribute("item-ids") || "").split(",");
        const items = (this.getAttribute("items") || "").split(",");

        const zippedItems = itemIds.map((e, i) => [e, items[i]]);

        const list = zippedItems.map(i => {
            const [id, item] = i;
            return `<div class="py-2 px-4 text-lg cursor-pointer" id="${id}">${item}</div>`;
        }).join("\n");
        this.innerHTML = list;

        this.querySelectorAll("div").forEach(e => e.onclick = () => { 
            console.log(`${e} element clicked...`);
        });
    }
});