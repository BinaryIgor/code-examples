export interface CurrencyCode {
    id: string;
    name: string;
}

export const USD = {
    id: "USD",
    name: "US Dollar"
};

export const EUR = {
    id: "EUR",
    name: "Euro"
};

export const JPY = {
    id: "JPY",
    name: "Japanese Yen"
};

export const GBP = {
    id: "GBP",
    name: "British Pound"
};

export const CNY = {
    id: "CNY",
    name: "Chinese Yuan"
};

export const PLN = {
    id: "PLN",
    name: "Polish Zloty"
}

export const currencyCodes: CurrencyCode[] = [USD, EUR, JPY, GBP, CNY, PLN];

export function currencyCodeById(id: string): CurrencyCode {
    const code = currencyCodes.find(cc => cc.id == id);
    if (code) {
        return code;
    }
    throw new Error(`Currency of ${id} name doesn't exist`);
}

export function currencyCodeByName(name: string): CurrencyCode {
    const code = currencyCodes.find(cc => cc.name == name);
    if (code) {
        return code;
    }
    throw new Error(`Currency of ${name} name doesn't exist`);
}