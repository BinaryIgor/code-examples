class TabsContainer extends HTMLElement {

    _activeTab = 0;
    set activeTab(value) {
        this._activeTab = value;
        this._updateActiveTab();
    }

    activeTabClass = "underline";

    connectedCallback() {
        this._tabsHeader = this.querySelector("[data-tabs-header]");
        this._tabsBody = this.querySelector("[data-tabs-body]");
        this.activeTabClass = this.getAttribute("active-tab-class") ?? this.activeTabClass;
        if (!this._tabsHeader) {
            throw new Error("Tabs header must be defined and marked with data-tabs-header attribute!");
        }
        if (!this._tabsBody) {
            throw new Error("Tabs body must be defined and marked with data-tabs-body attribute!");
        }

        [...this._tabsHeader.children].forEach((tab, i) => {
            tab.addEventListener('click', () => this.activeTab = i);
        });

        [...this._tabsBody.children].forEach((tab) => {
            tab.classList.add("hidden");
        });

        this._updateActiveTab();
    }

    _updateActiveTab() {
        [...this._tabsHeader.children].forEach((tab, i) => {
            if (i == this._activeTab) {
                tab.classList.add(this.activeTabClass);
            } else {
                tab.classList.remove(this.activeTabClass);
            }
        });
        [...this._tabsBody.children].forEach((tab, i) => {
            if (i == this._activeTab) {
                tab.classList.remove('hidden');
            } else {
                tab.classList.add('hidden');
            }
        });
    }
}

class TabHeader extends HTMLElement {

    connectedCallback() {
        this.classList.add("text-2xl");
        this.classList.add("p-2");
        this.classList.add("cursor-pointer");
        this.classList.add("grow");
    }
}

export function register() {
    customElements.define('tabs-container', TabsContainer);
    customElements.define('tab-header', TabHeader);
}