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

class DropDown extends HTMLElement {

    connectedCallback() {
        const title = this.getAttribute("title");

        // 3 guys:
        // <DropDownContainer>
        //   <div>Title</div>
        //   <DropDownItems>
        //     <li></li>
        //     <li></li>
        //   </DropDownItems>
        //</DropDownContainer>
        //

        this.innerHTML = `
            <div style="position: relative">
                <div>${title}</div>
                <ul style="position: absolute; z-index: 99" class="hidden cursor-pointer">
                    <li>Option 1</li>
                    <li>Option 2</li>
                </ul>
            </div>
        `;

        const options = this.querySelector("ul");
        this._optionsElement = options;

        const container = this.querySelector("div");
        container.onclick = (e) => {
            // Do not hide other, opened DropDowns
            e.stopPropagation();
            options.classList.toggle("hidden");
        };
    }
}

export function register() {
    customElements.define("drop-down-container", DropDownContainer);
    customElements.define("drop-down", DropDown);
}