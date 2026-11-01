<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)
const scrolled = ref(false)

const onHero = computed(() => route.name === 'home' && !scrolled.value)
const fullBleed = computed(() => ['explore', 'trip'].includes(route.name))

const links = computed(() => {
  const base = [
    { to: { name: 'explore' }, label: 'Explore' },
    { to: { name: 'planner' }, label: 'Plan a trip' },
  ]
  if (auth.role === 'TOURIST') {
    base.push({ to: { name: 'trips' }, label: 'My trips' }, { to: { name: 'bookings' }, label: 'Bookings' })
  }
  if (auth.role === 'PROVIDER') base.push({ to: { name: 'provider-bookings' }, label: 'Dashboard' })
  if (auth.role === 'ADMIN') base.push({ to: { name: 'admin-pois' }, label: 'Admin' })
  return base
})

const initials = computed(() =>
  (auth.user?.fullName ?? '').split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase(),
)

const onScroll = () => (scrolled.value = window.scrollY > 40)
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
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
        'inset-x-0 top-0 z-[1000] transition-all duration-300',
        route.name === 'home' ? 'fixed' : 'sticky',
        onHero ? 'bg-gradient-to-b from-black/55 to-transparent text-white' : 'border-b border-stone-200/80 bg-white/80 text-ink backdrop-blur-xl',
      ]"
    >
      <nav class="mx-auto flex h-16 max-w-7xl items-center justify-between gap-6 px-4 sm:px-6">
        <RouterLink :to="{ name: 'home' }" class="flex items-center gap-2.5 text-[17px] font-semibold tracking-tight">
          <img src="/favicon.svg" alt="" class="h-7 w-7" />
          Sayakat
        </RouterLink>

        <div class="hidden items-center gap-1 md:flex">
          <RouterLink
            v-for="link in links"
            :key="link.label"
            :to="link.to"
            :class="[
              'rounded-full px-3.5 py-1.5 text-sm transition',
              onHero ? 'text-white/80 hover:bg-white/10 hover:text-white' : 'text-stone-600 hover:bg-stone-100 hover:text-ink',
            ]"
            :active-class="onHero ? '!bg-white/15 !text-white' : '!bg-stone-100 !text-ink'"
          >
            {{ link.label }}
          </RouterLink>
        </div>

        <div class="hidden items-center gap-2 md:flex">
          <template v-if="auth.isAuthenticated">
            <span
              :class="['flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold',
                       onHero ? 'bg-white/15 text-white' : 'bg-stone-900 text-white']"
              v-tooltip.bottom="auth.user.fullName"
            >{{ initials }}</span>
            <button
              :class="['rounded-full px-3 py-1.5 text-sm transition', onHero ? 'text-white/80 hover:text-white' : 'text-stone-500 hover:text-ink']"
              @click="logout"
            >
              Sign out
            </button>
          </template>
          <template v-else>
            <RouterLink
              :to="{ name: 'login' }"
              :class="['rounded-full px-3.5 py-1.5 text-sm', onHero ? 'text-white/85 hover:text-white' : 'text-stone-600 hover:text-ink']"
            >
              Sign in
            </RouterLink>
            <RouterLink
              :to="{ name: 'register' }"
              :class="['rounded-full px-4 py-1.5 text-sm font-medium transition',
                       onHero ? 'bg-white text-ink hover:bg-stone-100' : 'bg-ink text-white hover:bg-stone-800']"
            >
              Get started
            </RouterLink>
          </template>
        </div>

        <button class="md:hidden" aria-label="Menu" @click="menuOpen = !menuOpen">
          <i :class="['pi text-lg', menuOpen ? 'pi-times' : 'pi-bars']" />
        </button>
      </nav>

      <div v-if="menuOpen" class="border-t border-stone-200 bg-white px-4 pb-4 text-ink md:hidden">
        <RouterLink v-for="link in links" :key="link.label" :to="link.to" class="block py-3 text-[15px]">
          {{ link.label }}
        </RouterLink>
        <button v-if="auth.isAuthenticated" class="py-3 text-[15px] text-rose-600" @click="logout">Sign out</button>
        <template v-else>
          <RouterLink :to="{ name: 'login' }" class="block py-3 text-[15px]">Sign in</RouterLink>
          <RouterLink :to="{ name: 'register' }" class="block py-3 text-[15px] font-medium">Get started</RouterLink>
        </template>
      </div>
    </header>

    <main class="flex-1">
      <slot />
    </main>

    <footer v-if="!fullBleed" class="border-t border-stone-200 bg-white">
      <div class="mx-auto flex max-w-7xl flex-col gap-6 px-4 py-10 sm:flex-row sm:items-end sm:justify-between sm:px-6">
        <div>
          <div class="flex items-center gap-2.5 font-semibold tracking-tight">
            <img src="/favicon.svg" alt="" class="h-6 w-6" /> Sayakat
          </div>
          <p class="mt-2 max-w-sm text-sm text-stone-500">
            Route planning and local hosts for travelling across Kyrgyzstan.
          </p>
        </div>
        <p class="text-xs text-stone-400">
          Diploma project · Ala-Too International University · Map data © OpenStreetMap contributors
        </p>
      </div>
    </footer>
  </div>
</template>
