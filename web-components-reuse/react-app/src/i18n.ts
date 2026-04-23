import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const enTranslations = {
  'errors': {
    'title': "Something went wrong...",
    'UnknownFetchError': "Unknown error while fetching data",
    'UnsupportedErrorFormat': "Server responded in an unexpected way"
  },
  'currency-code': {
    'USD': "US Dollar",
    'EUR': "Euro",
    'JPY': "Japanesse Yen",
    'GBP': "British Pound",
    'CNY': "Chinese Yuan",
    'PLN': "Polish Zloty"
  },
  'asset-code': {
    'BONDS': "Bonds",
    'STOCKS': "Stocks",
    'GOLD': "Gold",
    'CASH': "Cash Reserves",
    'RLEST': "Real Estate",
    'BTC': "Bitcoin"
  },
  'markets-header': {
    'markets-in': "Markets in",
    'live-updates': "Live updates:",
    'live-updates-on': "ON",
    'live-updates-off': "OFF"
  },
  'assets-and-currencies': {
    'assets-header': "Assets",
    'currencies-header': "Currencies",
    'market-size-label': "Market size",
    'previous-market-size-label': "Previous market size",
    'up-by-info': "UP by",
    'down-by-info': "DOWN by",
    'daily-turnover-label': "Daily turnover",
    'yearly-turnover-label': "Yearly turnover"
  },
  'markets-projections': {
    'projections-header': 'Projections',
    'markets-comparator': {
      'asset-or-currency-input-placeholder': 'Asset/Currency',
      'market-size-input-label': 'market size',
      'days-turnover-input-label': 'days turnover',
      'markets-to': 'to'
    },
    'projections-calculator': {
      'asset-or-currency-placeholder': 'Asset/Currency',
      'asset-or-currency-expected-annual-growth-rate': 'expected annual growth rate',
      'results-in-header': 'In',
      'year': 'year',
      'years': 'years'
    }
  }
};

i18n.use(initReactI18next)
  .init({
    resources: {
      en: {
        translation: enTranslations
      }
    },
    lng: "en",
    // language to use, more information here: https://www.i18next.com/overview/configuration-options#languages-namespaces-resources
    // you can use the i18n.changeLanguage function to change the language manually: https://www.i18next.com/overview/api#changelanguage
    // if you're using a language detector, do not define the lng option
    interpolation: {
      escapeValue: false // react already saves from xss
    }
  });

export default i18n;