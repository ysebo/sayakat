<script setup>
import { computed, onMounted, ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import ToggleSwitch from 'primevue/toggleswitch'
import Message from 'primevue/message'
import { useToast } from 'primevue/usetoast'
import LeafletMap from '@/components/LeafletMap.vue'
import { serviceApi } from '@/api'
import { errorMessage } from '@/api/http'
import { PRICE_UNITS, SERVICE_TYPES, som, unitLabel } from '@/utils/format'

const toast = useToast()
const services = ref([])
const loading = ref(true)

async function load() {
  loading.value = true
  services.value = await serviceApi.mine()
  loading.value = false
}
onMounted(load)

const empty = () => ({
  type: 'ACCOMMODATION', title: '', description: '', priceKgs: 2000, priceUnit: 'PER_NIGHT',
  lat: null, lon: null, capacity: 10, active: true,
})
const editing = ref(null)
const error = ref('')
const saving = ref(false)

const pin = computed(() =>
  editing.value?.lat != null ? [{ id: 'pin', lat: editing.value.lat, lon: editing.value.lon, color: '#b8432f', icon: 'pi-home' }] : [],
)

function open(service) {
  error.value = ''
  editing.value = service ? { ...service } : empty()
}

function setLocation({ lat, lon }) {
  editing.value.lat = Number(lat.toFixed(5))
  editing.value.lon = Number(lon.toFixed(5))
}

async function save() {
  saving.value = true
  error.value = ''
  const { id, ...body } = editing.value
  try {
    if (id) await serviceApi.update(id, body)
    else await serviceApi.create(body)
    toast.add({ severity: 'success', summary: 'Saved', life: 2500 })
    editing.value = null
    await load()
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-semibold">My services</h1>
      <p class="text-surface-500">Accommodation with a location is offered to the route planner as a place to sleep.</p>
    </div>
    <Button label="Add service" icon="pi pi-plus" @click="open(null)" />
  </div>

  <DataTable :value="services" :loading="loading" data-key="id" class="rounded-xl bg-white shadow-sm" paginator :rows="10">
    <template #empty>No services yet.</template>
    <Column field="title" header="Title" sortable />
    <Column header="Type">
      <template #body="{ data }">
        <span class="flex items-center gap-2">
          <i :class="['pi', SERVICE_TYPES.find((t) => t.value === data.type)?.icon]" />
          {{ SERVICE_TYPES.find((t) => t.value === data.type)?.label }}
        </span>
      </template>
    </Column>
    <Column header="Price" sortable sort-field="priceKgs">
      <template #body="{ data }">{{ som(data.priceKgs) }} <span class="text-xs text-surface-500">{{ unitLabel(data.priceUnit) }}</span></template>
    </Column>
    <Column field="capacity" header="Capacity" />
    <Column header="Status">
      <template #body="{ data }"><Tag :value="data.active ? 'Active' : 'Hidden'" :severity="data.active ? 'success' : 'secondary'" /></template>
    </Column>
    <Column class="w-16">
      <template #body="{ data }"><Button icon="pi pi-pencil" text rounded aria-label="Edit" @click="open(data)" /></template>
    </Column>
  </DataTable>

  <Dialog :visible="!!editing" modal :header="editing?.id ? 'Edit service' : 'New service'" class="w-[min(48rem,95vw)]"
          @update:visible="(v) => !v && (editing = null)">
    <div v-if="editing" class="grid gap-4 md:grid-cols-2">
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Type</label>
        <Select v-model="editing.type" :options="SERVICE_TYPES" option-label="label" option-value="value" />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Title</label>
        <InputText v-model="editing.title" />
      </div>
      <div class="flex flex-col gap-1.5 md:col-span-2">
        <label class="text-sm font-medium">Description</label>
        <Textarea v-model="editing.description" rows="2" auto-resize />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Price, KGS</label>
        <InputNumber v-model="editing.priceKgs" :min="0" :step="100" />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Per</label>
        <Select v-model="editing.priceUnit" :options="PRICE_UNITS" option-label="label" option-value="value" />
      </div>
      <div class="flex flex-col gap-1.5">
        <label class="text-sm font-medium">Capacity</label>
        <InputNumber v-model="editing.capacity" :min="1" />
      </div>
      <div class="flex items-center gap-3 pt-6">
        <ToggleSwitch v-model="editing.active" input-id="active" />
        <label for="active" class="text-sm">Visible to tourists</label>
      </div>
      <div class="md:col-span-2">
        <label class="text-sm font-medium">
          Location <span class="font-normal text-surface-500">— click on the map
            <template v-if="editing.lat != null">({{ editing.lat }}, {{ editing.lon }})</template></span>
        </label>
        <div class="mt-1.5 h-64 overflow-hidden rounded-lg ring-1 ring-surface-200">
          <LeafletMap :markers="pin" :fit-to-content="false"
                      :center="editing.lat != null ? [editing.lat, editing.lon] : [41.6, 74.6]"
                      :zoom="editing.lat != null ? 10 : 6" @map-click="setLocation" />
        </div>
      </div>
      <Message v-if="error" severity="error" size="small" class="md:col-span-2">{{ error }}</Message>
    </div>
    <template #footer>
      <Button label="Cancel" text @click="editing = null" />
      <Button label="Save" icon="pi pi-check" :loading="saving" @click="save" />
    </template>
  </Dialog>
</template>
