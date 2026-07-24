import { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { type Asset, type Currency, type ExchangeRate, api } from '../data/api';
import { CurrencyCode } from '../data/codes';
import { useUpdater } from '../data/updater';
import * as Events from './events';

export default function Home() {
  const { t } = useTranslation();

  const [, setLiveUpdatesEnabled] = useState(true);

  const [denomination, setDenomination] = useState(CurrencyCode.USD);
  const [, setDenominationToUSDExchangeRate] = useState<number>(1);
  const [denominationExchangeRates, setDenominationExchangeRates] = useState<ExchangeRate[]>([]);
  const [denominationExchangeRateVersion, setDenominationExchangeRateVersion] = useState<string>();

  const [assets, setAssets] = useState<Asset[]>([]);
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [assetsVersion, setAssetsVersion] = useState<string>();
  const [currenciesVersion, setCurrenciesVersion] = useState<string>();

  const assetName = (a: Asset) => t('asset-code.' + a.code);
  const currencyName = (c: Currency) => t('currency-code.' + c.code);
  const assetInputOptions = useMemo<{ name: string, marketSize: number }[]>(() =>
    assets.map(a => ({ name: assetName(a), marketSize: a.marketSize })), [assets]);
  const currencyInputOptions = useMemo<{ name: string, marketSize: number }[]>(() =>
    currencies.map(c => ({ name: currencyName(c), marketSize: c.marketSize })), [currencies]);

  const fetchAssets = async () => {
    const response = await api.assets(denomination, assetsVersion);
    if (response.success()) {
      if (response.hasValue()) {
        const responseValue = response.value();
        setAssets(responseValue.assets);
        setAssetsVersion(responseValue.responseVersion);
      }
    } else {
      Events.showErrorModal(response.error());
    }
  };
  const fetchCurrencies = async () => {
    const response = await api.currencies(denomination, currenciesVersion);
    if (response.success()) {
      if (response.hasValue()) {
        const responseValue = response.value();
        setCurrencies(responseValue.currencies);
        setCurrenciesVersion(responseValue.responseVersion);
      }
    } else {
      Events.showErrorModal(response.error());
    }
  };
  const fetchExchangeRates = async () => {
    const response = await api.exchangeRates(denomination, denominationExchangeRateVersion);
    if (response.success()) {
      if (response.hasValue()) {
        const responseValue = response.value();
        setDenominationExchangeRates(responseValue.exchangeRates);
        setDenominationToUSDExchangeRate(responseValue.exchangeRates.find(er => er.to == CurrencyCode.USD)?.value ?? 1);
        setDenominationExchangeRateVersion(responseValue.responseVersion);
      }
    } else {
      Events.showErrorModal(response.error());
    }
  };

  const liveUpdatesToggledEventHandler = (e: Event) => {
    const enabled = (e as CustomEvent).detail as boolean;
    setLiveUpdatesEnabled(enabled);
    useUpdater().paused = !enabled;
  };

  const denominationChangedEventHandler = (e: Event) => {
    const denomination = (e as CustomEvent).detail as CurrencyCode;
    setDenomination(denomination);
  };

  useEffect(() => {
    fetchAssets();
    useUpdater().fetchAssets = fetchAssets;
  }, [denomination, assetsVersion]);

  useEffect(() => {
    fetchCurrencies();
    useUpdater().fetchCurrencies = fetchCurrencies;
  }, [denomination, currenciesVersion]);

  useEffect(() => {
    fetchExchangeRates();
    useUpdater().fetchExchangeRates = fetchExchangeRates;
  }, [denomination, denominationExchangeRateVersion]);

  useEffect(() => {
    document.addEventListener('mh.live-updates-toggled', liveUpdatesToggledEventHandler);
    document.addEventListener('mh.denomination-changed', denominationChangedEventHandler);
    return () => {
      document.removeEventListener('mh.live-updates-toggled', liveUpdatesToggledEventHandler);
      document.removeEventListener('mh.denomination-changed', denominationChangedEventHandler);
      useUpdater().clear();
    };
  }, []);

  return (<>
    <markets-header denomination={denomination} t-namespace="markets-header." t={t}
      denominationExchangeRates={denominationExchangeRates.map(d => ({ name: d.to, exchangeRate: d.value }))}>
    </markets-header>
    <assets-and-currencies t-namespace="assets-and-currencies." t={t}
      assets={assets.map(a => ({ id: a.code, name: assetName(a), marketSize: a.marketSize, denomination: a.denomination }))}
      currencies={currencies.map(c => ({ id: c.code, name: currencyName(c), marketSize: c.marketSize, denomination: c.denomination }))}
      denomination={denomination}>
    </assets-and-currencies>
    <markets-projections t-namespace="markets-projections." t={t} assets={assetInputOptions} currencies={currencyInputOptions}>
    </markets-projections>
  </>);
}