import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import Tooltip from 'primevue/tooltip'
import { definePreset } from '@primeuix/themes'
import Aura from '@primeuix/themes/aura'
import 'primeicons/primeicons.css'
import 'leaflet/dist/leaflet.css'
import './style.css'

import App from './App.vue'
import router from './router'
import { reveal } from './directives/reveal'

const SayakatPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#eef4fa', 100: '#d5e3f1', 200: '#aac6e2', 300: '#7fa6cc', 400: '#5588b6',
      500: '#2c6aa0', 600: '#245c91', 700: '#1d4e89', 800: '#174271', 900: '#14365c', 950: '#0b2240',
    },
  },
})

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(PrimeVue, {
  theme: {
    preset: SayakatPreset,
    options: {
      darkModeSelector: '.app-dark',
      cssLayer: { name: 'primevue', order: 'theme, base, primevue' },
    },
  },
})
app.use(ToastService)
app.use(ConfirmationService)
app.directive('tooltip', Tooltip)
app.directive('reveal', reveal)
app.mount('#app')
