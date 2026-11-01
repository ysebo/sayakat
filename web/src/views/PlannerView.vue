<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DatePicker from 'primevue/datepicker'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import SelectButton from 'primevue/selectbutton'
import Button from 'primevue/button'
import Message from 'primevue/message'
import { poiApi, planApi } from '@/api'
import { errorMessage } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { INTEREST_OPTIONS, PACES, som, toIso } from '@/utils/format'

const auth = useAuthStore()
const router = useRouter()

const cities = ref([])
const nextMonth = new Date()
nextMonth.setDate(nextMonth.getDate() + 30)

const form = ref({
  startPoiId: null,
  startDate: nextMonth,
  days: 5,
  budgetKgs: 60000,
  pace: 'MODERATE',
  interests: ['LAKE', 'NATURE'],
  algorithm: 'TIMEFOLD',
})
const algorithms = [
  { label: 'Optimised (Timefold)', value: 'TIMEFOLD' },
  { label: 'Quick (greedy)', value: 'GREEDY' },
]
const error = ref('')
const loading = ref(false)

onMounted(async () => {
  cities.value = await poiApi.list({ category: 'CITY' })
  form.value.startPoiId = cities.value.find((c) => c.externalId === 'bishkek')?.id ?? cities.value[0]?.id
})

const canSubmit = computed(() => auth.role === 'TOURIST')
const perDay = computed(() => Math.round(form.value.budgetKgs / Math.max(1, form.value.days)))

function toggleInterest(value) {
  const list = form.value.interests
  form.value.interests = list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
}

async function submit() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: '/plan' } })
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
  <div class="bg-gradient-to-b from-lake-50 to-snow">
    <div class="mx-auto max-w-4xl px-4 py-12 sm:px-6">
      <p class="text-sm font-medium uppercase tracking-[0.2em] text-ochre-700">Trip planner</p>
      <h1 class="mt-2 text-4xl font-semibold sm:text-5xl">Where will your саякат take you?</h1>
      <p class="mt-3 max-w-2xl text-lake-900/70">
        Tell us the basics. The planner chooses places, orders them day by day, respects opening hours, seasons and
        mountain roads, and finds a place to sleep every night.
      </p>

      <form class="mt-10 space-y-8 rounded-3xl bg-white p-6 shadow-sm ring-1 ring-lake-100 sm:p-8" @submit.prevent="submit">
        <div class="grid gap-6 sm:grid-cols-3">
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium">Start &amp; finish in</label>
            <Select v-model="form.startPoiId" :options="cities" option-label="name" option-value="id" />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium">First day</label>
            <DatePicker v-model="form.startDate" date-format="d M yy" :min-date="new Date()" show-icon fluid />
          </div>
          <div class="flex flex-col gap-1.5">
            <label class="text-sm font-medium">Days</label>
            <InputNumber v-model="form.days" :min="1" :max="21" show-buttons button-layout="horizontal" fluid />
          </div>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium">Budget per person</label>
          <InputNumber v-model="form.budgetKgs" :min="5000" :step="5000" suffix=" KGS" fluid />
          <p class="text-xs text-lake-900/60">
            ≈ {{ som(perDay) }} per day — covers entrance fees, lodging and transport
          </p>
        </div>

        <div>
          <label class="text-sm font-medium">Pace</label>
          <div class="mt-2 grid gap-3 sm:grid-cols-3">
            <button
              v-for="p in PACES"
              :key="p.value"
              type="button"
              :class="[
                'rounded-2xl border-2 p-4 text-left transition',
                form.pace === p.value ? 'border-lake-700 bg-lake-50' : 'border-lake-100 hover:border-lake-300',
              ]"
              @click="form.pace = p.value"
            >
              <p class="font-display text-lg font-semibold">{{ p.label }}</p>
              <p class="text-xs text-lake-900/60">{{ p.hint }}</p>
            </button>
          </div>
        </div>

        <div>
          <label class="text-sm font-medium">What do you love?</label>
          <div class="mt-2 flex flex-wrap gap-2">
            <button
              v-for="i in INTEREST_OPTIONS"
              :key="i.value"
              type="button"
              :class="[
                'flex items-center gap-2 rounded-full border px-4 py-2 text-sm font-medium transition',
                form.interests.includes(i.value) ? 'border-transparent text-white shadow' : 'border-lake-100 bg-white',
              ]"
              :style="form.interests.includes(i.value) ? { background: i.color } : {}"
              @click="toggleInterest(i.value)"
            >
              <i :class="['pi text-xs', i.icon]" /> {{ i.label }}
            </button>
          </div>
          <p class="mt-2 text-xs text-lake-900/60">Famous places still count even outside your interests — just less.</p>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium">Planning engine</label>
          <SelectButton v-model="form.algorithm" :options="algorithms" option-label="label" option-value="value"
                        :allow-empty="false" />
        </div>

        <Message v-if="auth.isAuthenticated && !canSubmit" severity="info" size="small">
          Trip planning is available for traveller accounts.
        </Message>
        <Message v-if="error" severity="error" size="small">{{ error }}</Message>

        <Button
          type="submit"
          :label="auth.isAuthenticated ? 'Build my route' : 'Sign in to build my route'"
          icon="pi pi-compass"
          size="large"
          :loading="loading"
          :disabled="auth.isAuthenticated && !canSubmit"
          class="w-full !border-ochre-500 !bg-ochre-500 !text-lake-950 sm:w-auto"
        />
      </form>
    </div>
  </div>
</template>
