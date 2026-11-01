<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import LeafletMap from '@/components/LeafletMap.vue'
import { poiApi } from '@/api'
import { INTEREST_OPTIONS, category, duration, satelliteTile, season } from '@/utils/format'

const router = useRouter()
const pois = ref([])
const heroMap = ref(null)
const tourIndex = ref(0)
let tourTimer = null

const TOUR = [
  { name: 'Issyk-Kul', region: 'Issyk-Kul region', lat: 42.42, lon: 77.25, zoom: 9 },
  { name: 'Son-Kul', region: 'Naryn region', lat: 41.83, lon: 75.13, zoom: 11 },
  { name: 'Ala-Archa', region: 'Chuy region', lat: 42.57, lon: 74.49, zoom: 12 },
  { name: 'Jeti-Oguz', region: 'Issyk-Kul region', lat: 42.33, lon: 78.22, zoom: 12 },
  { name: 'Sary-Chelek', region: 'Jalal-Abad region', lat: 41.86, lon: 71.95, zoom: 12 },
]
const current = computed(() => TOUR[tourIndex.value])

function nextStop() {
  tourIndex.value = (tourIndex.value + 1) % TOUR.length
  const t = TOUR[tourIndex.value]
  heroMap.value?.flyTo(t.lat, t.lon, t.zoom, 4)
}

onMounted(async () => {
  tourTimer = setInterval(nextStop, 9000)
  try {
    pois.value = await poiApi.list()
  } catch {
    pois.value = []
  }
})
onBeforeUnmount(() => clearInterval(tourTimer))

const featured = computed(() => pois.value.filter((p) => p.category !== 'CITY' && p.popularity >= 5).slice(0, 6))
const regions = computed(() => new Set(pois.value.map((p) => p.region)).size)

const quick = ref({ days: 5, interests: ['LAKE', 'NATURE'] })
function toggle(value) {
  const list = quick.value.interests
  quick.value.interests = list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
}
function startPlanning() {
  router.push({ name: 'planner', query: { days: quick.value.days, interests: quick.value.interests.join(',') } })
}

const steps = [
  { n: '01', title: 'Describe your trip', text: 'Dates, budget, pace and what you care about — alpine lakes, trekking, Silk Road history or bazaars.' },
  { n: '02', title: 'Get an optimised route', text: 'A constraint solver chooses places and orders them day by day, respecting opening hours, seasons, road times and where you sleep.' },
  { n: '03', title: 'Book local hosts', text: 'Send requests to yurt camps, guesthouses, guides and drivers along the route — right from the itinerary.' },
]

const constraints = [
  { level: 'Hard', items: ['Opening hours', 'Season of access', 'Daily time budget', 'Lodging within reach', 'Total budget'] },
  { level: 'Medium', items: ['Personal value of each place'] },
  { level: 'Soft', items: ['Time spent driving'] },
]
</script>

<template>
  <section class="grain relative isolate h-[100svh] min-h-[640px] overflow-hidden bg-ink text-white">
    <div class="absolute inset-0 -z-20 scale-105 [&_.leaflet-container]:!bg-ink">
      <LeafletMap ref="heroMap" basemap="satellite" :interactive="false" :fit-to-content="false"
                  :center="[TOUR[0].lat, TOUR[0].lon]" :zoom="TOUR[0].zoom" />
    </div>
    <div class="absolute inset-0 -z-10 bg-gradient-to-b from-black/60 via-black/25 to-black/80" />
    <div class="absolute inset-0 -z-10 bg-gradient-to-r from-black/60 via-transparent to-transparent" />

    <div class="mx-auto flex h-full max-w-7xl flex-col justify-end px-4 pb-10 sm:px-6 sm:pb-14">
      <div class="max-w-3xl">
        <p class="mb-5 inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3 py-1 text-xs backdrop-blur">
          <span class="h-1.5 w-1.5 rounded-full bg-teal-300" />
          Now over {{ current.name }} · {{ current.region }}
        </p>
        <h1 class="text-balance text-5xl font-medium leading-[1.02] sm:text-7xl lg:text-[84px]">
          Kyrgyzstan,
          <span class="font-serif italic font-normal text-teal-200">planned around you.</span>
        </h1>
        <p class="mt-6 max-w-xl text-lg text-white/75">
          From the shores of Issyk-Kul to the jailoo of Son-Kul — a day-by-day route that respects seasons,
          mountain roads and your budget, with local hosts one click away.
        </p>
      </div>

      <div class="mt-10 w-full max-w-4xl rounded-2xl border border-white/15 bg-white/10 p-2 backdrop-blur-xl">
        <div class="flex flex-col gap-2 lg:flex-row lg:items-center">
          <div class="flex items-center gap-3 rounded-xl bg-white/10 px-4 py-3">
            <span class="text-xs uppercase tracking-wider text-white/60">Days</span>
            <button class="h-7 w-7 rounded-full bg-white/10 hover:bg-white/20" aria-label="Fewer days"
                    @click="quick.days = Math.max(1, quick.days - 1)">−</button>
            <span class="w-6 text-center text-lg font-medium tabular-nums">{{ quick.days }}</span>
            <button class="h-7 w-7 rounded-full bg-white/10 hover:bg-white/20" aria-label="More days"
                    @click="quick.days = Math.min(21, quick.days + 1)">+</button>
          </div>
          <div class="flex flex-1 flex-wrap gap-1.5 px-2 py-1">
            <button
              v-for="i in INTEREST_OPTIONS.slice(0, 6)"
              :key="i.value"
              :class="['rounded-full px-3 py-1.5 text-[13px] transition',
                       quick.interests.includes(i.value) ? 'bg-white text-ink' : 'bg-white/10 text-white/85 hover:bg-white/20']"
              @click="toggle(i.value)"
            >
              {{ i.label }}
            </button>
          </div>
          <button class="flex items-center justify-center gap-2 rounded-xl bg-teal-400 px-6 py-3.5 font-medium text-ink transition hover:bg-teal-300"
                  @click="startPlanning">
            Plan my route <i class="pi pi-arrow-right text-sm" />
          </button>
        </div>
      </div>
    </div>
  </section>

  <section class="border-b border-stone-200 bg-white">
    <div class="mx-auto grid max-w-7xl grid-cols-2 gap-px bg-stone-200 sm:grid-cols-4">
      <div v-for="s in [
             { v: pois.length || '—', l: 'curated places' },
             { v: regions || '—', l: 'regions covered' },
             { v: '3', l: 'constraint levels' },
             { v: '< 20s', l: 'to an optimised route' },
           ]" :key="s.l" class="bg-white px-6 py-8">
        <p class="text-3xl font-medium tracking-tight tabular-nums">{{ s.v }}</p>
        <p class="mt-1 text-sm text-stone-500">{{ s.l }}</p>
      </div>
    </div>
  </section>

  <section class="mx-auto max-w-7xl px-4 py-24 sm:px-6">
    <div class="grid gap-12 lg:grid-cols-[1fr_2fr]">
      <div v-reveal>
        <p class="text-sm font-medium text-teal-700">How it works</p>
        <h2 class="mt-3 text-4xl font-medium leading-tight">
          Three steps from an idea to a <span class="font-serif italic">bookable</span> route.
        </h2>
      </div>
      <div class="grid gap-px overflow-hidden rounded-2xl border border-stone-200 bg-stone-200 sm:grid-cols-3">
        <div v-for="(step, i) in steps" :key="step.n" v-reveal="i * 100" class="bg-white p-6">
          <p class="font-mono text-xs text-stone-400">{{ step.n }}</p>
          <h3 class="mt-8 text-lg font-medium">{{ step.title }}</h3>
          <p class="mt-2 text-sm leading-relaxed text-stone-500">{{ step.text }}</p>
        </div>
      </div>
    </div>
  </section>

  <section v-if="featured.length" class="mx-auto max-w-7xl px-4 pb-24 sm:px-6">
    <div class="flex items-end justify-between gap-4">
      <h2 v-reveal class="text-4xl font-medium">Places worth the road</h2>
      <RouterLink :to="{ name: 'explore' }" class="hidden items-center gap-1.5 text-sm text-stone-600 hover:text-ink sm:flex">
        View all on the map <i class="pi pi-arrow-up-right text-xs" />
      </RouterLink>
    </div>
    <div class="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      <RouterLink
        v-for="(poi, i) in featured"
        :key="poi.id"
        v-reveal="(i % 3) * 90"
        :to="{ name: 'explore', query: { poi: poi.id } }"
        class="group block"
      >
        <div class="relative aspect-[4/3] overflow-hidden rounded-2xl bg-stone-200">
          <img :src="satelliteTile(poi.lat, poi.lon, 12)" :alt="poi.name" loading="lazy"
               class="h-full w-full scale-[1.25] object-cover transition duration-700 group-hover:scale-[1.4]" />
          <div class="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent" />
          <span class="absolute left-3 top-3 rounded-full bg-white/90 px-2.5 py-1 text-xs font-medium text-ink backdrop-blur">
            {{ category(poi.category).label }}
          </span>
          <span class="absolute bottom-3 left-3 font-mono text-[11px] text-white/80">
            {{ poi.lat.toFixed(3) }}°N {{ poi.lon.toFixed(3) }}°E
          </span>
        </div>
        <div class="mt-4 flex items-start justify-between gap-3">
          <div>
            <h3 class="text-lg font-medium">{{ poi.name }}</h3>
            <p class="text-sm text-stone-500">{{ poi.region }} · {{ season(poi.seasonFrom, poi.seasonTo) }}</p>
          </div>
          <span class="shrink-0 text-sm text-stone-500">{{ duration(poi.visitDurationMin) }}</span>
        </div>
      </RouterLink>
    </div>
  </section>

  <section class="bg-ink text-white">
    <div class="mx-auto grid max-w-7xl gap-12 px-4 py-24 sm:px-6 lg:grid-cols-2">
      <div v-reveal>
        <p class="text-sm font-medium text-teal-300">Under the hood</p>
        <h2 class="mt-3 text-4xl font-medium leading-tight">
          Not a list of tips — <span class="font-serif italic text-teal-200">a solved optimisation problem.</span>
        </h2>
        <p class="mt-5 max-w-lg text-stone-400">
          Every trip is modelled as a multi-day orienteering problem with time windows and lodging selection,
          and solved with Timefold. A greedy baseline is kept for comparison in the evaluation.
        </p>
        <RouterLink :to="{ name: 'planner' }"
                    class="mt-8 inline-flex items-center gap-2 rounded-full bg-white px-5 py-2.5 text-sm font-medium text-ink hover:bg-stone-200">
          Try the planner <i class="pi pi-arrow-right text-xs" />
        </RouterLink>
      </div>
      <div class="space-y-3">
        <div v-for="(c, i) in constraints" :key="c.level" v-reveal="i * 100"
             class="rounded-2xl border border-white/10 bg-white/[0.03] p-5">
          <p class="text-xs font-medium uppercase tracking-wider text-stone-400">{{ c.level }} constraints</p>
          <div class="mt-3 flex flex-wrap gap-2">
            <span v-for="item in c.items" :key="item" class="rounded-full border border-white/10 px-3 py-1 text-sm text-stone-200">
              {{ item }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </section>

  <section class="mx-auto max-w-7xl px-4 py-24 sm:px-6">
    <div v-reveal class="flex flex-col items-start justify-between gap-8 rounded-3xl border border-stone-200 bg-white p-8 sm:p-12 md:flex-row md:items-center">
      <div>
        <h2 class="text-3xl font-medium">Host travellers in your yurt camp or guesthouse?</h2>
        <p class="mt-2 max-w-xl text-stone-500">
          Publish your services and receive booking requests from tourists whose route passes by.
        </p>
      </div>
      <RouterLink :to="{ name: 'register', query: { role: 'PROVIDER' } }"
                  class="shrink-0 rounded-full bg-ink px-6 py-3 text-sm font-medium text-white hover:bg-stone-800">
        Become a provider
      </RouterLink>
    </div>
  </section>
</template>
