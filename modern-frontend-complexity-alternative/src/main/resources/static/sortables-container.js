class SortablesContainer extends HTMLElement {

    connectedCallback() {
        const sortableColumnAscTemplateId = this.getAttribute("data-sortable-column-asc-template-id") ?? "sortable-column-asc-template";
        const sortableColumnDescTemplateId = this.getAttribute("data-sortable-column-desc-template-id") ?? "sortable-column-desc-template";

        // TODO: validate
        const sortableColumnAscTemplate = document.getElementById(sortableColumnAscTemplateId)?.innerHTML;
        const sortableColumnDescTemplate = document.getElementById(sortableColumnDescTemplateId)?.innerHTML;

        const sortableColumns = this.querySelectorAll("[data-sortable-column-key]");

        for (let c of sortableColumns) {
            c.onclick = () => this.#handleSortableColumnClick(c, sortableColumns,
                sortableColumnAscTemplate, sortableColumnDescTemplate
            );
        }
    }

    #handleSortableColumnClick(column, columns, ascTemplate, descTemplate) {
        const columnKey = column.getAttribute("data-sortable-column-key");
        const indicator = column.querySelector("[data-sortable-column-indicator]");
        let ascDirection = true;
        if (indicator && indicator.innerHTML) {
            if (indicator.innerHTML == ascTemplate) {
                indicator.innerHTML = descTemplate;
                ascDirection = false;
            } else {
                indicator.innerHTML = ascTemplate;
            }
        } else if (indicator) {
            indicator.innerHTML = ascTemplate;
        }

        for (let c of columns) {
            if (c == column) {
                continue;
            }
            const indicator = c.querySelector("[data-sortable-column-indicator]");
            if (indicator) {
                indicator.innerHTML = null;
            }
        }

        if (columnKey && indicator) {
            this.dispatchEvent(new CustomEvent("sortables-container-sorting-changed",
                { detail: { key: columnKey, asc: ascDirection } }));
        }
    }
}

if (customElements.get("sortables-container") == undefined) {
    customElements.define("sortables-container", SortablesContainer);
}