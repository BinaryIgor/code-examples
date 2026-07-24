import { Response, type PromiseResponse, type Api, type AssetsResponse, type CurrenciesResponse, type ExchangeRatesResponse } from "./api";
import type { CurrencyCode } from "./codes";

export class HttpApi implements Api {

  private readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  assets(denomination: CurrencyCode, version?: string): PromiseResponse<AssetsResponse> {
    return this.get(`assets?denomination=${denomination}`, version);
  }

  async get<T>(path: string, version?: string): PromiseResponse<T> {
    try {
      const response = await fetch(`${this.baseUrl}/${path}`, { method: "GET", headers: version ? { 'If-None-Match': version } : {} });
      if (response.status == 304) {
        return Response.ofSuccess();
      }
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

  currencies(denomination: CurrencyCode, version?: string): PromiseResponse<CurrenciesResponse> {
    return this.get(`currencies?denomination=${denomination}`, version);
  }

  exchangeRates(from: CurrencyCode, version?: string): PromiseResponse<ExchangeRatesResponse> {
    return this.get(`exchange-rates/${from}`, version);
  }
}