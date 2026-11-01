<script setup>
import { computed, onMounted, ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Tag from 'primevue/tag'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Textarea from 'primevue/textarea'
import SelectButton from 'primevue/selectbutton'
import { useToast } from 'primevue/usetoast'
import { bookingApi } from '@/api'
import { errorMessage } from '@/api/http'
import { BOOKING_SEVERITY, date } from '@/utils/format'

const toast = useToast()
const bookings = ref([])
const loading = ref(true)
const filter = ref('PENDING')
const filters = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'All', value: 'ALL' },
]

async function load() {
  loading.value = true
  bookings.value = await bookingApi.incoming()
  loading.value = false
}
onMounted(load)

const shown = computed(() => (filter.value === 'ALL' ? bookings.value : bookings.value.filter((b) => b.status === 'PENDING')))
const pendingCount = computed(() => bookings.value.filter((b) => b.status === 'PENDING').length)

const decision = ref(null)
const comment = ref('')

function decide(booking, confirm) {
  decision.value = { booking, confirm }
  comment.value = ''
}

async function submitDecision() {
  const { booking, confirm } = decision.value
  try {
    await (confirm ? bookingApi.confirm(booking.id, comment.value) : bookingApi.reject(booking.id, comment.value))
    toast.add({ severity: confirm ? 'success' : 'info', summary: confirm ? 'Confirmed' : 'Rejected', life: 3000 })
    decision.value = null
    await load()
  } catch (e) {
    toast.add({ severity: 'error', summary: errorMessage(e), life: 4000 })
  }
}
</script>

<template>
  <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
    <div>
      <h1 class="text-3xl font-medium">Booking requests</h1>
      <p class="text-surface-500">{{ pendingCount }} waiting for your answer</p>
    </div>
    <SelectButton v-model="filter" :options="filters" option-label="label" option-value="value" :allow-empty="false" />
  </div>

  <DataTable :value="shown" :loading="loading" data-key="id" striped-rows class="overflow-hidden rounded-xl border border-stone-200 bg-white"
             paginator :rows="10" :rows-per-page-options="[10, 25]">
    <template #empty>No requests.</template>
    <Column field="serviceTitle" header="Service" />
    <Column header="Guest">
      <template #body="{ data }">
        <p>{{ data.touristName }}</p>
        <p v-if="data.message" class="max-w-xs truncate text-xs text-surface-500" v-tooltip.top="data.message">{{ data.message }}</p>
      </template>
    </Column>
    <Column header="Dates">
      <template #body="{ data }">{{ date(data.dateFrom) }} → {{ date(data.dateTo) }}</template>
    </Column>
    <Column field="guests" header="Guests" />
    <Column header="Status">
      <template #body="{ data }"><Tag :value="data.status" :severity="BOOKING_SEVERITY[data.status]" /></template>
    </Column>
    <Column header="" class="w-48">
      <template #body="{ data }">
        <div v-if="data.status === 'PENDING'" class="flex gap-2">
          <Button label="Confirm" size="small" icon="pi pi-check" @click="decide(data, true)" />
          <Button label="Reject" size="small" severity="secondary" outlined @click="decide(data, false)" />
        </div>
      </template>
    </Column>
  </DataTable>

  <Dialog :visible="!!decision" modal :header="decision?.confirm ? 'Confirm booking' : 'Reject booking'"
          class="w-[min(28rem,95vw)]" @update:visible="(v) => !v && (decision = null)">
    <p class="mb-3 text-sm text-surface-600">
      {{ decision?.booking.touristName }} · {{ decision?.booking.serviceTitle }}
    </p>
    <label class="text-sm font-medium">Message to the guest (optional)</label>
    <Textarea v-model="comment" rows="3" auto-resize fluid class="mt-1.5"
              :placeholder="decision?.confirm ? 'Meeting point, what to bring…' : 'Reason, alternative dates…'" />
    <template #footer>
      <Button label="Back" text @click="decision = null" />
      <Button :label="decision?.confirm ? 'Confirm' : 'Reject'" :severity="decision?.confirm ? 'primary' : 'danger'"
              @click="submitDecision" />
    </template>
  </Dialog>
</template>
