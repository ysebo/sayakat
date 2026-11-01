<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Drawer from 'primevue/drawer'
import ProgressBar from 'primevue/progressbar'
import Message from 'primevue/message'
import LeafletMap from '@/components/LeafletMap.vue'
import BookingDialog from '@/components/BookingDialog.vue'
import { planApi } from '@/api'
import { errorMessage } from '@/api/http'
import { DAY_COLORS, SERVICE_TYPES, category, date, duration, escapeHtml, som, unitLabel } from '@/utils/format'

const props = defineProps({ id: { type: String, required: true } })

const plan = ref(null)
const error = ref('')
const activeDay = ref(null)
const focus = ref(null)
let timer = null

async function load() {
  try {
    plan.value = await planApi.get(props.id)
    if (plan.value.status === 'SOLVING') timer = setTimeout(load, 1500)
  } catch (e) {
    error.value = errorMessage(e)
  }
}
watch(() => props.id, () => { clearTimeout(timer); plan.value = null; load() }, { immediate: true })
onBeforeUnmount(() => clearTimeout(timer))

const solving = computed(() => plan.value?.status === 'SOLVING')
const days = computed(() => plan.value?.itinerary ?? [])
const dayColor = (i) => DAY_COLORS[i % DAY_COLORS.length]

const CAMP = 'No lodging nearby'
function nightLocation(d) {
  const day = days.value[d]
  const start = [plan.value.start.lat, plan.value.start.lon]
  if (day.lodgingLat != null) return [day.lodgingLat, day.lodgingLon]
  if (day.lodgingName === CAMP) return [day.stops.at(-1).lat, day.stops.at(-1).lon]
  if (!day.stops.length) return d > 0 ? nightLocation(d - 1) : start
  return start
}

const dayStart = (i) => (i === 0 ? [plan.value.start.lat, plan.value.start.lon] : nightLocation(i - 1))

const dayEnd = (day) =>
  day.dayIndex === days.value.length - 1 ? [plan.value.end.lat, plan.value.end.lon] : nightLocation(day.dayIndex)

const shownDays = computed(() => (activeDay.value == null ? days.value : days.value.filter((d) => d.dayIndex === activeDay.value)))

const polylines = computed(() =>
  shownDays.value.map((day) => {
    const points = [dayStart(day.dayIndex), ...day.stops.map((s) => [s.lat, s.lon])]
    const end = dayEnd(day)
    if (end) points.push(end)
    return { points, color: dayColor(day.dayIndex) }
  }),
)

const markers = computed(() => {
  if (!plan.value) return []
  const list = [{
    id: 'start', lat: plan.value.start.lat, lon: plan.value.start.lon, color: '#0b2240', icon: 'pi-flag-fill',
    popup: `<strong>${escapeHtml(plan.value.start.name)}</strong><br>Start & finish`,
  }]
  for (const day of shownDays.value) {
    day.stops.forEach((s) => {
      list.push({
        id: `${day.dayIndex}-${s.seq}`, lat: s.lat, lon: s.lon, color: dayColor(day.dayIndex),
        label: `${day.dayIndex + 1}`,
        popup: `<strong>${escapeHtml(s.name)}</strong><br>Day ${day.dayIndex + 1} · ${s.arrival}–${s.departure}`,
      })
    })
    if (day.lodgingLat != null) {
      list.push({
        id: `lodge-${day.dayIndex}`, lat: day.lodgingLat, lon: day.lodgingLon, color: '#3f6428', icon: 'pi-moon',
        popup: `<strong>${escapeHtml(day.lodgingName)}</strong><br>Night ${day.dayIndex + 1}`,
      })
    }
  }
  return list
})

const budgetUse = computed(() =>
  plan.value?.totalCostKgs ? Math.min(100, Math.round((plan.value.totalCostKgs / plan.value.budgetKgs) * 100)) : 0,
)

function selectDay(index) {
  activeDay.value = activeDay.value === index ? null : index
}

function focusStop(s) {
  focus.value = { lat: s.lat, lon: s.lon, zoom: 12 }
}

const drawerDay = ref(null)
const drawerOpen = ref(false)
const services = ref([])
const serviceType = ref(null)
const servicesLoading = ref(false)
const bookingService = ref(null)
const bookingOpen = ref(false)

async function openServices(day) {
  drawerDay.value = day
  drawerOpen.value = true
  await loadServices()
}

async function loadServices() {
  servicesLoading.value = true
  try {
    services.value = await planApi.servicesForDay(props.id, drawerDay.value.dayIndex, serviceType.value ?? undefined)
  } finally {
    servicesLoading.value = false
  }
}

function book(service) {
  bookingService.value = service
  bookingOpen.value = true
}
</script>

<template>
  <div v-if="error" class="mx-auto max-w-3xl p-8"><Message severity="error">{{ error }}</Message></div>

  <div v-else-if="!plan || solving" class="mx-auto flex min-h-[70vh] max-w-xl flex-col items-center justify-center px-6 text-center">
    <i class="pi pi-compass animate-spin text-5xl text-ochre-500 [animation-duration:3s]" />
    <h1 class="mt-6 text-3xl font-semibold">Weaving your route…</h1>
    <p class="mt-2 text-lake-900/70">
      Checking opening hours, mountain seasons and road times, comparing thousands of combinations.
      This takes up to 20 seconds.
    </p>
    <ProgressBar mode="indeterminate" class="mt-8 !h-1.5 w-full" />
  </div>

  <div v-else-if="plan.status === 'FAILED'" class="mx-auto max-w-3xl p-8">
    <Message severity="error">Planning failed: {{ plan.error }}</Message>
  </div>

  <div v-else class="flex flex-col lg:h-[calc(100vh-73px)] lg:flex-row">
    <section class="order-2 overflow-y-auto bg-snow lg:order-1 lg:w-[460px] lg:border-r lg:border-lake-100">
      <div class="border-b border-lake-100 p-5">
        <div class="flex items-center gap-2">
          <Tag :value="plan.algorithm === 'TIMEFOLD' ? 'Optimised' : 'Greedy'" severity="info" />
          <Tag v-if="plan.feasible" value="All constraints met" severity="success" icon="pi pi-check" />
          <Tag v-else value="Some constraints violated" severity="warn" icon="pi pi-exclamation-triangle" />
        </div>
        <h1 class="mt-3 text-3xl font-semibold">{{ plan.days }} days from {{ plan.start.name }}</h1>
        <p class="text-sm text-lake-900/60">{{ date(plan.startDate, { day: 'numeric', month: 'long', year: 'numeric' }) }}</p>

        <div class="mt-4 grid grid-cols-3 gap-3 text-center">
          <div class="rounded-xl bg-white p-3 ring-1 ring-lake-100">
            <p class="text-xs text-lake-900/60">Places</p>
            <p class="font-display text-xl font-semibold">{{ days.reduce((n, d) => n + d.stops.length, 0) }}</p>
          </div>
          <div class="rounded-xl bg-white p-3 ring-1 ring-lake-100">
            <p class="text-xs text-lake-900/60">On the road</p>
            <p class="font-display text-xl font-semibold">{{ duration(plan.totalTravelMin) }}</p>
          </div>
          <div class="rounded-xl bg-white p-3 ring-1 ring-lake-100">
            <p class="text-xs text-lake-900/60">Est. cost</p>
            <p class="font-display text-xl font-semibold">{{ Math.round(plan.totalCostKgs / 1000) }}k</p>
          </div>
        </div>
        <div class="mt-3">
          <div class="flex justify-between text-xs text-lake-900/60">
            <span>Budget used</span><span>{{ som(plan.totalCostKgs) }} / {{ som(plan.budgetKgs) }}</span>
          </div>
          <ProgressBar :value="budgetUse" :show-value="false" class="!mt-1 !h-1.5" />
        </div>
        <p class="mt-3 font-mono text-[11px] text-lake-900/40" v-tooltip.bottom="'hard / medium (value) / soft (travel) score'">
          score {{ plan.score }}
        </p>
      </div>

      <ol class="p-5">
        <li v-for="day in days" :key="day.dayIndex" class="relative mb-6 pl-8">
          <span class="absolute left-0 top-1 flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold text-white"
                :style="{ background: dayColor(day.dayIndex) }">{{ day.dayIndex + 1 }}</span>
          <span v-if="day.dayIndex < days.length - 1" class="absolute left-[11px] top-8 bottom-[-1.5rem] w-0.5 bg-lake-100" />

          <button class="w-full text-left" @click="selectDay(day.dayIndex)">
            <p class="font-display text-lg font-semibold">
              {{ date(day.date) }}
              <span v-if="activeDay === day.dayIndex" class="ml-1 text-xs font-normal text-lake-700">(shown on map)</span>
            </p>
            <p class="text-xs text-lake-900/60">{{ duration(day.travelMin) }} driving · {{ som(day.costKgs) }} · day ends {{ day.endsAt }}</p>
          </button>

          <ul class="mt-3 space-y-2">
            <li v-if="!day.stops.length" class="rounded-xl bg-white p-3 text-sm text-lake-900/60 ring-1 ring-lake-100">
              Free day / transfer
            </li>
            <li v-for="s in day.stops" :key="s.seq"
                class="flex cursor-pointer items-center gap-3 rounded-xl bg-white p-3 ring-1 ring-lake-100 transition hover:ring-lake-300"
                @click="focusStop(s)">
              <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-white"
                    :style="{ background: category(s.category).color }">
                <i :class="['pi text-xs', category(s.category).icon]" />
              </span>
              <div class="min-w-0 flex-1">
                <p class="truncate font-medium">{{ s.name }}</p>
                <p class="text-xs text-lake-900/60">{{ s.arrival }} – {{ s.departure }}</p>
              </div>
            </li>
            <li v-if="day.lodgingName" class="flex items-center gap-3 rounded-xl bg-jailoo-100/60 p-3 text-sm">
              <i class="pi pi-moon text-jailoo-700" />
              <span class="flex-1">{{ day.lodgingName }}</span>
            </li>
          </ul>
          <Button label="Hosts & guides nearby" icon="pi pi-users" text size="small" class="!mt-1 !px-0"
                  @click="openServices(day)" />
        </li>
      </ol>
    </section>

    <section class="order-1 h-[50vh] lg:order-2 lg:h-auto lg:flex-1">
      <LeafletMap :markers="markers" :polylines="polylines" :focus="focus" />
    </section>
  </div>

  <Drawer v-model:visible="drawerOpen" position="right" class="!w-[min(28rem,100vw)]"
          :header="drawerDay ? `Day ${drawerDay.dayIndex + 1} · ${date(drawerDay.date)}` : ''">
    <div class="mb-4 flex flex-wrap gap-2">
      <Button label="All" size="small" :outlined="serviceType !== null" @click="serviceType = null; loadServices()" />
      <Button v-for="t in SERVICE_TYPES" :key="t.value" :label="t.label" :icon="`pi ${t.icon}`" size="small"
              :outlined="serviceType !== t.value" @click="serviceType = t.value; loadServices()" />
    </div>
    <ProgressBar v-if="servicesLoading" mode="indeterminate" class="!h-1" />
    <p v-else-if="!services.length" class="text-sm text-lake-900/60">No services within 30 km yet.</p>
    <div v-for="s in services" :key="s.id" class="mb-3 rounded-xl p-4 ring-1 ring-surface-200">
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="font-medium">{{ s.title }}</p>
          <p class="text-xs text-surface-500">{{ s.providerName }}</p>
        </div>
        <Tag :value="SERVICE_TYPES.find((t) => t.value === s.type)?.label" severity="secondary" />
      </div>
      <p v-if="s.description" class="mt-2 text-sm text-surface-600">{{ s.description }}</p>
      <div class="mt-3 flex items-center justify-between">
        <span class="font-semibold">{{ som(s.priceKgs) }} <span class="text-xs font-normal text-surface-500">{{ unitLabel(s.priceUnit) }}</span></span>
        <Button label="Request" size="small" icon="pi pi-send" @click="book(s)" />
      </div>
    </div>
  </Drawer>

  <BookingDialog v-model:visible="bookingOpen" :service="bookingService" :date="drawerDay?.date" :plan-id="Number(id)" />
</template>
