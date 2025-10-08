import { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { type Asset, type Currency, type ExchangeRate, api } from './api';
import { CurrencyCode } from './codes';
import { useUpdater } from './updater';
import * as Events from './events';

export default function Home() {
  const { t } = useTranslation();

  const [liveUpdatesEnabled, setLiveUpdatesEnabled] = useState(true);
  const [denomination, setDenomination] = useState(CurrencyCode.USD);
  const [denominationToUSDExchangeRate, setDenominationToUSDExchangeRate] = useState<number>(1);
  const [denominationExchangeRates, setDenominationExchangeRates] = useState<ExchangeRate[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [assetValueChangeReason, setAssetValueChangeReason] = useState<string>();

  const assetInputOptions = useMemo<{ name: string, marketSize: number }[]>(() =>
    assets.map(a => ({ name: assetName(a), marketSize: a.marketSize })), [assets]);

  const currencyInputOptions = useMemo<{ name: string, marketSize: number }[]>(() =>
    currencies.map(c => ({ name: currencyName(c), marketSize: c.marketSize })), [currencies]);

  const assetName = (a: Asset) => t('asset-code.' + a.code);
  const currencyName = (c: Currency) => t('currency-code.' + c.code);

  const fetchAssets = () => api.assets(denomination)
    .then(r => {
      if (r.success()) {
        setAssets(r.value());
      } else {
        Events.showErrorModal(r.error());
      }
    });
  const fetchCurrencies = () => api.currencies(denomination)
    .then(r => {
      if (r.success()) {
        setCurrencies(r.value());
      } else {
        Events.showErrorModal(r.error());
      }
    });

  const updateDenominationExchangeRates = async () => {
    const response = await api.exchangeRates(denomination);
    if (response.success()) {
      const exchangeRates = response.value();
      setDenominationExchangeRates(exchangeRates);
      const denominationToUSDExchangeRate = exchangeRates.find(er => er.to == CurrencyCode.USD)?.value ?? 1;
      setDenominationToUSDExchangeRate(denominationToUSDExchangeRate);
      return { updatedExchangeRates: exchangeRates, updatedToUSDExchangeRate: denominationToUSDExchangeRate };
    } else {
      Events.showErrorModal(response.error());
      return { updatedExchangeRates: denominationExchangeRates, updatedToUSDExchangeRate: denominationToUSDExchangeRate };
    }
  };

  const onDenominationChanged = () => {
    updateDenominationExchangeRates();
    fetchAssets();
    fetchCurrencies();
  };

  const liveUpdatesToggledEventHandler = (e: Event) => {
    const enabled = (e as CustomEvent).detail as boolean;
    setLiveUpdatesEnabled(enabled);
    useUpdater().setPaused(!enabled);
  };

  const denominationChangedEventHandler = (e: Event) => {
    const denomination = (e as CustomEvent).detail as CurrencyCode;
    setDenomination(denomination);
    onDenominationChanged();
  };

  useEffect(() => {
    useUpdater().exchangeRatesChangedListener = async () => {
      const previousToUSDExchangeRate = denominationToUSDExchangeRate;
      const { updatedToUSDExchangeRate } = await updateDenominationExchangeRates();
      console.log("Previous vs updated", previousToUSDExchangeRate, updatedToUSDExchangeRate);
      if (previousToUSDExchangeRate != updatedToUSDExchangeRate) {

      }
    };
  }, []);


  return (<></>);
}