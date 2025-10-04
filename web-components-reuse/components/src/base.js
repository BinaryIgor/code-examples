// Common types definition
/**
* @typedef {Object} AssetOrCurrency
* @property {string} name
* @property {number} marketSize
*/

export function formatMoney(value, denomination) {
    const zeros = value.length;
    if (zeros > 15) {
        return `${value.substring(0, zeros - 15)} ${value.substring(zeros - 15, zeros - 12)} ${value.substring(zeros - 12, zeros - 9)} ${value.substring(zeros - 9, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    if (zeros > 12) {
        return `${value.substring(0, zeros - 12)} ${value.substring(zeros - 12, zeros - 9)} ${value.substring(zeros - 9, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    if (zeros > 9) {
        return `${value.substring(0, zeros - 9)} ${value.substring(zeros - 9, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    if (zeros > 6) {
        return `${value.substring(0, zeros - 6)} ${value.substring(zeros - 6, zeros - 3)} ${value.substring(zeros - 3)} ${denomination}`;
    }
    return `${value} ${denomination}`;
}

export class BaseHTMLElement extends HTMLElement {

    _t = null;
    _tNamespace = '';

    set t(value) {
        this._t = value;
        // TODO: shouldn't all components be re-renderable and called from here?
    }

    set tNamespace(value) {
        this._tNamespace = value;
    }

    translation(key) {
        const namespacedKey = (this.getAttribute("t-namespace") ?? this._tNamespace) + key;
        const attributeTranslation = this.getAttribute(`t-${namespacedKey}`);
        if (attributeTranslation != undefined) {
            return attributeTranslation;
        }
        return this._t ? this._t(namespacedKey) : null;
    }

    translationAttribute(key) {
        const translation = this.translation(key);
        if (translation) {
            return `t-${key}="${translation}"`;
        }
        return "";
    }

    translationAttributeRemovingNamespace(key, namespace) {
        const translation = this.translation(namespace + key);
        if (translation) {
            return `t-${key}="${translation}"`;
        }
        return "";
    }
}