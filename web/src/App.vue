<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import PublicLayout from '@/layouts/PublicLayout.vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'

const route = useRoute()
const layout = computed(() => (route.meta.layout === 'dashboard' ? DashboardLayout : PublicLayout))
</script>

<template>
  <Toast position="bottom-right" />
  <ConfirmDialog />
  <component :is="layout">
    <RouterView />
  </component>
</template>
