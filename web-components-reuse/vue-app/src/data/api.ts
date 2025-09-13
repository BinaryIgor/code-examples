import type { CurrencyCode } from "./currency-code";
import { MockedApi } from "./mocked-api";

export interface Asset {
    id: string;
    name: string;
    marketSize: number;
    denomination: CurrencyCode
}

export interface Currency {
    code: CurrencyCode;
    marketSize: number;
    denomination: CurrencyCode;
}

export interface ExchangeRate {
    from: CurrencyCode;
    to: CurrencyCode;
    value: number;
}

// TODO: error type
export interface Api {

    topAssets(denomination: CurrencyCode): Promise<Asset[]>

    topCurrencies(denomination: CurrencyCode): Promise<Currency[]>

    exchangeRates(): Promise<ExchangeRate[]>

    exchangeRate(to: CurrencyCode): Promise<ExchangeRate>
}

export const api: Api = new MockedApi();