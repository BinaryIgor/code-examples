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