<script setup>
import { computed, onMounted, ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import Rating from 'primevue/rating'
import Message from 'primevue/message'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import { poiApi } from '@/api'
import { errorMessage } from '@/api/http'
import { CATEGORIES, category, duration, season, som } from '@/utils/format'

const toast = useToast()
const confirm = useConfirm()
const pois = ref([])
const loading = ref(true)
const search = ref('')

const categoryOptions = Object.entries(CATEGORIES).map(([value, c]) => ({ value, label: c.label }))
const months = Array.from({ length: 12 }, (_, i) => ({
  value: i + 1,
  label: new Date(2026, i, 1).toLocaleString('en', { month: 'long' }),
}))

async function load() {
  loading.value = true
  pois.value = await poiApi.list()
  loading.value = false
}
onMounted(load)

const shown = computed(() => {
  const q = search.value.trim().toLowerCase()
  return q ? pois.value.filter((p) => `${p.name} ${p.region} ${p.category}`.toLowerCase().includes(q)) : pois.value
})

const editing = ref(null)
const error = ref('')

function open(poi) {
  error.value = ''
  editing.value = poi
    ? { ...poi, tagsText: (poi.tags ?? []).join(', ') }
    : { externalId: '', name: '', category: 'NATURE', region: '', lat: 42.0, lon: 75.0, visitDurationMin: 120,
        costKgs: 0, opensAt: null, closesAt: null, seasonFrom: 1, seasonTo: 12, popularity: 3, description: '',
        source: 'manual', tagsText: '' }
}

async function save() {
  error.value = ''
  const { id, tagsText, ...rest } = editing.value
  const body = { ...rest, tags: tagsText.split(',').map((t) => t.trim()).filter(Boolean), opensAt: rest.opensAt || null, closesAt: rest.closesAt || null }
  try {
    if (id) await poiApi.update(id, body)
    else await poiApi.create(body)
    toast.add({ severity: 'success', summary: 'Saved — rebuild the travel matrix to use new coordinates', life: 4000 })
    editing.value = null
    await load()
  } catch (e) {
    error.value = errorMessage(e)
  }
}

function remove(poi) {
  confirm.require({
    message: `Delete ${poi.name}?`,
    header: 'Delete attraction',
    acceptProps: { label: 'Delete', severity: 'danger' },
    rejectProps: { label: 'Cancel', text: true },
    accept: async () => {
      try {
        await poiApi.remove(poi.id)
        await load()
      } catch (e) {
        toast.add({ severity: 'error', summary: 'Cannot delete', detail: 'It is used in saved trips', life: 4000 })
      }
    },
  })
}
</script>

<template>
  <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-semibold">Attractions</h1>
      <p class="text-surface-500">{{ pois.length }} places in the dataset</p>
    </div>
    <div class="flex gap-2">
      <IconField>
        <InputIcon class="pi pi-search" />
        <InputText v-model="search" placeholder="Search" />
      </IconField>
      <Button label="Add" icon="pi pi-plus" @click="open(null)" />
    </div>
  </div>

  <DataTable :value="shown" :loading="loading" data-key="id" class="rounded-xl bg-white shadow-sm" paginator :rows="15"
             sort-field="name" :sort-order="1" size="small">
    <Column field="name" header="Name" sortable />
    <Column field="category" header="Category" sortable>
      <template #body="{ data }">
        <span class="flex items-center gap-2"><i :class="['pi text-xs', category(data.category).icon]"
              :style="{ color: category(data.category).color }" />{{ category(data.category).label }}</span>
      </template>
    </Column>
    <Column field="region" header="Region" sortable />
    <Column header="Season"><template #body="{ data }">{{ season(data.seasonFrom, data.seasonTo) }}</template></Column>
    <Column header="Visit"><template #body="{ data }">{{ duration(data.visitDurationMin) }}</template></Column>
    <Column header="Cost" sortable sort-field="costKgs"><template #body="{ data }">{{ data.costKgs ? som(data.costKgs) : 'Free' }}</template></Column>
    <Column field="popularity" header="★" sortable />
    <Column class="w-24">
      <template #body="{ data }">
        <Button icon="pi pi-pencil" text rounded size="small" aria-label="Edit" @click="open(data)" />
        <Button icon="pi pi-trash" text rounded size="small" severity="danger" aria-label="Delete" @click="remove(data)" />
      </template>
    </Column>
  </DataTable>

  <Dialog :visible="!!editing" modal :header="editing?.id ? 'Edit attraction' : 'New attraction'" class="w-[min(48rem,95vw)]"
          @update:visible="(v) => !v && (editing = null)">
    <div v-if="editing" class="grid gap-4 md:grid-cols-2">
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Name</label><InputText v-model="editing.name" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">External id</label><InputText v-model="editing.externalId" /></div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Category</label>
        <Select v-model="editing.category" :options="categoryOptions" option-label="label" option-value="value" />
      </div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Region</label><InputText v-model="editing.region" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Latitude</label><InputNumber v-model="editing.lat" :min-fraction-digits="4" :max-fraction-digits="6" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Longitude</label><InputNumber v-model="editing.lon" :min-fraction-digits="4" :max-fraction-digits="6" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Visit duration, min</label><InputNumber v-model="editing.visitDurationMin" :min="10" :step="15" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Entrance, KGS</label><InputNumber v-model="editing.costKgs" :min="0" :step="50" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Opens (HH:mm, empty = always)</label><InputText v-model="editing.opensAt" placeholder="09:00" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Closes</label><InputText v-model="editing.closesAt" placeholder="18:00" /></div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Season from</label>
        <Select v-model="editing.seasonFrom" :options="months" option-label="label" option-value="value" />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Season to</label>
        <Select v-model="editing.seasonTo" :options="months" option-label="label" option-value="value" />
      </div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Popularity</label><Rating v-model="editing.popularity" :cancel="false" /></div>
      <div class="flex flex-col gap-1.5"><label class="text-sm font-medium">Tags (comma separated)</label><InputText v-model="editing.tagsText" /></div>
      <div class="flex flex-col gap-1.5 md:col-span-2"><label class="text-sm font-medium">Description</label><Textarea v-model="editing.description" rows="3" auto-resize /></div>
      <Message v-if="error" severity="error" size="small" class="md:col-span-2">{{ error }}</Message>
    </div>
    <template #footer>
      <Button label="Cancel" text @click="editing = null" />
      <Button label="Save" icon="pi pi-check" @click="save" />
    </template>
  </Dialog>
</template>
