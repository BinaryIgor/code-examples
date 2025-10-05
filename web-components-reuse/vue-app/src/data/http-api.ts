import { Response, type Api, type Asset, type Currency, type ExchangeRate } from "./api";
import type { CurrencyCode } from "./codes";

export class HttpApi implements Api {

    constructor(private readonly baseUrl: string) {

    }

    async assets(denomination: CurrencyCode): Promise<Response<Asset[]>> {
        return this.get(`assets?denomination=${denomination}`);
    }

    async get<T>(path: string): Promise<Response<T>> {
        try {
            const response = await fetch(`${this.baseUrl}/${path}`);
            const jsonResponse = await response.json();
            if (response.ok) {
                return Response.ofSuccess(jsonResponse as T);
            }
            return Response.ofFailure(jsonResponse['error'] ?? 'UnsupportedErrorFormat');
        } catch (e: any) {
            console.error("Failed to fetch", e);
            return Response.ofFailure("UnknownFetchError");
        }
    }

    currencies(denomination: CurrencyCode): Promise<Response<Currency[]>> {
        return this.get(`currencies?denomination=${denomination}`);
    }

    exchangeRates(from: CurrencyCode): Promise<Response<ExchangeRate[]>> {
        return this.get(`exchange-rates/${from}`);
    }
}