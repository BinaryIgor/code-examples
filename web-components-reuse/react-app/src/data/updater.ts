export class Updater {

  fetchAssets: Function | null = null;
  fetchCurrencies: Function | null = null;
  fetchExchangeRates: Function | null = null;
  private _paused = false;

  set paused(value: boolean) {
    this._paused = value;
  }

  with(fetchAssets: Function, fetchCurrencies: Function, fetchExchangeRates: Function) {
    this.fetchAssets = fetchAssets;
    this.fetchCurrencies = fetchCurrencies;
    this.fetchExchangeRates = fetchExchangeRates;
  }

  public start() {
    setInterval(() => this.update(), 1000);
  }

  public clear() {
    this.fetchAssets = null;
    this.fetchCurrencies = null;
    this.fetchExchangeRates = null;
  }

  private async update() {
    if (this._paused) {
      return;
    }
    if (this.fetchAssets) {
      this.fetchAssets();
    }
    if (this.fetchCurrencies) {
      this.fetchCurrencies();
    }
    if (this.fetchExchangeRates) {
      this.fetchExchangeRates();
    }
  }
}

let updater: Updater | undefined;
export function useUpdater(): Updater {
  if (!updater) {
    updater = new Updater();
    updater.start();
  }
  return updater;
}