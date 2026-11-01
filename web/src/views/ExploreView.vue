<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import LeafletMap from '@/components/LeafletMap.vue'
import { poiApi } from '@/api'
import { CATEGORIES, category, duration, escapeHtml, satelliteTile, season, som } from '@/utils/format'

const route = useRoute()
const pois = ref([])
const selected = ref(new Set())
const query = ref('')
const focus = ref(null)
const activeId = ref(null)

onMounted(async () => {
  pois.value = await poiApi.list()
  const target = pois.value.find((p) => String(p.id) === route.query.poi)
  if (target) show(target)
})

const visible = computed(() => {
  const q = query.value.trim().toLowerCase()
  return pois.value.filter(
    (p) =>
      (selected.value.size === 0 || selected.value.has(p.category)) &&
      (!q || p.name.toLowerCase().includes(q) || (p.region ?? '').toLowerCase().includes(q)),
  )
})

const markers = computed(() =>
  visible.value.map((p) => ({
    id: p.id,
    lat: p.lat,
    lon: p.lon,
    color: category(p.category).color,
    icon: category(p.category).icon,
    title: p.name,
    popup: `<strong>${escapeHtml(p.name)}</strong><br><span style="color:#78716c">${escapeHtml(category(p.category).label)} · ${escapeHtml(season(p.seasonFrom, p.seasonTo))}</span>`,
  })),
)

function toggle(key) {
  const next = new Set(selected.value)
  next.has(key) ? next.delete(key) : next.add(key)
  selected.value = next
}

function show(poi) {
  activeId.value = poi.id
  focus.value = { lat: poi.lat, lon: poi.lon, zoom: 11 }
}

function onMarker(id) {
  activeId.value = id
  document.getElementById(`poi-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
}
</script>

<template>
  <div class="flex h-[calc(100svh-64px)] flex-col lg:flex-row">
    <aside class="flex max-h-[48svh] flex-col border-stone-200 bg-white lg:max-h-none lg:w-[420px] lg:border-r">
      <div class="space-y-4 border-b border-stone-200 p-5">
        <div class="flex items-baseline justify-between">
          <h1 class="text-2xl font-medium">Explore</h1>
          <span class="text-sm text-stone-500 tabular-nums">{{ visible.length }} places</span>
        </div>
        <label class="flex items-center gap-2 rounded-xl border border-stone-200 bg-stone-50 px-3 py-2.5 focus-within:border-stone-400">
          <i class="pi pi-search text-sm text-stone-400" />
          <input v-model="query" placeholder="Search places or regions" class="w-full bg-transparent text-sm outline-none" />
        </label>
        <div class="-mx-1 flex gap-1.5 overflow-x-auto px-1 pb-1">
          <button
            v-for="(c, key) in CATEGORIES"
            :key="key"
            :class="[
              'flex shrink-0 items-center gap-1.5 rounded-full border px-3 py-1 text-xs transition',
              selected.has(key) ? 'border-ink bg-ink text-white' : 'border-stone-200 text-stone-600 hover:border-stone-400',
            ]"
            @click="toggle(key)"
          >
            <span class="h-1.5 w-1.5 rounded-full" :style="{ background: c.color }" /> {{ c.label }}
          </button>
        </div>
      </div>
      <ul class="flex-1 overflow-y-auto">
        <li
          v-for="p in visible"
          :id="`poi-${p.id}`"
          :key="p.id"
          :class="['flex cursor-pointer gap-3 border-b border-stone-100 px-5 py-3.5 transition hover:bg-stone-50',
                   activeId === p.id && 'bg-stone-50']"
          @click="show(p)"
        >
          <div class="h-16 w-16 shrink-0 overflow-hidden rounded-xl bg-stone-200">
            <img :src="satelliteTile(p.lat, p.lon, 13)" alt="" loading="lazy" class="h-full w-full scale-150 object-cover" />
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-1.5 text-xs text-stone-500">
              <span class="h-1.5 w-1.5 rounded-full" :style="{ background: category(p.category).color }" />
              {{ category(p.category).label }} · {{ p.region }}
            </div>
            <p class="mt-0.5 truncate font-medium">{{ p.name }}</p>
            <p class="text-xs text-stone-500">
              {{ season(p.seasonFrom, p.seasonTo) }} · {{ duration(p.visitDurationMin) }}{{ p.costKgs ? ` · ${som(p.costKgs)}` : '' }}
            </p>
            <p v-if="activeId === p.id" class="mt-2 text-sm leading-relaxed text-stone-600">{{ p.description }}</p>
          </div>
        </li>
      </ul>
    </aside>
    <div class="relative min-h-[300px] flex-1">
      <LeafletMap :markers="markers" :focus="focus" :fit-to-content="false" @marker-click="onMarker" />
    </div>
  </div>
</template>
