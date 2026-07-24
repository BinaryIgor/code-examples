export const CurrencyCode = {
  USD: "USD",
  EUR: "EUR",
  JPY: "JPY",
  GBP: "GBP",
  CNY: "CNY",
  PLN: "PLN"
};

export type CurrencyCode = typeof CurrencyCode[keyof typeof CurrencyCode];

export const currencyCodes: CurrencyCode[] = [
  CurrencyCode.USD,
  CurrencyCode.EUR,
  CurrencyCode.JPY,
  CurrencyCode.GBP,
  CurrencyCode.CNY,
  CurrencyCode.PLN
];

export const AssetCode = {
  BONDS: "BONDS",
  STOCKS: "STOCKS",
  GOLD: "GOLD",
  CASH: "CASH",
  RLEST: "RLEST",
  BTC: "BTC"
};

export type AssetCode = typeof AssetCode[keyof typeof AssetCode];