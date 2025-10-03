<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, useTemplateRef } from 'vue';
import { useI18n } from 'vue-i18n';
import { type Asset, type Currency, type ExchangeRate, api } from '../data/api';
import { USD, type CurrencyCode, currencyCodeById } from '../data/currency-code';
import { useUpdater } from '../data/updater';

const { t } = useI18n();

const liveUpdatesEnabled = ref<boolean>(true);
const denomination = ref<CurrencyCode>(USD);
const denominationExchangeRate = ref<number>(1);
const denominationExchangeRates = ref<ExchangeRate[]>([]);
const assets = ref<Asset[]>([]);
const currencies = ref<Currency[]>([]);
const assetsValueChangeReason = ref<string>();

const fromAssetOrCurrency = ref<{ name: string, marketSize: number }>();
const toAssetOrCurrency = ref<{ name: string, marketSize: number }>();

const marketsComparator = useTemplateRef<HTMLElement>("markets-comparator");

const fetchAssets = () => api.topAssets(denomination.value).then(a => assets.value = a);
const fetchCurrencies = () => api.topCurrencies(denomination.value).then(c => currencies.value = c);

useUpdater().setExchangeRatesChangedListener(() => {
    api.exchangeRate(denomination.value)
        .then(er => {
            updateDenominationExchangeRates();
            if (er.value != denominationExchangeRate.value) {
                assetsValueChangeReason.value = "CURRENCY_EXCHANGE_RATE_CHANGED";
                fetchAssets();
            }
        });
});
useUpdater().setAssetsValueChangedListener(() => {
    assetsValueChangeReason.value = "ASSET_VALUE_CHANGED";
    fetchAssets();
});
useUpdater().setCurrenciesValueChangedListener(() => {
    fetchCurrencies();
});

const onDenominationChanged = () => {
    api.exchangeRate(denomination.value)
        .then(async er => {
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

const assetInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
    assets.value.map(a => ({ name: a.name, marketSize: a.marketSize })));

const currencyInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
    currencies.value.map(c => ({ name: c.code.name, marketSize: c.marketSize })));

onDenominationChanged();

const liveUpdatesToggledEventHandler = (e: Event) => {
    liveUpdatesEnabled.value = (e as CustomEvent).detail as boolean;
    useUpdater().setPaused(!liveUpdatesEnabled.value);
};

const denominationChangedEventHandler = (e: Event) => {
    const denominationId = (e as CustomEvent).detail as string;
    denomination.value = currencyCodeById(denominationId);
    onDenominationChanged();
};

const fromChosenMarketSizeChangedEventHandler = (e: Event) => {
    const { name, marketSize } = (e as CustomEvent).detail as { name: string, marketSize: number };
    onChosenFromMarketSizeChangeHandler(name, marketSize);
};
const toChosenMarketSizeChangedEventHandler = (e: Event) => {
    const { name, marketSize } = (e as CustomEvent).detail as { name: string, marketSize: number };
    onChosenToMarketSizeChangeHandler(name, marketSize);
};

onMounted(() => {
    document.addEventListener('mh:live-updates-toggled', liveUpdatesToggledEventHandler);
    document.addEventListener('mh:denomination-changed', denominationChangedEventHandler);
    document.addEventListener('mc:from-market-size-changed', fromChosenMarketSizeChangedEventHandler);
    document.addEventListener('mc:to-market-size-changed', toChosenMarketSizeChangedEventHandler);
});

onUnmounted(() => {
    document.removeEventListener('mh:live-updates-toggled', liveUpdatesToggledEventHandler);
    document.removeEventListener('mh:denomination-changed', denominationChangedEventHandler);
    document.removeEventListener('mc:from-market-size-changed', fromChosenMarketSizeChangedEventHandler);
    document.removeEventListener('mc:to-market-size-changed', toChosenMarketSizeChangedEventHandler);
});

</script>

<template>
    <div class="m-2">
        <markets-header :denomination="USD.id"
            :denominationExchangeRates="denominationExchangeRates.map(d => ({ name: d.to.id, exchangeRate: d.value }))">
        </markets-header>
    </div>
    <div class="m-2">
        <assets-and-currencies
            :assets="assets.map(a => ({ id: a.id, name: a.name, marketSize: a.marketSize, denomination: a.denomination.id }))"
            :assetsValueChangeReason="assetsValueChangeReason"
            :currencies="currencies.map(c => ({ id: c.code.id, name: c.code.name, marketSize: c.marketSize, denomination: c.denomination.id }))"
            :denomination="denomination">
        </assets-and-currencies>
    </div>
    <div class="m-2">
        <h2 class="mt-16 text-2xl my-4">{{ t('calculatorHeader') }}</h2>
        <markets-comparator :assets="assetInputOptions" :currencies="currencyInputOptions">
        </markets-comparator>
        <projections-calculator :assetOrCurrency1="fromAssetOrCurrency" :assetOrCurrency2="toAssetOrCurrency">
        </projections-calculator>
    </div>
</template>
