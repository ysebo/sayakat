<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import Drawer from 'primevue/drawer'
import Message from 'primevue/message'
import LeafletMap from '@/components/LeafletMap.vue'
import BookingDialog from '@/components/BookingDialog.vue'
import { planApi } from '@/api'
import { errorMessage } from '@/api/http'
import { DAY_COLORS, SERVICE_TYPES, category, compactSom, date, duration, escapeHtml, som, unitLabel } from '@/utils/format'

const props = defineProps({ id: { type: String, required: true } })

const plan = ref(null)
const error = ref('')
const activeDay = ref(null)
const focus = ref(null)
const elapsed = ref(0)
let timer = null
let clock = null

async function load() {
  try {
    plan.value = await planApi.get(props.id)
    if (plan.value.status === 'SOLVING') timer = setTimeout(load, 1500)
    else clearInterval(clock)
  } catch (e) {
    error.value = errorMessage(e)
  }
}
watch(
  () => props.id,
  () => {
    clearTimeout(timer)
    clearInterval(clock)
    plan.value = null
    elapsed.value = 0
    clock = setInterval(() => elapsed.value++, 1000)
    load()
  },
  { immediate: true },
)
onBeforeUnmount(() => {
  clearTimeout(timer)
  clearInterval(clock)
})

const solving = computed(() => !plan.value || plan.value.status === 'SOLVING')
const days = computed(() => plan.value?.itinerary ?? [])
const dayColor = (i) => DAY_COLORS[i % DAY_COLORS.length]
const placeCount = computed(() => days.value.reduce((n, d) => n + d.stops.length, 0))

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

const shownDays = computed(() =>
  activeDay.value == null ? days.value : days.value.filter((d) => d.dayIndex === activeDay.value),
)

const polylines = computed(() =>
  shownDays.value.map((day) => ({
    points: [dayStart(day.dayIndex), ...day.stops.map((s) => [s.lat, s.lon]), dayEnd(day)],
    color: dayColor(day.dayIndex),
  })),
)

const markers = computed(() => {
  if (!plan.value) return []
  const list = [{
    id: 'start', lat: plan.value.start.lat, lon: plan.value.start.lon, color: '#0c0a09', icon: 'pi-flag-fill',
    popup: `<strong>${escapeHtml(plan.value.start.name)}</strong><br><span style="color:#78716c">Start &amp; finish</span>`,
  }]
  for (const day of shownDays.value) {
    day.stops.forEach((s) => {
      list.push({
        id: `${day.dayIndex}-${s.seq}`, lat: s.lat, lon: s.lon, color: dayColor(day.dayIndex),
        label: `${day.dayIndex + 1}`,
        popup: `<strong>${escapeHtml(s.name)}</strong><br><span style="color:#78716c">Day ${day.dayIndex + 1} · ${s.arrival}–${s.departure}</span>`,
      })
    })
    if (day.lodgingLat != null) {
      list.push({
        id: `lodge-${day.dayIndex}`, lat: day.lodgingLat, lon: day.lodgingLon, color: '#44403c', icon: 'pi-moon',
        popup: `<strong>${escapeHtml(day.lodgingName)}</strong><br><span style="color:#78716c">Night ${day.dayIndex + 1}</span>`,
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

function setType(type) {
  serviceType.value = type
  loadServices()
}

function book(service) {
  bookingService.value = service
  bookingOpen.value = true
}

const solvingSteps = ['Reading 46 places and 25 lodgings', 'Checking seasons and opening hours', 'Optimising day-by-day order', 'Choosing where to sleep']
</script>

<template>
  <div v-if="error" class="mx-auto max-w-3xl p-8"><Message severity="error">{{ error }}</Message></div>

  <div v-else-if="solving" class="mx-auto flex min-h-[calc(100svh-64px)] max-w-md flex-col justify-center px-6">
    <div class="relative h-12 w-12">
      <span class="absolute inset-0 animate-ping rounded-full bg-teal-400/40" />
      <span class="relative flex h-12 w-12 items-center justify-center rounded-full bg-ink text-white">
        <i class="pi pi-compass" />
      </span>
    </div>
    <h1 class="mt-8 text-3xl font-medium">Building your route</h1>
    <p class="mt-2 text-stone-500">The solver compares thousands of combinations. Usually under 20 seconds.</p>
    <ul class="mt-8 space-y-3">
      <li v-for="(s, i) in solvingSteps" :key="s" class="flex items-center gap-3 text-sm">
        <i :class="['pi text-xs', elapsed > i * 3 + 2 ? 'pi-check text-teal-600' : elapsed > i * 3 ? 'pi-spin pi-spinner text-stone-500' : 'pi-circle text-stone-300']" />
        <span :class="elapsed > i * 3 ? 'text-ink' : 'text-stone-400'">{{ s }}</span>
      </li>
    </ul>
    <p class="mt-8 font-mono text-xs text-stone-400">{{ elapsed }}s</p>
  </div>

  <div v-else-if="plan.status === 'FAILED'" class="mx-auto max-w-3xl p-8">
    <Message severity="error">Planning failed: {{ plan.error }}</Message>
  </div>

  <div v-else class="flex flex-col lg:h-[calc(100svh-64px)] lg:flex-row">
    <section class="order-2 overflow-y-auto bg-white lg:order-1 lg:w-[440px] lg:border-r lg:border-stone-200">
      <div class="border-b border-stone-200 p-6">
        <div class="flex flex-wrap items-center gap-2 text-xs">
          <span class="rounded-full bg-stone-100 px-2.5 py-1 font-medium text-stone-700">
            {{ plan.algorithm === 'TIMEFOLD' ? 'Optimised' : 'Greedy' }}
          </span>
          <span v-if="plan.feasible" class="inline-flex items-center gap-1 rounded-full bg-teal-50 px-2.5 py-1 font-medium text-teal-700">
            <i class="pi pi-check text-[10px]" /> All constraints met
          </span>
          <span v-else class="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2.5 py-1 font-medium text-amber-700">
            <i class="pi pi-exclamation-triangle text-[10px]" /> Some constraints violated
          </span>
        </div>
        <h1 class="mt-4 text-3xl font-medium">{{ plan.days }} days from {{ plan.start.name }}</h1>
        <p class="mt-1 text-sm text-stone-500">{{ date(plan.startDate, { day: 'numeric', month: 'long', year: 'numeric' }) }}</p>

        <div class="mt-6 grid grid-cols-3 divide-x divide-stone-200 rounded-xl border border-stone-200">
          <div class="px-4 py-3">
            <p class="text-xs text-stone-500">Places</p>
            <p class="mt-0.5 text-xl font-medium tabular-nums">{{ placeCount }}</p>
          </div>
          <div class="px-4 py-3">
            <p class="text-xs text-stone-500">Driving</p>
            <p class="mt-0.5 text-xl font-medium tabular-nums">{{ duration(plan.totalTravelMin) }}</p>
          </div>
          <div class="px-4 py-3">
            <p class="text-xs text-stone-500">Cost</p>
            <p class="mt-0.5 text-xl font-medium tabular-nums">{{ compactSom(plan.totalCostKgs) }}</p>
          </div>
        </div>
        <div class="mt-4">
          <div class="flex justify-between text-xs text-stone-500">
            <span>Budget used</span><span class="tabular-nums">{{ budgetUse }}% of {{ som(plan.budgetKgs) }}</span>
          </div>
          <div class="mt-1.5 h-1.5 overflow-hidden rounded-full bg-stone-100">
            <div class="h-full rounded-full bg-teal-500 transition-all" :style="{ width: `${budgetUse}%` }" />
          </div>
        </div>
        <p class="mt-4 font-mono text-[11px] text-stone-400" v-tooltip.bottom="'hard / medium (value) / soft (driving)'">
          score {{ plan.score }}
        </p>
      </div>

      <div class="flex gap-1.5 overflow-x-auto border-b border-stone-200 px-6 py-3">
        <button :class="['shrink-0 rounded-full px-3 py-1 text-xs transition', activeDay == null ? 'bg-ink text-white' : 'bg-stone-100 text-stone-600']"
                @click="activeDay = null">All days</button>
        <button v-for="d in days" :key="d.dayIndex"
                :class="['flex shrink-0 items-center gap-1.5 rounded-full px-3 py-1 text-xs transition', activeDay === d.dayIndex ? 'bg-ink text-white' : 'bg-stone-100 text-stone-600']"
                @click="selectDay(d.dayIndex)">
          <span class="h-1.5 w-1.5 rounded-full" :style="{ background: dayColor(d.dayIndex) }" /> Day {{ d.dayIndex + 1 }}
        </button>
      </div>

      <ol class="px-6 py-6">
        <li v-for="day in days" :key="day.dayIndex" class="relative pb-8 pl-9 last:pb-2">
          <span class="absolute left-0 top-0.5 flex h-6 w-6 items-center justify-center rounded-full text-[11px] font-semibold text-white"
                :style="{ background: dayColor(day.dayIndex) }">{{ day.dayIndex + 1 }}</span>
          <span v-if="day.dayIndex < days.length - 1" class="absolute bottom-0 left-[11px] top-8 w-px bg-stone-200" />

          <div class="flex items-baseline justify-between gap-3">
            <p class="font-medium">{{ date(day.date) }}</p>
            <p class="text-xs text-stone-500 tabular-nums">{{ duration(day.travelMin) }} · {{ som(day.costKgs) }}</p>
          </div>

          <ul class="mt-3 space-y-1">
            <li v-if="!day.stops.length" class="rounded-lg bg-stone-50 px-3 py-2.5 text-sm text-stone-500">Transfer day</li>
            <li v-for="s in day.stops" :key="s.seq"
                class="group flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 transition hover:bg-stone-50"
                @click="focusStop(s)">
              <span class="w-11 shrink-0 font-mono text-xs text-stone-400">{{ s.arrival }}</span>
              <span class="h-2 w-2 shrink-0 rounded-full" :style="{ background: category(s.category).color }" />
              <span class="min-w-0 flex-1 truncate text-[15px]">{{ s.name }}</span>
              <i class="pi pi-arrow-up-right text-[10px] text-stone-300 transition group-hover:text-stone-500" />
            </li>
            <li v-if="day.lodgingName" class="flex items-center gap-3 px-3 py-2.5 text-sm text-stone-600">
              <span class="w-11 shrink-0 font-mono text-xs text-stone-400">{{ day.endsAt }}</span>
              <i class="pi pi-moon text-xs text-stone-400" />
              <span class="truncate">{{ day.lodgingName }}</span>
            </li>
          </ul>
          <button class="mt-2 inline-flex items-center gap-1.5 rounded-full border border-stone-200 px-3 py-1 text-xs text-stone-600 transition hover:border-stone-400 hover:text-ink"
                  @click="openServices(day)">
            <i class="pi pi-users text-[11px]" /> Hosts &amp; guides nearby
          </button>
        </li>
      </ol>
    </section>

    <section class="order-1 h-[50svh] lg:order-2 lg:h-auto lg:flex-1">
      <LeafletMap :markers="markers" :polylines="polylines" :focus="focus" />
    </section>
  </div>

  <Drawer v-model:visible="drawerOpen" position="right" class="!w-[min(28rem,100vw)]">
    <template #header>
      <div>
        <p class="text-xs text-stone-500">Day {{ drawerDay ? drawerDay.dayIndex + 1 : '' }} · {{ drawerDay ? date(drawerDay.date) : '' }}</p>
        <p class="text-lg font-medium">Hosts &amp; guides nearby</p>
      </div>
    </template>
    <div class="mb-5 inline-flex rounded-full border border-stone-200 p-1">
      <button :class="['rounded-full px-3 py-1 text-xs', serviceType == null ? 'bg-ink text-white' : 'text-stone-600']" @click="setType(null)">All</button>
      <button v-for="t in SERVICE_TYPES" :key="t.value"
              :class="['rounded-full px-3 py-1 text-xs', serviceType === t.value ? 'bg-ink text-white' : 'text-stone-600']"
              @click="setType(t.value)">{{ t.label }}</button>
    </div>
    <div v-if="servicesLoading" class="space-y-3">
      <div v-for="n in 3" :key="n" class="h-24 animate-pulse rounded-xl bg-stone-100" />
    </div>
    <p v-else-if="!services.length" class="text-sm text-stone-500">No services within 30 km yet.</p>
    <div v-for="s in servicesLoading ? [] : services" :key="s.id" class="mb-3 rounded-xl border border-stone-200 p-4">
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="font-medium">{{ s.title }}</p>
          <p class="text-xs text-stone-500">{{ s.providerName }}</p>
        </div>
        <span class="inline-flex items-center gap-1 rounded-full bg-stone-100 px-2 py-0.5 text-[11px] text-stone-600">
          <i :class="['pi text-[10px]', SERVICE_TYPES.find((t) => t.value === s.type)?.icon]" />
          {{ SERVICE_TYPES.find((t) => t.value === s.type)?.label }}
        </span>
      </div>
      <p v-if="s.description" class="mt-2 text-sm text-stone-600">{{ s.description }}</p>
      <div class="mt-4 flex items-center justify-between">
        <span class="font-medium tabular-nums">{{ som(s.priceKgs) }} <span class="text-xs font-normal text-stone-500">{{ unitLabel(s.priceUnit) }}</span></span>
        <button class="rounded-full bg-ink px-3.5 py-1.5 text-xs font-medium text-white hover:bg-stone-800" @click="book(s)">Request</button>
      </div>
    </div>
  </Drawer>

  <BookingDialog v-model:visible="bookingOpen" :service="bookingService" :date="drawerDay?.date" :plan-id="Number(id)" />
</template>
