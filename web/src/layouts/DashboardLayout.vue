<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const sidebarOpen = ref(false)

const items = computed(() =>
  auth.role === 'ADMIN'
    ? [
        { to: { name: 'admin-pois' }, label: 'Attractions', icon: 'pi-map-marker' },
        { to: { name: 'admin-tools' }, label: 'Dataset & matrix', icon: 'pi-database' },
      ]
    : [
        { to: { name: 'provider-bookings' }, label: 'Requests', icon: 'pi-inbox' },
        { to: { name: 'provider-services' }, label: 'Services', icon: 'pi-th-large' },
      ],
)

const initials = computed(() =>
  (auth.user?.fullName ?? '').split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase(),
)

function logout() {
  auth.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="flex min-h-screen bg-stone-50">
    <div v-if="sidebarOpen" class="fixed inset-0 z-[1090] bg-black/30 md:hidden" @click="sidebarOpen = false" />
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-[1100] w-60 flex-col border-r border-stone-200 bg-white transition-transform md:sticky md:top-0 md:flex md:h-screen md:translate-x-0',
        sidebarOpen ? 'flex translate-x-0' : 'hidden -translate-x-full',
      ]"
    >
      <RouterLink :to="{ name: 'home' }" class="flex h-16 items-center gap-2.5 px-5 font-semibold tracking-tight">
        <img src="/favicon.svg" alt="" class="h-7 w-7" /> Sayakat
      </RouterLink>
      <p class="px-5 pb-2 pt-4 text-[11px] font-medium uppercase tracking-wider text-stone-400">
        {{ auth.role === 'ADMIN' ? 'Administration' : 'Provider' }}
      </p>
      <nav class="flex-1 space-y-0.5 px-3">
        <RouterLink
          v-for="item in items"
          :key="item.label"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-stone-600 transition hover:bg-stone-100 hover:text-ink"
          active-class="!bg-stone-100 !text-ink font-medium"
          @click="sidebarOpen = false"
        >
          <i :class="['pi text-[13px]', item.icon]" /> {{ item.label }}
        </RouterLink>
        <RouterLink
          :to="{ name: 'explore' }"
          class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-stone-600 transition hover:bg-stone-100 hover:text-ink"
        >
          <i class="pi pi-compass text-[13px]" /> Public site
        </RouterLink>
      </nav>
      <div class="flex items-center gap-3 border-t border-stone-200 p-4">
        <span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-ink text-xs font-semibold text-white">
          {{ initials }}
        </span>
        <div class="min-w-0 flex-1">
          <p class="truncate text-sm font-medium">{{ auth.user?.fullName }}</p>
          <p class="truncate text-xs text-stone-500">{{ auth.user?.email }}</p>
        </div>
        <button class="text-stone-400 transition hover:text-ink" aria-label="Sign out" v-tooltip.top="'Sign out'" @click="logout">
          <i class="pi pi-sign-out" />
        </button>
      </div>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col">
      <header class="flex h-14 items-center gap-3 border-b border-stone-200 bg-white px-4 md:hidden">
        <button aria-label="Menu" @click="sidebarOpen = true"><i class="pi pi-bars" /></button>
        <span class="font-semibold tracking-tight">Sayakat</span>
      </header>
      <main class="mx-auto w-full max-w-6xl flex-1 px-4 py-8 sm:px-8 sm:py-10">
        <slot />
      </main>
    </div>
  </div>
</template>
