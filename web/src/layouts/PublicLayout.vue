<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)

const overlay = computed(() => route.name === 'home')

const links = computed(() => {
  const base = [
    { to: { name: 'explore' }, label: 'Explore' },
    { to: { name: 'planner' }, label: 'Plan a trip' },
  ]
  if (auth.role === 'TOURIST') {
    base.push({ to: { name: 'trips' }, label: 'My trips' }, { to: { name: 'bookings' }, label: 'Bookings' })
  }
  if (auth.role === 'PROVIDER') base.push({ to: { name: 'provider-bookings' }, label: 'Provider dashboard' })
  if (auth.role === 'ADMIN') base.push({ to: { name: 'admin-pois' }, label: 'Admin' })
  return base
})

watch(() => route.fullPath, () => (menuOpen.value = false))

function logout() {
  auth.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <header
      :class="[
        'z-[1000] w-full transition-colors',
        overlay ? 'absolute top-0 text-white' : 'sticky top-0 border-b border-lake-100 bg-snow/90 backdrop-blur',
      ]"
    >
      <nav class="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6">
        <RouterLink :to="{ name: 'home' }" class="flex items-center gap-2 font-display text-2xl font-semibold">
          <img src="/favicon.svg" alt="" class="h-8 w-8" />
          Sayakat
        </RouterLink>

        <div class="hidden items-center gap-6 md:flex">
          <RouterLink
            v-for="link in links"
            :key="link.label"
            :to="link.to"
            class="text-sm font-medium opacity-85 transition hover:opacity-100"
            active-class="!opacity-100 underline decoration-ochre-500 decoration-2 underline-offset-8"
          >
            {{ link.label }}
          </RouterLink>
          <template v-if="auth.isAuthenticated">
            <span class="text-sm opacity-70">{{ auth.user.fullName }}</span>
            <Button label="Sign out" size="small" :severity="overlay ? 'contrast' : 'secondary'" outlined @click="logout" />
          </template>
          <template v-else>
            <RouterLink :to="{ name: 'login' }" class="text-sm font-medium">Sign in</RouterLink>
            <RouterLink :to="{ name: 'register' }">
              <Button label="Join" size="small" class="!border-ochre-500 !bg-ochre-500 !text-lake-950" />
            </RouterLink>
          </template>
        </div>

        <button class="md:hidden" aria-label="Menu" @click="menuOpen = !menuOpen">
          <i :class="['pi text-xl', menuOpen ? 'pi-times' : 'pi-bars']" />
        </button>
      </nav>

      <div v-if="menuOpen" class="border-t border-lake-100 bg-snow px-4 pb-4 text-lake-950 md:hidden">
        <RouterLink v-for="link in links" :key="link.label" :to="link.to" class="block py-3 font-medium">
          {{ link.label }}
        </RouterLink>
        <button v-if="auth.isAuthenticated" class="py-3 font-medium text-felt-500" @click="logout">Sign out</button>
        <template v-else>
          <RouterLink :to="{ name: 'login' }" class="block py-3 font-medium">Sign in</RouterLink>
          <RouterLink :to="{ name: 'register' }" class="block py-3 font-medium">Join</RouterLink>
        </template>
      </div>
    </header>

    <main class="flex-1">
      <slot />
    </main>

    <footer class="bg-lake-950 text-lake-100">
      <div class="ornament" />
      <div class="mx-auto flex max-w-7xl flex-col gap-2 px-4 py-8 text-sm sm:flex-row sm:justify-between sm:px-6">
        <p class="font-display text-lg text-white">Sayakat — саякат, a journey.</p>
        <p class="opacity-70">Diploma project · Ala-Too International University · Map data © OpenStreetMap contributors</p>
      </div>
    </footer>
  </div>
</template>
