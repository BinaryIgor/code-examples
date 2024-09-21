class HTMXShadowDOM extends HTMLElement {

    connectedCallback() {
        const shadowRoot = this.attachShadow({ mode: "open" });
        shadowRoot.innerHTML = `
        <style>
          h1 {
            margin: 0.5rem;
          }
          div {
            margin: 0.5rem;
            font-style: italic;
          }
          button {
            margin: 0.5rem;
            padding: 0.5rem;
            font-weight: bold;
          }
        </style>
        <h1>HTMX Shadow DOM</h1>
        <div id="htmx-trigger-output">Should work!</div>
        <button hx-post="/htmx-shadow-dom-button-trigger" hx-target="#htmx-trigger-output">Change it using HTMX</button>
        `;
        htmx.process(shadowRoot);
    }
}

customElements.define('htmx-shadow-dom', HTMXShadowDOM);