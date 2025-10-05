<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { type Asset, type Currency, type ExchangeRate, api } from '../data/api';
import { CurrencyCode } from '../data/codes';
import { useUpdater } from '../data/updater';
import * as Events from './events';

const { t } = useI18n();

const liveUpdatesEnabled = ref<boolean>(true);
const denomination = ref<CurrencyCode>(CurrencyCode.USD);
const denominationToUSDExchangeRate = ref<number>(1);
const denominationExchangeRates = ref<ExchangeRate[]>([]);
const assets = ref<Asset[]>([]);
const currencies = ref<Currency[]>([]);
const assetsValueChangeReason = ref<string>();

const fetchAssets = () => api.assets(denomination.value)
    .then(r => {
        if (r.success()) {
            assets.value = r.value();
        } else {
            Events.showErrorModal(r.error());
        }
    });
const fetchCurrencies = () => api.currencies(denomination.value)
    .then(r => {
        if (r.success()) {
            currencies.value = r.value();
        } else {
            Events.showErrorModal(r.error());
        }
    });

useUpdater().setExchangeRatesChangedListener(() => {
    const previousDenominationExchangeReate = denominationToUSDExchangeRate.value;
    updateDenominationExchangeRates();
    if (previousDenominationExchangeReate != denominationToUSDExchangeRate.value) {
        assetsValueChangeReason.value = "CURRENCY_EXCHANGE_RATE_CHANGED";
        fetchAssets();
    }
});
useUpdater().setAssetsValueChangedListener(() => {
    assetsValueChangeReason.value = "ASSET_VALUE_CHANGED";
    fetchAssets();
});
useUpdater().setCurrenciesValueChangedListener(() => {
    fetchCurrencies();
});

const onDenominationChanged = () => {
    updateDenominationExchangeRates();
    fetchAssets();
    fetchCurrencies();
};

const updateDenominationExchangeRates = async () => {
    const response = await api.exchangeRates(denomination.value);
    if (response.success()) {
        denominationExchangeRates.value = response.value();
        denominationToUSDExchangeRate.value = denominationExchangeRates.value.find(er => er.to == CurrencyCode.USD)?.value ?? 1;
    } else {
        Events.showErrorModal(response.error());
    }
};

const assetInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
    assets.value.map(a => ({ name: assetName(a), marketSize: a.marketSize })));

const currencyInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
    currencies.value.map(c => ({ name: currencyName(c), marketSize: c.marketSize })));

const assetName = (a: Asset) => t('asset-code.' + a.code);
const currencyName = (c: Currency) => t('currency-code.' + c.code);

onDenominationChanged();

const liveUpdatesToggledEventHandler = (e: Event) => {
    liveUpdatesEnabled.value = (e as CustomEvent).detail as boolean;
    useUpdater().setPaused(!liveUpdatesEnabled.value);
};

const denominationChangedEventHandler = (e: Event) => {
    denomination.value = (e as CustomEvent).detail as CurrencyCode;
    onDenominationChanged();
};

onMounted(() => {
    document.addEventListener('mh.live-updates-toggled', liveUpdatesToggledEventHandler);
    document.addEventListener('mh.denomination-changed', denominationChangedEventHandler);
});

onUnmounted(() => {
    document.removeEventListener('mh.live-updates-toggled', liveUpdatesToggledEventHandler);
    document.removeEventListener('mh.denomination-changed', denominationChangedEventHandler);
});

</script>

<template>
    <markets-header :denomination="denomination" t-namespace="markets-header." :t="t"
        :denominationExchangeRates="denominationExchangeRates.map(d => ({ name: d.to, exchangeRate: d.value }))">
    </markets-header>
    <assets-and-currencies t-namespace="assets-and-currencies." :t="t"
        :assets="assets.map(a => ({ id: a.code, name: assetName(a), marketSize: a.marketSize, denomination: a.denomination }))"
        :assetsValueChangeReason="assetsValueChangeReason"
        :currencies="currencies.map(c => ({ id: c.code, name: currencyName(c), marketSize: c.marketSize, denomination: c.denomination }))"
        :denomination="denomination">
    </assets-and-currencies>
    <markets-projections t-namespace="markets-projections." :t="t" :assets="assetInputOptions"
        :currencies="currencyInputOptions">
    </markets-projections>
</template>
