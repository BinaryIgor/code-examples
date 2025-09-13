<script setup lang="ts">
import { ref } from 'vue';
import { type Asset, type Currency, type ExchangeRate, api } from '../data/api';
import { USD, type CurrencyCode } from '../data/currency-code';
import { useUpdater } from '../data/updater';
import MarketsComparator from './MarketsComparator.vue';
import ProjectionsCalculator from './ProjectionsCalculator.vue';

const liveUpdateEnabled = ref<boolean>(true);
const denomination = ref<CurrencyCode>(USD);
const denominationExchangeRate = ref<number>(1);
const denominationExchangeRates = ref<ExchangeRate[]>();
const assets = ref<Asset[]>([]);
const currencies = ref<Currency[]>([]);
const assetsValueChangeReasons = ref<string>();

const fromAssetOrCurrency = ref<{ name: string, marketSize: number }>();
const toAssetOrCurrency = ref<{ name: string, marketSize: number }>();

const fetchAssets = () => api.topAssets(denomination.value).then(a => assets.value = a);
const fetchCurrencies = () => api.topCurrencies(denomination.value).then(c => currencies.value = c);

useUpdater().setExchangeRatesChangedListener(() => {
    api.exchangeRate(denomination.value)
        .then(er => {
            updateDenominationExchangeRates();
            if (er.value != denominationExchangeRate.value) {
                assetsValueChangeReasons.value = "CURRENCY_EXCHANGE_RATE_CHANGE";
                fetchAssets();
            }
        });
});
useUpdater().setAssetsValueChangedListener(() => {
    assetsValueChangeReasons.value = "ASSETS_VALUE_CHANGE";
    fetchAssets();
});
useUpdater().setCurrenciesValueChangedListener(() => {
    fetchCurrencies();
});


const toggleLiveUpdateClickHandler = () => {
    liveUpdateEnabled.value = !liveUpdateEnabled.value;
    useUpdater().setPaused(!liveUpdateEnabled.value);
};


const denominationClickHandler = (d: CurrencyCode) => {
    api.exchangeRate(d)
        .then(async er => {
            denomination.value = d;
            denominationExchangeRate.value = er.value;

            updateDenominationExchangeRates();

            fetchAssets();
            fetchCurrencies();
        });
};

const updateDenominationExchangeRates = async () => {
    const exchangeRates = await api.exchangeRates();
    const fromDollarToDenominationExchangeRate = exchangeRates.find(er => er.to.id == denomination.value.id)?.value ?? 1;
    const fromDenominationToDollarExchangeRate = 1 / fromDollarToDenominationExchangeRate;
    denominationExchangeRates.value = exchangeRates.map(er => ({
        from: denomination.value,
        to: er.to,
        value: Math.round(fromDenominationToDollarExchangeRate * er.value * 100) / 100.0
    }));
};

const onChosenFromMarketSizeChangeHandler = (assetOrCurrency: string, marketSize: number) => {
    fromAssetOrCurrency.value = { name: assetOrCurrency, marketSize };
};

const onChosenToMarketSizeChangeHandler = (assetOrCurrency: string, marketSize: number) => {
    toAssetOrCurrency.value = { name: assetOrCurrency, marketSize };
};

denominationClickHandler(denomination.value);

</script>

<template>
    <div class="m-2">
        <div class="absolute right-2 text-xl">Live Update: <span class="cursor-pointer"
                @click="() => toggleLiveUpdateClickHandler()">{{ liveUpdateEnabled ? "ON" : "OFF" }}</span>
        </div>
        <span class="text-3xl">Markets in </span>
        <drop-down-container>
            <span class="underline cursor-pointer text-3xl pr-24">{{ denomination.id }}</span>
            <ul class="border-1 rounded cursor-pointer bg-white text-lg" data-drop-down-options>
                <li class="py-2 px-4 border-b-1 last:border-0" v-for="d in denominationExchangeRates"
                    @click="() => denominationClickHandler(d.to)">
                    {{ d.to.id }}: {{ d.value }}
                </li>
            </ul>
        </drop-down-container>
    </div>
    <div class="m-2">
        <tabs-container active-tab-class="underline">
            <div class="flex" data-tabs-header>
                <tab-header>Assets</tab-header>
                <tab-header>Currencies</tab-header>
            </div>
            <div data-tabs-body>
                <div class="h-[40dvh] overflow-y-auto">
                    <div>
                        <asset-element class="my-2" v-for="a in assets" :id="a.id" :name="a.name"
                            :market-size="a.marketSize" :denomination="a.denomination.id"
                            :value-change-reasons="assetsValueChangeReasons" />
                    </div>
                </div>
                <div class="h-[40dvh] overflow-y-auto">
                    <div>
                        <currency-element class="my-2" v-for="c in currencies" :id="c.code.id" :name="c.code.name"
                            :market-size="c.marketSize" :denomination="c.denomination.id" />
                    </div>
                </div>
            </div>
        </tabs-container>
    </div>
    <div class="m-2">
        <!--TODO: could be a web component as well!-->
        <h2 class="mt-16 text-2xl my-4">Calculator</h2>
        <MarketsComparator :assets="assets" :currencies="currencies"
            :onChosenFromMarketSizeChange="onChosenFromMarketSizeChangeHandler"
            :onChosenToMarketSizeChange="onChosenToMarketSizeChangeHandler" />
        <ProjectionsCalculator :assetOrCurrency1="fromAssetOrCurrency ? fromAssetOrCurrency : undefined"
            :assetOrCurrency2="toAssetOrCurrency ? toAssetOrCurrency : undefined" />
    </div>
</template>

<style scoped></style>
