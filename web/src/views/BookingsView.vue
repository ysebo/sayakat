<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import { bookingApi } from '@/api'
import { errorMessage } from '@/api/http'
import { BOOKING_SEVERITY, SERVICE_TYPES, date } from '@/utils/format'

const bookings = ref(null)
const confirm = useConfirm()
const toast = useToast()

const load = async () => (bookings.value = await bookingApi.mine())
onMounted(load)

function cancel(b) {
  confirm.require({
    message: `Cancel your request for ${b.serviceTitle}?`,
    header: 'Cancel booking',
    acceptProps: { label: 'Cancel booking', severity: 'danger' },
    rejectProps: { label: 'Keep', text: true },
    accept: async () => {
      try {
        await bookingApi.cancel(b.id)
        await load()
      } catch (e) {
        toast.add({ severity: 'error', summary: errorMessage(e), life: 4000 })
      }
    },
  })
}
</script>

<template>
  <div class="mx-auto max-w-4xl px-4 py-12 sm:px-6">
    <h1 class="text-4xl font-semibold">My bookings</h1>
    <p class="mt-2 text-lake-900/60">Requests you sent to hosts, guides and drivers. Payment is arranged directly with them.</p>

    <p v-if="bookings && !bookings.length" class="mt-10 text-lake-900/60">
      No requests yet — open a trip and choose “Hosts &amp; guides nearby”.
    </p>

    <div class="mt-8 space-y-3">
      <div v-for="b in bookings ?? []" :key="b.id"
           class="flex flex-col gap-3 rounded-2xl bg-white p-5 ring-1 ring-lake-100 sm:flex-row sm:items-center">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-lake-50 text-lake-700">
          <i :class="['pi', SERVICE_TYPES.find((t) => t.value === b.serviceType)?.icon]" />
        </div>
        <div class="min-w-0 flex-1">
          <p class="font-medium">{{ b.serviceTitle }}</p>
          <p class="text-sm text-lake-900/60">
            {{ b.providerName }} · {{ date(b.dateFrom) }} → {{ date(b.dateTo) }} · {{ b.guests }} guest(s)
          </p>
          <p v-if="b.providerComment" class="mt-1 text-sm italic text-lake-900/70">“{{ b.providerComment }}”</p>
        </div>
        <div class="flex items-center gap-2">
          <RouterLink v-if="b.planId" :to="{ name: 'trip', params: { id: b.planId } }">
            <Button icon="pi pi-map" text rounded v-tooltip.top="'Open trip'" aria-label="Open trip" />
          </RouterLink>
          <Tag :value="b.status" :severity="BOOKING_SEVERITY[b.status]" />
          <Button v-if="b.status === 'PENDING' || b.status === 'CONFIRMED'" label="Cancel" size="small" text
                  severity="danger" @click="cancel(b)" />
        </div>
      </div>
    </div>
  </div>
</template>
