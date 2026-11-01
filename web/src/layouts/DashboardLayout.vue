<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const sidebarOpen = ref(false)

const items = computed(() =>
  auth.role === 'ADMIN'
    ? [
        { to: { name: 'admin-pois' }, label: 'Attractions', icon: 'pi-map-marker' },
        { to: { name: 'admin-tools' }, label: 'Dataset & matrix', icon: 'pi-cog' },
      ]
    : [
        { to: { name: 'provider-bookings' }, label: 'Booking requests', icon: 'pi-inbox' },
        { to: { name: 'provider-services' }, label: 'My services', icon: 'pi-briefcase' },
      ],
)

function logout() {
  auth.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="flex min-h-screen bg-surface-50">
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-[1100] w-64 flex-col bg-lake-950 text-lake-100 transition-transform md:static md:flex md:translate-x-0',
        sidebarOpen ? 'flex translate-x-0' : 'hidden -translate-x-full',
      ]"
    >
      <RouterLink :to="{ name: 'home' }" class="flex items-center gap-2 px-5 py-5 font-display text-xl text-white">
        <img src="/favicon.svg" alt="" class="h-7 w-7" /> Sayakat
      </RouterLink>
      <p class="px-5 pb-2 text-xs uppercase tracking-wider opacity-60">
        {{ auth.role === 'ADMIN' ? 'Administration' : 'Provider' }}
      </p>
      <nav class="flex-1 px-3">
        <RouterLink
          v-for="item in items"
          :key="item.label"
          :to="item.to"
          class="mb-1 flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition hover:bg-white/10"
          active-class="bg-white/15 text-white"
          @click="sidebarOpen = false"
        >
          <i :class="['pi', item.icon]" /> {{ item.label }}
        </RouterLink>
      </nav>
      <div class="border-t border-white/10 p-4 text-sm">
        <p class="truncate font-medium text-white">{{ auth.user?.fullName }}</p>
        <p class="truncate opacity-60">{{ auth.user?.email }}</p>
        <Button label="Sign out" icon="pi pi-sign-out" text size="small" class="!mt-2 !px-0 !text-lake-100" @click="logout" />
      </div>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col">
      <header class="flex items-center gap-3 border-b border-surface-200 bg-white px-4 py-3 md:hidden">
        <button aria-label="Menu" @click="sidebarOpen = !sidebarOpen"><i class="pi pi-bars text-lg" /></button>
        <span class="font-display text-lg">Sayakat</span>
      </header>
      <main class="mx-auto w-full max-w-6xl flex-1 p-4 sm:p-8">
        <slot />
      </main>
    </div>
  </div>
</template>
