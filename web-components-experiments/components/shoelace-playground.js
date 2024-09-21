console.log("Some Shoelace js...");

const slButton = document.querySelector("sl-button");
console.log(slButton.shadowRoot);

const qrCode = document.querySelector("sl-qr-code"); 
const qrCodeInput = document.querySelector("sl-input");

customElements.whenDefined("sl-qr-code").then(() => {
    qrCodeInput.value = qrCode.value;
    qrCodeInput.addEventListener("sl-input", () => qrCode.value = qrCodeInput.value); 
});