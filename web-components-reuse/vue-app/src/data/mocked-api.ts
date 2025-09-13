import type { Api, Asset, Currency, ExchangeRate } from "./api";
import { type CurrencyCode, USD, EUR, JPY, GBP, CNY, PLN } from "./currency-code";

// TODO: common API not to duplicate it
export class MockedApi implements Api {

    // TODO: percentage of global market assets field
    private assets = [
        {
            id: "bonds",
            name: "Bonds",
            marketSize: 145.1e12,
            denomination: USD

        },
        {
            id: "stocks",
            name: "Stocks",
            marketSize: 126.7e12,
            denomination: USD

        },
        {
            id: "gold",
            name: "Gold",
            marketSize: 22.6e12,
            denomination: USD
        },
        {
            id: "cash",
            name: "Cash Reserves",
            marketSize: 12.6e12,
            denomination: USD
        },
        {
            id: "rlest",
            name: "Real Estate",
            marketSize: 12.5e12,
            denomination: USD
        },
        {
            id: "btc",
            name: "Bitcoin",
            marketSize: 2.3e12,
            denomination: USD
        }
    ];

    private currencies = [
        {
            code: USD,
            marketSize: 6.639e12,
            denomination: USD
        },
        {
            code: EUR,
            marketSize: 2.292e12,
            denomination: USD
        },
        {
            code: JPY,
            marketSize: 1.253e12,
            denomination: USD
        },
        {
            code: GBP,
            marketSize: 968e9,
            denomination: USD
        },
        {
            code: CNY,
            marketSize: 526.2e9,
            denomination: USD
        },
        {
            code: PLN,
            marketSize: 13e9,
            denomination: USD
        }
    ];

    private baseUsdExchangeRates = [
        {
            currencyCode: USD,
            value: 1
        },
        {
            currencyCode: EUR,
            value: 0.85
        },
        {
            currencyCode: JPY,
            value: 148
        },
        {
            currencyCode: GBP,
            value: 0.73
        },
        {
            currencyCode: CNY,
            value: 7.11
        },
        {
            currencyCode: PLN,
            value: 3.63
        },
    ];
    private lastUsdExchangeRates = this.baseUsdExchangeRates;

    private nextExchangeRatesChange = false;
    private nextAssetsValueChange = false;
    private nextCurrenciesValueChange = false;

    setNextExchangeRatsChange(change: boolean) {
        this.nextExchangeRatesChange = change;
    }

    setNextAssetsValueChange(change: boolean) {
        this.nextAssetsValueChange = change;
    }

    setNextCurrenciesValueChange(change: boolean) {
        this.nextCurrenciesValueChange = change;
    }

    // TODO: order might change as well!
    topAssets(denomination: CurrencyCode): Promise<Asset[]> {
        const exchangeRateValue = this.exchangeRateFor(denomination);
        const denominatedAssets = this.assets.map(a => {
            const nextValueMultiplier = this.nextAssetsValueChange && Math.random() > 0.5 ? 0.9 + (Math.random() * 0.2) : 1;
            return {
                ...a, marketSize: Math.round(a.marketSize * nextValueMultiplier * exchangeRateValue),
                denomination
            }
        });

        this.nextAssetsValueChange = false;

        return Promise.resolve(denominatedAssets);
    }

    private exchangeRateFor(denomination: CurrencyCode): number {
        const exchangeRate = this.lastUsdExchangeRates.find(er => er.currencyCode.id === denomination.id);
        if (!exchangeRate) {
            throw new Error(`There is no exchange rate for ${denomination} denomination!`);
        }
        return (exchangeRate as any).value;
    }

    topCurrencies(denomination: CurrencyCode): Promise<Currency[]> {
        const exchangeRateValue = this.exchangeRateFor(denomination);

        const denominatedCurrencies = this.currencies.map(c => {
            const nextValueMultiplier = this.nextCurrenciesValueChange && Math.random() > 0.5 ? 0.9 + (Math.random() * 0.2) : 1;
            return {
                ...c, marketSize: Math.round(c.marketSize * nextValueMultiplier * exchangeRateValue),
                denomination
            }
        });

        this.nextCurrenciesValueChange = false;

        return Promise.resolve(denominatedCurrencies);
    }

    exchangeRates(): Promise<ExchangeRate[]> {
        const nextUsdExchangeRates = this.lastUsdExchangeRates.map(er => {
            let nextValueMultiplier;
            if (er.currencyCode == USD) {
                nextValueMultiplier = 1;
            } else {
                nextValueMultiplier = this.nextExchangeRatesChange ? 0.95 + (Math.random() * 0.1) : 1;
            }
            return { ...er, value: er.value * nextValueMultiplier };
        });
        this.lastUsdExchangeRates = nextUsdExchangeRates;

        this.nextExchangeRatesChange = false;

        return Promise.resolve(nextUsdExchangeRates.map(er => ({ from: USD, to: er.currencyCode, value: er.value })));
    }

    async exchangeRate(to: CurrencyCode): Promise<ExchangeRate> {
        if (this.nextExchangeRatesChange) {
            await this.exchangeRates();
        }
        const exchangeRate = this.lastUsdExchangeRates.find(er => er.currencyCode.id == to.id);
        if (!exchangeRate) {
            throw new Error(`Couldn't find from USD to ${to} exchange rate`);
        }
        return Promise.resolve({
            from: USD,
            value: exchangeRate.value,
            to: exchangeRate.currencyCode
        });
    }
}