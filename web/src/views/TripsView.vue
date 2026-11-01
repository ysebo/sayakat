<script setup>
import { onMounted, ref } from 'vue'
import { planApi } from '@/api'
import { DAY_COLORS, category, date, duration, som } from '@/utils/format'

const plans = ref(null)
onMounted(async () => (plans.value = await planApi.mine()))

const STATUS = {
  SOLVING: ['Planning', 'bg-sky-50 text-sky-700'],
  SOLVED: ['Ready', 'bg-teal-50 text-teal-700'],
  FAILED: ['Failed', 'bg-rose-50 text-rose-700'],
}
const places = (p) => p.itinerary.reduce((n, d) => n + d.stops.length, 0)
</script>

<template>
  <div class="mx-auto max-w-6xl px-4 py-12 sm:px-6 lg:py-16">
    <div class="flex items-end justify-between gap-4">
      <div>
        <h1 class="text-4xl font-medium">My trips</h1>
        <p class="mt-2 text-stone-500">Every route you have planned.</p>
      </div>
      <RouterLink :to="{ name: 'planner' }" class="rounded-full bg-ink px-4 py-2 text-sm font-medium text-white hover:bg-stone-800">
        <i class="pi pi-plus mr-1.5 text-xs" /> New trip
      </RouterLink>
    </div>

    <div v-if="plans && !plans.length" class="mt-12 rounded-2xl border border-dashed border-stone-300 p-12 text-center">
      <p class="text-stone-500">No trips yet.</p>
      <RouterLink :to="{ name: 'planner' }" class="mt-3 inline-block font-medium underline underline-offset-4">Plan your first one</RouterLink>
    </div>

    <div v-if="!plans" class="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="n in 3" :key="n" class="h-48 animate-pulse rounded-2xl bg-stone-100" />
    </div>

    <div class="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      <RouterLink
        v-for="(p, i) in plans ?? []"
        :key="p.id"
        v-reveal="(i % 3) * 70"
        :to="{ name: 'trip', params: { id: p.id } }"
        class="group flex flex-col rounded-2xl border border-stone-200 bg-white p-5 transition hover:border-stone-300 hover:shadow-lg hover:shadow-stone-200/60"
      >
        <div class="flex items-center justify-between">
          <span :class="['rounded-full px-2.5 py-1 text-xs font-medium', STATUS[p.status][1]]">{{ STATUS[p.status][0] }}</span>
          <span class="text-xs text-stone-400">{{ p.algorithm === 'TIMEFOLD' ? 'Optimised' : 'Greedy' }}</span>
        </div>
        <h2 class="mt-5 text-xl font-medium">{{ p.days }} days from {{ p.start.name }}</h2>
        <p class="text-sm text-stone-500">{{ date(p.startDate, { day: 'numeric', month: 'long', year: 'numeric' }) }}</p>
        <div class="mt-4 flex gap-1">
          <span v-for="d in p.itinerary" :key="d.dayIndex" class="h-1 flex-1 rounded-full"
                :style="{ background: DAY_COLORS[d.dayIndex % DAY_COLORS.length], opacity: d.stops.length ? 1 : 0.25 }" />
        </div>
        <div class="mt-4 flex flex-wrap gap-1.5">
          <span v-for="k in p.interests" :key="k" class="inline-flex items-center gap-1 rounded-full bg-stone-100 px-2 py-0.5 text-xs text-stone-600">
            <span class="h-1.5 w-1.5 rounded-full" :style="{ background: category(k).color }" /> {{ category(k).label }}
          </span>
        </div>
        <p v-if="p.status === 'SOLVED'" class="mt-auto pt-5 text-sm text-stone-500 tabular-nums">
          {{ places(p) }} places · {{ duration(p.totalTravelMin) }} driving · {{ som(p.totalCostKgs) }}
        </p>
      </RouterLink>
    </div>
  </div>
</template>
