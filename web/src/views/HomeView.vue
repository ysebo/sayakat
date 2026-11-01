<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import MountainScene from '@/components/MountainScene.vue'
import { poiApi } from '@/api'
import { category, season } from '@/utils/format'

const featured = ref([])

onMounted(async () => {
  try {
    const all = await poiApi.list()
    featured.value = all.filter((p) => p.category !== 'CITY' && p.popularity >= 5).slice(0, 6)
  } catch {
    featured.value = []
  }
})

const steps = [
  { icon: 'pi-sliders-h', title: 'Tell us how you travel', text: 'Dates, budget, pace and what you love — lakes, trekking, Silk Road history or bazaars.' },
  { icon: 'pi-compass', title: 'Get a day-by-day route', text: 'An optimisation engine picks the places that fit: opening hours, seasons, mountain roads and where to sleep.' },
  { icon: 'pi-home', title: 'Book local hosts', text: 'Request yurt camps, guesthouses, guides and drivers along your route — straight from the itinerary.' },
]
</script>

<template>
  <section class="relative flex min-h-[92vh] items-center overflow-hidden text-white">
    <MountainScene />
    <div class="absolute inset-0 bg-gradient-to-r from-lake-950/75 via-lake-950/30 to-transparent" />
    <div class="relative mx-auto w-full max-w-7xl px-4 pb-40 pt-32 sm:px-6">
      <p class="mb-4 text-sm font-medium uppercase tracking-[0.25em] text-ochre-300">Саякат · journey</p>
      <h1 class="max-w-3xl text-5xl leading-[1.05] font-semibold drop-shadow-lg sm:text-7xl">
        Kyrgyzstan, planned around&nbsp;you.
      </h1>
      <p class="mt-6 max-w-xl text-lg text-lake-100">
        From the shores of Issyk-Kul to the yurts of Son-Kul — a route that respects seasons, mountain roads and
        your budget, with local hosts one click away.
      </p>
      <div class="mt-10 flex flex-wrap gap-3">
        <RouterLink :to="{ name: 'planner' }">
          <Button label="Plan my trip" icon="pi pi-arrow-right" icon-pos="right" size="large"
                  class="!border-ochre-500 !bg-ochre-500 !px-6 !text-lake-950" />
        </RouterLink>
        <RouterLink :to="{ name: 'explore' }">
          <Button label="Explore the map" size="large" severity="contrast" outlined class="!border-white !text-white" />
        </RouterLink>
      </div>
    </div>
  </section>

  <section class="mx-auto max-w-7xl px-4 py-20 sm:px-6">
    <h2 v-reveal class="text-3xl font-semibold sm:text-4xl">How it works</h2>
    <div class="mt-10 grid gap-6 md:grid-cols-3">
      <div
        v-for="(step, i) in steps"
        :key="step.title"
        v-reveal="i * 120"
        class="rounded-2xl border border-lake-100 bg-white p-6 shadow-sm"
      >
        <div class="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-lake-50 text-lake-700">
          <i :class="['pi text-xl', step.icon]" />
        </div>
        <h3 class="text-xl font-semibold">{{ i + 1 }}. {{ step.title }}</h3>
        <p class="mt-2 text-lake-900/75">{{ step.text }}</p>
      </div>
    </div>
  </section>

  <div class="ornament mx-auto max-w-7xl" />

  <section v-if="featured.length" class="mx-auto max-w-7xl px-4 py-20 sm:px-6">
    <div class="flex items-end justify-between gap-4">
      <h2 v-reveal class="text-3xl font-semibold sm:text-4xl">Places people dream about</h2>
      <RouterLink :to="{ name: 'explore' }" class="hidden text-sm font-medium text-lake-700 sm:block">
        All places <i class="pi pi-arrow-right text-xs" />
      </RouterLink>
    </div>
    <div class="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      <article
        v-for="(poi, i) in featured"
        :key="poi.id"
        v-reveal="(i % 3) * 100"
        class="group overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-lake-100 transition hover:-translate-y-1 hover:shadow-lg"
      >
        <div class="relative h-36" :style="{ background: `linear-gradient(135deg, ${category(poi.category).color}, #0b2240)` }">
          <i :class="['pi absolute right-5 bottom-4 text-5xl text-white/25', category(poi.category).icon]" />
          <span class="absolute left-4 top-4 rounded-full bg-white/20 px-3 py-1 text-xs font-medium text-white backdrop-blur">
            {{ category(poi.category).label }}
          </span>
        </div>
        <div class="p-5">
          <h3 class="text-xl font-semibold">{{ poi.name }}</h3>
          <p class="mt-1 text-sm text-lake-900/60">{{ poi.region }} · {{ season(poi.seasonFrom, poi.seasonTo) }}</p>
          <p class="mt-3 line-clamp-2 text-sm text-lake-900/80">{{ poi.description }}</p>
        </div>
      </article>
    </div>
  </section>

  <section class="bg-jailoo-700 text-white">
    <div class="mx-auto flex max-w-7xl flex-col items-start gap-6 px-4 py-16 sm:px-6 md:flex-row md:items-center md:justify-between">
      <div v-reveal>
        <h2 class="text-3xl font-semibold">Host travellers in your yurt camp or guesthouse?</h2>
        <p class="mt-2 max-w-xl text-jailoo-100">Publish your services and receive booking requests from tourists whose route passes by.</p>
      </div>
      <RouterLink :to="{ name: 'register', query: { role: 'PROVIDER' } }">
        <Button label="Become a provider" size="large" severity="contrast" />
      </RouterLink>
    </div>
  </section>
</template>
