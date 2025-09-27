import { api, type Api } from "./api";
import type { MockedApi } from "./mocked-api";

export class Updater {

    private exchangeRatesChangedListener = () => { };
    private assetsValueChangedListener = () => { };
    private currenciesValueChangedListener = () => { };
    private paused = false;

    constructor(private readonly api: Api) {
    }

    public start() {
        setInterval(() => this.update(), 1000);
    }

    private async update() {
        if (this.paused) {
            return;
        }
        if (Math.random() > 0.75) {
            (this.api as MockedApi).setNextExchangeRatsChange(true);
            this.exchangeRatesChangedListener();
        }
        if (Math.random() > 0.5) {
            (this.api as MockedApi).setNextAssetsValueChange(true);
            this.assetsValueChangedListener();
        }
        if (Math.random() > 0.5) {
            (this.api as MockedApi).setNextCurrenciesValueChange(true);
            this.currenciesValueChangedListener();
        }
    }

    public setExchangeRatesChangedListener(listener: () => void) {
        this.exchangeRatesChangedListener = listener;
    }

    public setAssetsValueChangedListener(listener: () => void) {
        this.assetsValueChangedListener = listener;
    }

    public setCurrenciesValueChangedListener(listener: () => void) {
        this.currenciesValueChangedListener = listener;
    }

    public setPaused(paused: boolean) {
        this.paused = paused;
    }
}

let _updater: Updater | undefined;
export function useUpdater(): Updater {
    if (!_updater) {
        _updater = new Updater(api);
        _updater.start();
    }
    return _updater;
}