
const navigationId = "app-navigation";
const navigationDropdownId = "app-navigation-dropdown";
const FORM_LABEL = "data-form";
const CONFIRMABLE_ELEMENT_TITLE_LABEL = "data-confirmable-element-title";
const CONFIRMABLE_ELEMENT_CONTENT_LABEL = "data-confirmable-element-content";
const SUBMIT_FORM_LABEL = "data-submit-form";
const HIDDEN_CLASS = "hidden";
const DISABLED_CLASS = "disabled";
const HTMX_PUSH_URL_ATTRIBUTE = "hx-push-url";

const HTMX_EVENTS = {
    confirm: "htmx:confirm",
    afterSwap: "htmx:afterSwap",
    historyRestored: "htmx:historyCacheMissLoad",
    load: "htmx:load",
    responseError: "htmx:responseError",
    sendError: "htmx:sendError",
    pushedIntoHistory: "htmx:pushedIntoHistory"
};

const TRIGGERS = {
    changeRoute: "change-route",
    resetScroll: "reset-scroll",
    formValidated: "form-validated",
    resetForm: "reset-form"
};

const scrollPositions = {
    restorablePaths: ["/authors"],
    positions: new Map()
};
let scrollReset = false;

initConfirmableModal();
initErrorModal();
initNavigation();
initEventListeners();

function initConfirmableModal() {
    const confirmableModal = document.getElementById("confirmable-modal");
    const confirmableModalTitle = document.getElementById("confirmable-modal-title");
    const confirmableModalContent = document.getElementById("confirmable-modal-content");

    let confirmableEvent = null;

    function isModalShown() {
        return !confirmableModal.classList.contains(HIDDEN_CLASS);
    }

    function hideModal() {
        confirmableModal.classList.add(HIDDEN_CLASS);
    }

    function showModal() {
        confirmableModal.classList.remove(HIDDEN_CLASS);
    }

    document.getElementById("confirmable-modal-cancel").onclick = e => {
        e.stopPropagation();
        hideModal();
    };
    document.getElementById("confirmable-modal-ok").onclick = e => {
        e.stopPropagation();
        hideModal();
        if (confirmableEvent) {
            confirmableEvent.detail.issueRequest();
            confirmableEvent = null;
        }
    };

    document.getElementById("confirmable-modal-close").onclick = () => confirmableModal.classList.toggle(HIDDEN_CLASS);

    document.addEventListener(HTMX_EVENTS.confirm, e => {
        const sourceElement = e.detail.elt;
        const confirmableTitleMessage = sourceElement.getAttribute(CONFIRMABLE_ELEMENT_TITLE_LABEL);
        const confirmableContentMessage = sourceElement.getAttribute(CONFIRMABLE_ELEMENT_CONTENT_LABEL);

        if (confirmableTitleMessage || confirmableContentMessage) {
            e.preventDefault();

            confirmableEvent = e;

            if (confirmableTitleMessage) {
                confirmableModalTitle.innerHTML = confirmableTitleMessage;
            }
            if (confirmableContentMessage) {
                confirmableModalContent.innerHTML = confirmableContentMessage;
            }

            showModal();
        }
    });

    confirmableModal.addEventListener("click", () => {
        if (isModalShown()) {
            hideModal();
        }
    });
}

function initErrorModal() {
    const errorModal = document.getElementById("error-modal");
    const errorModalContent = document.getElementById("error-modal-content");

    document.getElementById("error-modal-close").onclick = () => {
        errorModal.classList.toggle(HIDDEN_CLASS);
    };

    document.addEventListener(HTMX_EVENTS.responseError, e => {
        errorModalContent.innerHTML = e.detail.xhr.response;
        errorModal.classList.remove(HIDDEN_CLASS);
    });

    document.addEventListener(HTMX_EVENTS.sendError, e => {
        errorModalContent.innerHTML = "Server unavailable";
        errorModal.classList.remove(HIDDEN_CLASS);
    });
}

function initNavigation() {
    let navigationDropdown;
    let navigationDropdownOptions;

    function findElementsAndInitNavigation() {
        navigationDropdown = document.getElementById(navigationDropdownId);
        navigationDropdownOptions = navigationDropdown.querySelector("ul");
        navigationDropdown.onclick = e => {
            e.stopPropagation();
            navigationDropdownOptions.classList.toggle(HIDDEN_CLASS);
        }
    }

    findElementsAndInitNavigation();

    addEventListener(HTMX_EVENTS.afterSwap, e => {
        if (e.target.id == navigationId) {
            findElementsAndInitNavigation();
        }
    });

    document.addEventListener("click", () => {
        if (navigationDropdownOptions && !navigationDropdownOptions.classList.contains(HIDDEN_CLASS)) {
            navigationDropdownOptions.classList.add(HIDDEN_CLASS);
        }
    });
}

function initEventListeners() {
    // window.addEventListener("popstate", e => {
    //     console.log("Popping state!", e, "url:", location.pathname);
    // });
    window.addEventListener(HTMX_EVENTS.pushedIntoHistory, e => {
        scrollReset = false;
    });

    window.addEventListener(TRIGGERS.formValidated, e => {
        const label = e.detail.label;
        if (label) {
            const formValid = e.detail.valid;

            const submitButtons = document.querySelectorAll(`[${SUBMIT_FORM_LABEL}="${label}"]`);
            submitButtons.forEach(sb => {
                if (formValid) {
                    sb.disabled = false;
                    sb.classList.remove(DISABLED_CLASS);
                } else {
                    sb.disabled = true;
                    sb.classList.add(DISABLED_CLASS);
                }
            });
        }
    });

    window.addEventListener(TRIGGERS.resetForm, e => {
        const formToReset = document.querySelector(`[${FORM_LABEL}="${e.detail.value}"]`);
        if (formToReset) {
            formToReset.querySelectorAll("input").forEach(i => {
                if (i.type != "submit") {
                    i.value = "";
                }
            });
            formToReset.querySelectorAll("textarea").forEach(i => i.value = "");
        }
    });

    // window.addEventListener(HTMX_EVENTS.configRequest, e => {
    //     console.log("Request...", e.detail);
    //     // e.preventDefault();
    // });

    let lastPath = null;
    let restorablePath = false;
    document.addEventListener("scroll", () => {
        if (window.scrollY == 0 || scrollReset) {
            return;
        }
        const currentPath = location.pathname;

        if (currentPath == lastPath && restorablePath) {
            scrollPositions.positions.set(currentPath, window.scrollY);
            return;
        }

        lastPath = currentPath;
        restorablePath = false;

        for (let r of scrollPositions.restorablePaths) {
            if (currentPath.startsWith(r)) {
                scrollPositions.positions.set(currentPath, window.scrollY);
                restorablePath = true;
                break;
            }
        }
    });

    let historyRestored = false;
    window.addEventListener(HTMX_EVENTS.historyRestored, () => historyRestored = true);
    window.addEventListener(HTMX_EVENTS.load, () => {
        if (historyRestored) {
            const savedScroll = scrollPositions.positions.get(location.pathname);

            if (savedScroll && savedScroll > 0) {
                window.scrollTo({
                    top: savedScroll,
                    left: 0
                });
            }

            scrollPositions.positions.delete(location.pathname);

            historyRestored = false;
        }
    });

    addEventListener(TRIGGERS.resetScroll, () => {
        window.scrollTo({
            top: 0,
            left: 0
        });
        scrollReset = true;
    });
}

function pushRouteToHistoryIfNot(el, ...routes) {
    let pushUrl = true;

    for (let r of routes) {
        if (location.pathname == r) {
            pushUrl = false;
            break;
        }
    }

    el.setAttribute(HTMX_PUSH_URL_ATTRIBUTE, `${pushUrl}`);
    el.dispatchEvent(new Event(TRIGGERS.changeRoute));
}