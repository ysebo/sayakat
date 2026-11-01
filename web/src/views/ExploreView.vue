<script setup>
import { computed, onMounted, ref } from 'vue'
import InputText from 'primevue/inputtext'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import LeafletMap from '@/components/LeafletMap.vue'
import { poiApi } from '@/api'
import { CATEGORIES, category, duration, escapeHtml, season, som } from '@/utils/format'

const pois = ref([])
const selected = ref(new Set())
const query = ref('')
const focus = ref(null)
const activeId = ref(null)

onMounted(async () => {
  pois.value = await poiApi.list()
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
    popup: `<strong>${escapeHtml(p.name)}</strong><br>${escapeHtml(category(p.category).label)} · ${escapeHtml(season(p.seasonFrom, p.seasonTo))}`,
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
  <div class="flex h-[calc(100vh-73px)] flex-col lg:flex-row">
    <aside class="flex max-h-[45vh] flex-col border-lake-100 bg-snow lg:max-h-none lg:w-[400px] lg:border-r">
      <div class="space-y-3 border-b border-lake-100 p-4">
        <h1 class="text-2xl font-semibold">Explore Kyrgyzstan</h1>
        <IconField>
          <InputIcon class="pi pi-search" />
          <InputText v-model="query" placeholder="Search places or regions" class="w-full" />
        </IconField>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="(c, key) in CATEGORIES"
            :key="key"
            :class="[
              'flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-medium transition',
              selected.has(key) ? 'border-transparent text-white' : 'border-lake-100 bg-white text-lake-900',
            ]"
            :style="selected.has(key) ? { background: c.color } : {}"
            @click="toggle(key)"
          >
            <i :class="['pi text-[10px]', c.icon]" /> {{ c.label }}
          </button>
        </div>
        <p class="text-xs text-lake-900/60">{{ visible.length }} places</p>
      </div>
      <ul class="flex-1 overflow-y-auto">
        <li
          v-for="p in visible"
          :id="`poi-${p.id}`"
          :key="p.id"
          :class="['cursor-pointer border-b border-lake-100 px-4 py-3 transition hover:bg-white', activeId === p.id && 'bg-white']"
          @click="show(p)"
        >
          <div class="flex items-start gap-3">
            <span class="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-white"
                  :style="{ background: category(p.category).color }">
              <i :class="['pi text-xs', category(p.category).icon]" />
            </span>
            <div class="min-w-0">
              <p class="font-medium">{{ p.name }}</p>
              <p class="text-xs text-lake-900/60">
                {{ p.region }} · {{ season(p.seasonFrom, p.seasonTo) }} · {{ duration(p.visitDurationMin) }}
                <template v-if="p.costKgs"> · {{ som(p.costKgs) }}</template>
              </p>
              <p v-if="activeId === p.id" class="mt-2 text-sm text-lake-900/80">{{ p.description }}</p>
            </div>
          </div>
        </li>
      </ul>
    </aside>
    <div class="relative min-h-[300px] flex-1">
      <LeafletMap :markers="markers" :focus="focus" :fit-to-content="false" @marker-click="onMarker" />
    </div>
  </div>
</template>
