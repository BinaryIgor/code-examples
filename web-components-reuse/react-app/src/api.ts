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

export class Response<T> {

  private readonly _value: T | null;
  private _error: string | null

  constructor(value: T | null, error: string | null) {
    this._value = value;
    this._error = error;
  }

  static ofSuccess<T>(value: T): Response<T> {
    return new Response(value, null);
  }

  static ofFailure<T>(error: string): Response<T> {
    return new Response(null as T, error);
  }

  success(): boolean {
    return this._value != null;
  }

  value(): T {
    if (this.success()) {
      return this._value as T;
    }
    throw new Error("Cannot return value from failed response");
  }

  error(): string {
    if (!this.success()) {
      return this._error as string;
    }
    throw new Error("Cannot return error from sucess response");
  }
}

export interface Api {

  assets(denomination: CurrencyCode): Promise<Response<Asset[]>>

  currencies(denomination: CurrencyCode): Promise<Response<Currency[]>>

  exchangeRates(from: CurrencyCode): Promise<Response<ExchangeRate[]>>
}

export const api: Api = new HttpApi(import.meta.env.VITE_API_BASE_URL);