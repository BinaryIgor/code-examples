import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import './style.css';
import App from './App.vue';
import { createRouter, createWebHistory } from 'vue-router';
import Home from './components/Home.vue';

// @ts-ignore
import { registerComponents } from './components/lib/registry.js';

registerComponents();

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: Home },
    ]
});

const i18n = createI18n({
    legacy: false,
    locale: 'en',
    fallbackLocale: 'en',
    messages: {
        en: {
            'markets-header': {
                'markets-in': "Market in",
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
        }
    }
});

createApp(App)
    .use(router)
    .use(i18n)
    .mount('#app');