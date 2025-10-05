export class Updater {

    private exchangeRatesChangedListener = () => { };
    private assetsValueChangedListener = () => { };
    private currenciesValueChangedListener = () => { };
    private paused = false;

    public start() {
        setInterval(() => this.update(), 1000);
    }

    private async update() {
        if (this.paused) {
            return;
        }
        this.exchangeRatesChangedListener();
        this.assetsValueChangedListener();
        this.currenciesValueChangedListener();
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
        _updater = new Updater();
        _updater.start();
    }
    return _updater;
}