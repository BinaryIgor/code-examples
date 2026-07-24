import type { CurrencyCode, AssetCode } from "./codes";
import { HttpApi } from "./http-api";

export interface Asset {
  code: AssetCode;
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

export interface AssetsResponse {
  assets: Asset[];
  assetsVersion: string;
  exchangeRatesVersion: string;
  responseVersion: string;
}

export interface CurrenciesResponse {
  currencies: Currency[];
  responseVersion: string;
}

export interface ExchangeRatesResponse {
  exchangeRates: ExchangeRate[];
  responseVersion: string;
}

export class Response<T> {

  private readonly _value: T | null | undefined;
  private readonly _error: string | null;

  constructor(value: T | null | undefined, error: string | null) {
    this._value = value;
    this._error = error;
  }

  static ofSuccess<T>(value?: T): Response<T> {
    return new Response(value, null);
  }

  static ofFailure<T>(error: string): Response<T> {
    return new Response(null as T, error);
  }

  success(): boolean {
    return this._error == null;
  }

  value(): T {
    if (!this.success()) {
      throw new Error("Cannot return value from failed response");
    }
    if (this.hasValue()) {
      return this._value as T;
    }
    throw new Error("Cannot return value from empty success response");
  }

  hasValue() {
    return this.success() && this._value;
  }

  error(): string {
    if (!this.success()) {
      return this._error as string;
    }
    throw new Error("Cannot return error from success response");
  }
}

export type PromiseResponse<T> = Promise<Response<T>>;

export interface Api {

  assets(denomination: CurrencyCode, version?: string): PromiseResponse<AssetsResponse>

  currencies(denomination: CurrencyCode, version?: string): PromiseResponse<CurrenciesResponse>

  exchangeRates(from: CurrencyCode, version?: string): PromiseResponse<ExchangeRatesResponse>
}

export const api: Api = new HttpApi(import.meta.env.VITE_API_BASE_URL);