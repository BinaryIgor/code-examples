// TODO: maybe something smarter?
export class Updater {

  exchangeRatesChangedListener = () => { };
  assetsValueChangedListener = () => { };
  currenciesValueChangedListener = () => { };
  private _paused = false;

  set paused(value: boolean) {
    this._paused = value;
  }

  public start() {
    setInterval(() => this.update(), 1000);
  }

  private async update() {
    if (this._paused) {
      return;
    }
    if (Math.random() > 0.5) {
      this.exchangeRatesChangedListener();
    }
    if (Math.random() > 0.5) {
      this.assetsValueChangedListener();
    }
    if (Math.random() > 0.5) {
      this.currenciesValueChangedListener();
    }
  }

  public setPaused(paused: boolean) {
    this._paused = paused;
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