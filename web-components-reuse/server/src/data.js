/** 
 * @enum {string}
 */
const AssetCode = {
  BONDS: "BONDS",
  STOCKS: "STOCKS",
  GOLD: "GOLD",
  CASH: "CASH",
  RLEST: "RLEST",
  BTC: "BTC"
};

/**
 * @enum {string}
 */
const CurrencyCode = {
  USD: "USD",
  EUR: "EUR",
  JPY: "JPY",
  GBP: "GBP",
  CNY: "CNY",
  PLN: "PLN"
};

/**
 * @typedef {Object} Asset
 * @property {AssetCode} code
 * @property {number} marketSize
 * @property {CurrencyCode} denomination
 * 
 * @typedef {Object} Currency
 * @property {CurrencyCode} code
 * @property {number} marketSize
 * @property {CurrencyCode} denomination
 * 
 * @typedef {Object} ExchangeRate
 * @property {CurrencyCode} from
 * @property {CurrencyCode} to
 * @property {number} value
 */

// TODO: percentage of all global assets?
/** @type {Asset[]} */
const assets = [
  {
    code: AssetCode.BONDS,
    marketSize: 145.1e12,
    denomination: CurrencyCode.USD
  },
  {
    code: AssetCode.STOCKS,
    marketSize: 126.7e12,
    denomination: CurrencyCode.USD

  },
  {
    code: AssetCode.GOLD,
    marketSize: 22.6e12,
    denomination: CurrencyCode.USD
  },
  {
    code: AssetCode.CASH,
    marketSize: 12.6e12,
    denomination: CurrencyCode.USD
  },
  {
    code: AssetCode.RLEST,
    marketSize: 12.5e12,
    denomination: CurrencyCode.USD
  },
  {
    code: AssetCode.BTC,
    marketSize: 2.3e12,
    denomination: CurrencyCode.USD
  }
];
let nextAssets = assets;

/** @type {Currency[]} */
const currencies = [
  {
    code: CurrencyCode.USD,
    marketSize: 6.639e12,
    denomination: CurrencyCode.USD
  },
  {
    code: CurrencyCode.EUR,
    marketSize: 2.292e12,
    denomination: CurrencyCode.USD
  },
  {
    code: CurrencyCode.JPY,
    marketSize: 1.253e12,
    denomination: CurrencyCode.USD
  },
  {
    code: CurrencyCode.GBP,
    marketSize: 968e9,
    denomination: CurrencyCode.USD
  },
  {
    code: CurrencyCode.CNY,
    marketSize: 526.2e9,
    denomination: CurrencyCode.USD
  },
  {
    code: CurrencyCode.PLN,
    marketSize: 13e9,
    denomination: CurrencyCode.USD
  }
];
let nextCurrencies = currencies;

const usdExchangeRates = [
  {
    code: CurrencyCode.USD,
    value: 1
  },
  {
    code: CurrencyCode.EUR,
    value: 0.85
  },
  {
    code: CurrencyCode.JPY,
    value: 148
  },
  {
    code: CurrencyCode.GBP,
    value: 0.73
  },
  {
    code: CurrencyCode.CNY,
    value: 7.11
  },
  {
    code: CurrencyCode.PLN,
    value: 3.63
  },
];
let nextUsdExchangeRates = usdExchangeRates;

let assetsVersion = 1;
let currenciesVersion = 1;
let exchangeRatesVersion = 1;

export function versionedAssets(denomination, version) {
  const denominatedAsetsVersion = prefixedVersion(denomination, assetsVersion);
  if (version == denominatedAsetsVersion) {
    return null;
  }

  const exchangeRateValue = exchangeRateFor(denomination);
  const denominatedAssets = nextAssets.map(a => ({ ...a, marketSize: Math.round(a.marketSize * exchangeRateValue), denomination }));

  return {
    assets: denominatedAssets,
    responseVersion: denominatedAsetsVersion
  };
}

// router.get('/currencies', (req, res) => {
//   const denomination = req.query.denomination ?? CurrencyCode.USD;
//   const clientCurrenciesVersion = versionFromHeader(req) ?? prefixedVersion(denomination, 0);
//   const serverCurrenciesVersion = prefixedVersion(denomination, currenciesVersion);
//   if (clientCurrenciesVersion == serverCurrenciesVersion) {
//     returnNotModified(res);
//     return;
//   }

//   const exchangeRateValue = exchangeRateFor(denomination);
//   const denominatedCurrencies = nextCurrencies.map(c => ({ ...c, marketSize: Math.round(c.marketSize * exchangeRateValue), denomination }));
//   returnVersionedJson(res, serverCurrenciesVersion, {
//     currencies: denominatedCurrencies,
//     responseVersion: serverCurrenciesVersion
//   });
// });

// router.get('/exchange-rates/:from', (req, res) => {
//   const from = req.params.from;

//   const clientExchangeRatesVersion = versionFromHeader(req) ?? prefixedVersion(from, 0);
//   const serverExchangeRatesVersion = prefixedVersion(from, exchangeRatesVersion);
//   if (clientExchangeRatesVersion == serverExchangeRatesVersion) {
//     returnNotModified(res);
//     return;
//   }

//   const fromRequestedToDollarExchangeRate = 1 / exchangeRateFor(from);

//   const exchangeRates = nextUsdExchangeRates.map(er => ({
//     from,
//     to: er.code,
//     value: Math.round(fromRequestedToDollarExchangeRate * er.value * 100) / 100.0
//   }));

//   returnVersionedJson(res, serverExchangeRatesVersion, {
//     exchangeRates, responseVersion: serverExchangeRatesVersion
//   });
// });

function exchangeRateFor(denomination) {
  if (denomination == CurrencyCode.USD) {
    return 1;
  }
  const exchangeRate = nextUsdExchangeRates.find(er => er.code === denomination);
  if (!exchangeRate) {
    throw new ValidationError(`There is no exchange rate for ${denomination} denomination!`);
  }
  return exchangeRate.value;
}

function prefixedVersion(prefix, version) {
  return `${prefix}:${version}`;
}

function versionFromHeader(req) {
  return req.headers['If-None-Match'];
}

function returnVersionedJson(res, version, object, status = 200) {
  res.setHeader('ETag', version);
  res.status(status).send(object);
}

function returnNotModified(res) {
  res.status(304).send();
}

export function scheduleDataRandomizer() {
  return setInterval(() => {
    if (Math.random() > 0.5) {
      randomizeAssets();
    }
    if (Math.random() > 0.5) {
      randomizeCurrencies();
    }
    if (Math.random() > 0.5) {
      randomizeExchangeRates();
    }
  }, 1000);
}

function randomizeAssets() {
  nextAssets = nextAssets.map(a => ({ ...a, marketSize: a.marketSize * nextValueMultiplier(0.9, 1.1) }));
  assetsVersion++;
}

function randomizeCurrencies() {
  nextCurrencies = nextCurrencies.map(c => ({ ...c, marketSize: c.marketSize * nextValueMultiplier(0.9, 1.1) }));
  currenciesVersion++;
}

function randomizeExchangeRates() {
  nextUsdExchangeRates = nextUsdExchangeRates.map(er => ({
    ...er,
    value: er.code == CurrencyCode.USD ? er.value : er.value * nextValueMultiplier(0.9, 1.1)
  }));
  exchangeRatesVersion++;
}

function nextValueMultiplier(min, max) {
  if (min > max) {
    throw new Error(`Min (${min}) cannot be greater than max (${max})`);
  }
  return min + (max - min) * Math.random();
}
