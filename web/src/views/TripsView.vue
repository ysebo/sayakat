<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import { planApi } from '@/api'
import { category, date, duration, som } from '@/utils/format'

const plans = ref(null)
onMounted(async () => (plans.value = await planApi.mine()))

const STATUS = { SOLVING: ['Planning…', 'info'], SOLVED: ['Ready', 'success'], FAILED: ['Failed', 'danger'] }
</script>

<template>
  <div class="mx-auto max-w-5xl px-4 py-12 sm:px-6">
    <div class="flex items-end justify-between gap-4">
      <h1 class="text-4xl font-semibold">My trips</h1>
      <RouterLink :to="{ name: 'planner' }"><Button label="New trip" icon="pi pi-plus" /></RouterLink>
    </div>

    <p v-if="plans && !plans.length" class="mt-10 text-lake-900/60">
      No trips yet. <RouterLink :to="{ name: 'planner' }" class="font-medium text-lake-700">Plan your first one</RouterLink>.
    </p>

    <div class="mt-8 grid gap-5 sm:grid-cols-2">
      <RouterLink
        v-for="(p, i) in plans ?? []"
        :key="p.id"
        v-reveal="(i % 2) * 80"
        :to="{ name: 'trip', params: { id: p.id } }"
        class="group rounded-2xl bg-white p-5 shadow-sm ring-1 ring-lake-100 transition hover:-translate-y-0.5 hover:shadow-md"
      >
        <div class="flex items-center justify-between">
          <Tag :value="STATUS[p.status][0]" :severity="STATUS[p.status][1]" />
          <span class="text-xs text-lake-900/50">{{ p.algorithm === 'TIMEFOLD' ? 'Optimised' : 'Greedy' }}</span>
        </div>
        <h2 class="mt-3 text-2xl font-semibold">{{ p.days }} days from {{ p.start.name }}</h2>
        <p class="text-sm text-lake-900/60">{{ date(p.startDate, { day: 'numeric', month: 'long', year: 'numeric' }) }}</p>
        <div class="mt-3 flex flex-wrap gap-1.5">
          <span v-for="i2 in p.interests" :key="i2" class="rounded-full px-2.5 py-0.5 text-xs text-white"
                :style="{ background: category(i2).color }">{{ category(i2).label }}</span>
        </div>
        <p v-if="p.status === 'SOLVED'" class="mt-4 text-sm text-lake-900/70">
          {{ p.itinerary.reduce((n, d) => n + d.stops.length, 0) }} places · {{ duration(p.totalTravelMin) }} driving ·
          {{ som(p.totalCostKgs) }}
        </p>
      </RouterLink>
    </div>
  </div>
</template>
