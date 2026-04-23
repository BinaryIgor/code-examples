<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { type Asset, type Currency, type ExchangeRate, api } from '../data/api';
import { CurrencyCode } from '../data/codes';
import * as Events from './events';
import { useUpdater } from '../data/updater';

const { t } = useI18n();

const liveUpdatesEnabled = ref<boolean>(true);

const denomination = ref<CurrencyCode>(CurrencyCode.USD);
const denominationToUSDExchangeRate = ref<number>(1);
const denominationExchangeRates = ref<ExchangeRate[]>([]);
const denominationExchangeRateVersion = ref<string>();

const assets = ref<Asset[]>([]);
const assetsVersion = ref<string>();
const currencies = ref<Currency[]>([]);
const currenciesVersion = ref<string>();

const fetchAssets = async () => {
  const response = await api.assets(denomination.value, assetsVersion.value);
  if (response.success()) {
    if (response.hasValue()) {
      const responseValue = response.value();
      assets.value = responseValue.assets;
      assetsVersion.value = responseValue.responseVersion;
    }
  } else {
    Events.showErrorModal(response.error());
  }
};
const fetchCurrencies = async () => {
  const response = await api.currencies(denomination.value, currenciesVersion.value);
  if (response.success()) {
    if (response.hasValue()) {
      const responseValue = response.value();
      currencies.value = responseValue.currencies;
      currenciesVersion.value = responseValue.responseVersion;
    }
  } else {
    Events.showErrorModal(response.error());
  }
};
const fetchExchangeRates = async () => {
  const response = await api.exchangeRates(denomination.value, denominationExchangeRateVersion.value);
  if (response.success()) {
    if (response.hasValue()) {
      const responseValue = response.value();
      denominationExchangeRates.value = responseValue.exchangeRates;
      denominationToUSDExchangeRate.value = denominationExchangeRates.value.find(er => er.to == CurrencyCode.USD)?.value ?? 1;
      denominationExchangeRateVersion.value = responseValue.responseVersion;
    }
  } else {
    Events.showErrorModal(response.error());
  }
};

const onDenominationChanged = () => {
  fetchExchangeRates();
  fetchAssets();
  fetchCurrencies();
};

const assetInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
  assets.value.map(a => ({ name: assetName(a), marketSize: a.marketSize })));

const currencyInputOptions = computed<{ name: string, marketSize: number }[]>(() =>
  currencies.value.map(c => ({ name: currencyName(c), marketSize: c.marketSize })));

const assetName = (a: Asset) => t('asset-code.' + a.code);
const currencyName = (c: Currency) => t('currency-code.' + c.code);

const liveUpdatesToggledEventHandler = (e: Event) => {
  liveUpdatesEnabled.value = (e as CustomEvent).detail as boolean;
  useUpdater().paused = !liveUpdatesEnabled.value;
};

const denominationChangedEventHandler = (e: Event) => {
  denomination.value = (e as CustomEvent).detail as CurrencyCode;
  onDenominationChanged();
};

onMounted(() => {
  document.addEventListener('mh.live-updates-toggled', liveUpdatesToggledEventHandler);
  document.addEventListener('mh.denomination-changed', denominationChangedEventHandler);
  useUpdater().with(fetchAssets, fetchCurrencies, fetchExchangeRates);
});

onUnmounted(() => {
  document.removeEventListener('mh.live-updates-toggled', liveUpdatesToggledEventHandler);
  document.removeEventListener('mh.denomination-changed', denominationChangedEventHandler);
  useUpdater().clear();
});

onDenominationChanged();

</script>

<template>
  <markets-header :denomination="denomination" t-namespace="markets-header." :t="t"
    :denominationExchangeRates="denominationExchangeRates.map(d => ({ name: d.to, exchangeRate: d.value }))">
  </markets-header>
  <assets-and-currencies t-namespace="assets-and-currencies." :t="t"
    :assets="assets.map(a => ({ id: a.code, name: assetName(a), marketSize: a.marketSize, denomination: a.denomination }))"
    :currencies="currencies.map(c => ({ id: c.code, name: currencyName(c), marketSize: c.marketSize, denomination: c.denomination }))"
    :denomination="denomination">
  </assets-and-currencies>
  <markets-projections t-namespace="markets-projections." :t="t" :assets="assetInputOptions"
    :currencies="currencyInputOptions">
  </markets-projections>
</template>
