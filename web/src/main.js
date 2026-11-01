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
      50: '{teal.50}', 100: '{teal.100}', 200: '{teal.200}', 300: '{teal.300}', 400: '{teal.400}',
      500: '{teal.500}', 600: '{teal.600}', 700: '{teal.700}', 800: '{teal.800}', 900: '{teal.900}', 950: '{teal.950}',
    },
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff', 50: '{stone.50}', 100: '{stone.100}', 200: '{stone.200}', 300: '{stone.300}',
          400: '{stone.400}', 500: '{stone.500}', 600: '{stone.600}', 700: '{stone.700}', 800: '{stone.800}',
          900: '{stone.900}', 950: '{stone.950}',
        },
        primary: { color: '{teal.600}', hoverColor: '{teal.700}', activeColor: '{teal.800}' },
      },
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
