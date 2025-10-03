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
            calculatorHeader: 'Calculator'
        }
    }
});

createApp(App)
    .use(router)
    .use(i18n)
    .mount('#app');