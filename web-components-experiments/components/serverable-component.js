/*
<inject-template>
    <h1 class="text-xl m-8">{title}</h1>
    <div class="mx-8">{description}</div>
</inject-template>
*/

class ServerableComponent extends HTMLElement {
    connectedCallback() {
        console.log(this.innerHTML);
        setTimeout(() => shadowRoot.querySelector("div").innerHTML = "Overriden description", 1000);
    }

     // connectedCallback() {
    //     const template = document.getElementById("serverable-component-template").content;
    //     const shadowRoot = this.attachShadow({ mode: "open" });
    //     shadowRoot.appendChild(template.cloneNode(true));

    //     setTimeout(() => shadowRoot.querySelector("div").innerHTML = "Overriden description", 1000);
    // }
}

customElements.define('serverable-component', ServerableComponent);