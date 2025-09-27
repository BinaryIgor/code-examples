class DropDownContainer extends HTMLElement {

    connectedCallback() {
        const anchor = this.querySelector('[data-drop-down-anchor]') ?? this;
        anchor.style = "position: relative; display: inline-block";

        const options = this.querySelector("[data-drop-down-options]");
        if (!options) {
            throw new Error("Options must be defined and marked with data-drop-down-options attribute!");
        }
        options.style = "position: absolute; z-index: 99";
        options.classList.add("hidden");

        anchor.onclick = (e) => {
            // Do not hide other, opened DropDowns
            e.stopPropagation();
            options.classList.toggle("hidden");
        };

        window.addEventListener("click", e => {
            if (e.target != anchor && e.target.parentNode != anchor) {
                console.log("Global DropDown close!");
                options.classList.add("hidden");
            }
        });
    }
}

export function register() {
    customElements.define("drop-down-container", DropDownContainer);
}