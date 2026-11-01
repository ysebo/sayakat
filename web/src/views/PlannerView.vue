<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DatePicker from 'primevue/datepicker'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import Slider from 'primevue/slider'
import Message from 'primevue/message'
import { poiApi, planApi } from '@/api'
import { errorMessage } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { INTEREST_OPTIONS, PACES, date, som, toIso } from '@/utils/format'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const cities = ref([])
const inAMonth = new Date()
inAMonth.setDate(inAMonth.getDate() + 30)

const form = ref({
  startPoiId: null,
  startDate: inAMonth,
  days: Number(route.query.days) || 5,
  budgetKgs: 60000,
  pace: 'MODERATE',
  interests: route.query.interests ? String(route.query.interests).split(',').filter(Boolean) : ['LAKE', 'NATURE'],
  algorithm: 'TIMEFOLD',
})
const error = ref('')
const loading = ref(false)

onMounted(async () => {
  cities.value = await poiApi.list({ category: 'CITY' })
  form.value.startPoiId = cities.value.find((c) => c.externalId === 'bishkek')?.id ?? cities.value[0]?.id
})

const canSubmit = computed(() => auth.role === 'TOURIST')
const perDay = computed(() => Math.round(form.value.budgetKgs / Math.max(1, form.value.days)))
const endDate = computed(() => {
  const d = new Date(form.value.startDate)
  d.setDate(d.getDate() + form.value.days - 1)
  return toIso(d)
})
const startCity = computed(() => cities.value.find((c) => c.id === form.value.startPoiId)?.name ?? '—')
const paceLabel = computed(() => PACES.find((p) => p.value === form.value.pace)?.label)

function toggleInterest(value) {
  const list = form.value.interests
  form.value.interests = list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
}

async function submit() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  error.value = ''
  loading.value = true
  try {
    const plan = await planApi.create({ ...form.value, startDate: toIso(form.value.startDate) })
    router.push({ name: 'trip', params: { id: plan.id } })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:py-16">
    <div class="max-w-2xl">
      <p class="text-sm font-medium text-teal-700">Trip planner</p>
      <h1 class="mt-2 text-balance text-4xl font-medium sm:text-5xl">
        Where will your <span class="font-serif italic">саякат</span> take you?
      </h1>
      <p class="mt-4 text-stone-500">
        Set the basics. The solver picks places, orders them day by day and finds a place to sleep every night.
      </p>
    </div>

    <div class="mt-12 grid gap-8 lg:grid-cols-[1fr_360px]">
      <form class="space-y-10" @submit.prevent="submit">
        <section>
          <h2 class="text-sm font-medium text-stone-500">When &amp; where</h2>
          <div class="mt-4 grid gap-4 sm:grid-cols-3">
            <div class="flex flex-col gap-1.5">
              <label class="text-sm">Start &amp; finish</label>
              <Select v-model="form.startPoiId" :options="cities" option-label="name" option-value="id" />
            </div>
            <div class="flex flex-col gap-1.5">
              <label class="text-sm">First day</label>
              <DatePicker v-model="form.startDate" date-format="d M yy" :min-date="new Date()" show-icon fluid />
            </div>
            <div class="flex flex-col gap-1.5">
              <label class="text-sm">Days</label>
              <InputNumber v-model="form.days" :min="1" :max="21" show-buttons button-layout="horizontal" fluid />
            </div>
          </div>
        </section>

        <section>
          <div class="flex items-baseline justify-between">
            <h2 class="text-sm font-medium text-stone-500">Budget per person</h2>
            <span class="text-2xl font-medium tabular-nums">{{ som(form.budgetKgs) }}</span>
          </div>
          <Slider v-model="form.budgetKgs" :min="10000" :max="300000" :step="5000" class="mt-5" />
          <p class="mt-3 text-sm text-stone-500">≈ {{ som(perDay) }} a day for entrance fees, lodging and transport</p>
        </section>

        <section>
          <h2 class="text-sm font-medium text-stone-500">Pace</h2>
          <div class="mt-4 grid gap-3 sm:grid-cols-3">
            <button
              v-for="p in PACES"
              :key="p.value"
              type="button"
              :class="[
                'rounded-xl border p-4 text-left transition',
                form.pace === p.value ? 'border-ink bg-white shadow-sm ring-1 ring-ink' : 'border-stone-200 bg-white hover:border-stone-400',
              ]"
              @click="form.pace = p.value"
            >
              <p class="font-medium">{{ p.label }}</p>
              <p class="mt-1 text-sm text-stone-500">{{ p.hint }}</p>
              <p class="font-mono text-xs text-stone-400">{{ p.hours }}</p>
            </button>
          </div>
        </section>

        <section>
          <h2 class="text-sm font-medium text-stone-500">Interests</h2>
          <div class="mt-4 flex flex-wrap gap-2">
            <button
              v-for="i in INTEREST_OPTIONS"
              :key="i.value"
              type="button"
              :class="[
                'flex items-center gap-2 rounded-full border px-4 py-2 text-sm transition',
                form.interests.includes(i.value) ? 'border-ink bg-ink text-white' : 'border-stone-200 bg-white text-stone-700 hover:border-stone-400',
              ]"
              @click="toggleInterest(i.value)"
            >
              <span class="h-1.5 w-1.5 rounded-full" :style="{ background: i.color }" /> {{ i.label }}
            </button>
          </div>
          <p class="mt-3 text-sm text-stone-500">Famous places still count outside your interests — just with a lower weight.</p>
        </section>

        <section>
          <h2 class="text-sm font-medium text-stone-500">Engine</h2>
          <div class="mt-4 inline-flex rounded-full border border-stone-200 bg-white p-1">
            <button
              v-for="a in [{ v: 'TIMEFOLD', l: 'Optimised · Timefold' }, { v: 'GREEDY', l: 'Instant · Greedy' }]"
              :key="a.v"
              type="button"
              :class="['rounded-full px-4 py-1.5 text-sm transition', form.algorithm === a.v ? 'bg-ink text-white' : 'text-stone-600']"
              @click="form.algorithm = a.v"
            >
              {{ a.l }}
            </button>
          </div>
        </section>
      </form>

      <aside class="lg:sticky lg:top-24 lg:self-start">
        <div class="rounded-2xl border border-stone-200 bg-white p-6 shadow-sm">
          <p class="text-sm text-stone-500">Your trip</p>
          <p class="mt-1 text-2xl font-medium">{{ form.days }} days from {{ startCity }}</p>
          <dl class="mt-6 space-y-3 text-sm">
            <div class="flex justify-between"><dt class="text-stone-500">Dates</dt><dd>{{ date(toIso(form.startDate), { day: 'numeric', month: 'short' }) }} – {{ date(endDate, { day: 'numeric', month: 'short' }) }}</dd></div>
            <div class="flex justify-between"><dt class="text-stone-500">Budget</dt><dd class="tabular-nums">{{ som(form.budgetKgs) }}</dd></div>
            <div class="flex justify-between"><dt class="text-stone-500">Pace</dt><dd>{{ paceLabel }}</dd></div>
            <div class="flex justify-between gap-4"><dt class="text-stone-500">Interests</dt><dd class="text-right">{{ form.interests.length || 'Any' }}</dd></div>
          </dl>
          <Message v-if="auth.isAuthenticated && !canSubmit" severity="info" size="small" class="mt-5">
            Trip planning is available for traveller accounts.
          </Message>
          <Message v-if="error" severity="error" size="small" class="mt-5">{{ error }}</Message>
          <button
            :disabled="loading || (auth.isAuthenticated && !canSubmit)"
            class="mt-6 flex w-full items-center justify-center gap-2 rounded-xl bg-ink py-3.5 font-medium text-white transition hover:bg-stone-800 disabled:opacity-50"
            @click="submit"
          >
            <i :class="['pi', loading ? 'pi-spin pi-spinner' : 'pi-compass']" />
            {{ auth.isAuthenticated ? 'Build my route' : 'Sign in to build my route' }}
          </button>
        </div>
      </aside>
    </div>
  </div>
</template>
