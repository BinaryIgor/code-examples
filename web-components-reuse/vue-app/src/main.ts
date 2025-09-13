import { createApp } from 'vue';
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

createApp(App)
    .use(router)
    .mount('#app');