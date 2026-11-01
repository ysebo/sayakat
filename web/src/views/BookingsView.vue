<script setup>
import { onMounted, ref } from 'vue'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import { bookingApi } from '@/api'
import { errorMessage } from '@/api/http'
import { SERVICE_TYPES, date } from '@/utils/format'

const bookings = ref(null)
const confirm = useConfirm()
const toast = useToast()

const STATUS = {
  PENDING: 'bg-amber-50 text-amber-700',
  CONFIRMED: 'bg-teal-50 text-teal-700',
  REJECTED: 'bg-rose-50 text-rose-700',
  CANCELLED: 'bg-stone-100 text-stone-500',
}

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
  <div class="mx-auto max-w-4xl px-4 py-12 sm:px-6 lg:py-16">
    <h1 class="text-4xl font-medium">Bookings</h1>
    <p class="mt-2 text-stone-500">Requests sent to hosts, guides and drivers. Payment is arranged directly with them.</p>

    <div v-if="bookings && !bookings.length" class="mt-12 rounded-2xl border border-dashed border-stone-300 p-12 text-center text-stone-500">
      No requests yet — open a trip and choose “Hosts &amp; guides nearby”.
    </div>

    <div class="mt-10 divide-y divide-stone-200 overflow-hidden rounded-2xl border border-stone-200 bg-white">
      <div v-for="b in bookings ?? []" :key="b.id" class="flex flex-col gap-4 p-5 sm:flex-row sm:items-center">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-stone-100 text-stone-600">
          <i :class="['pi', SERVICE_TYPES.find((t) => t.value === b.serviceType)?.icon]" />
        </div>
        <div class="min-w-0 flex-1">
          <p class="font-medium">{{ b.serviceTitle }}</p>
          <p class="text-sm text-stone-500">
            {{ b.providerName }} · {{ date(b.dateFrom) }} → {{ date(b.dateTo) }} · {{ b.guests }} guest{{ b.guests > 1 ? 's' : '' }}
          </p>
          <p v-if="b.providerComment" class="mt-1.5 text-sm text-stone-600">“{{ b.providerComment }}”</p>
        </div>
        <div class="flex items-center gap-3">
          <RouterLink v-if="b.planId" :to="{ name: 'trip', params: { id: b.planId } }"
                      class="text-stone-400 hover:text-ink" v-tooltip.top="'Open trip'" aria-label="Open trip">
            <i class="pi pi-map" />
          </RouterLink>
          <span :class="['rounded-full px-2.5 py-1 text-xs font-medium', STATUS[b.status]]">{{ b.status.toLowerCase() }}</span>
          <button v-if="b.status === 'PENDING' || b.status === 'CONFIRMED'"
                  class="text-sm text-stone-500 hover:text-rose-600" @click="cancel(b)">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>
