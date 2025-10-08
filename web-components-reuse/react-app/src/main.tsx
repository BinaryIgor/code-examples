import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import './i18n';
import App from './App.tsx';

// @ts-ignore
import { register } from './custom-header.js';

// @ts-ignore
import { registerComponents } from './web-components.js';

registerComponents();

register();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
