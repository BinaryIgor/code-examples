<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
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

onMounted(() => {
    document.addEventListener('mh:live-updates-toggled', liveUpdatesToggledEventHandler);
    document.addEventListener('mh:denomination-changed', denominationChangedEventHandler);
});

onUnmounted(() => {
    document.removeEventListener('mh:live-updates-toggled', liveUpdatesToggledEventHandler);
    document.removeEventListener('mh:denomination-changed', denominationChangedEventHandler);
});

</script>

<template>
    <markets-header :denomination="USD.id" t-namespace="markets-header." :t="t"
        :denominationExchangeRates="denominationExchangeRates.map(d => ({ name: d.to.id, exchangeRate: d.value }))">
    </markets-header>
    <assets-and-currencies t-namespace="assets-and-currencies." :t="t"
        :assets="assets.map(a => ({ id: a.id, name: a.name, marketSize: a.marketSize, denomination: a.denomination.id }))"
        :assetsValueChangeReason="assetsValueChangeReason"
        :currencies="currencies.map(c => ({ id: c.code.id, name: c.code.name, marketSize: c.marketSize, denomination: c.denomination.id }))"
        :denomination="denomination">
    </assets-and-currencies>
    <markets-projections t-namespace="markets-projections." :t="t" :assets="assetInputOptions"
        :currencies="currencyInputOptions">
    </markets-projections>
</template>
