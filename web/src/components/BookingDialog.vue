<script setup>
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import DatePicker from 'primevue/datepicker'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import Message from 'primevue/message'
import { useToast } from 'primevue/usetoast'
import { bookingApi } from '@/api'
import { errorMessage } from '@/api/http'
import { som, toIso, unitLabel } from '@/utils/format'

const props = defineProps({ service: Object, date: String, planId: Number })
const visible = defineModel('visible', { type: Boolean })
const emit = defineEmits(['booked'])
const toast = useToast()

const from = ref(null)
const to = ref(null)
const guests = ref(1)
const message = ref('')
const error = ref('')
const loading = ref(false)

watch(visible, (open) => {
  if (!open) return
  const start = new Date(`${props.date}T00:00:00`)
  const end = new Date(start)
  end.setDate(end.getDate() + 1)
  from.value = start
  to.value = end
  guests.value = 1
  message.value = ''
  error.value = ''
})

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await bookingApi.create({
      serviceId: props.service.id,
      planId: props.planId,
      dateFrom: toIso(from.value),
      dateTo: toIso(to.value),
      guests: guests.value,
      message: message.value || null,
    })
    toast.add({ severity: 'success', summary: 'Request sent', detail: `${props.service.providerName} will confirm soon`, life: 4000 })
    visible.value = false
    emit('booked')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <Dialog v-model:visible="visible" modal :header="service?.title" class="w-[min(32rem,95vw)]">
    <template v-if="service">
      <p class="-mt-2 mb-5 text-sm text-stone-500">
        {{ service.providerName }} · {{ som(service.priceKgs) }} {{ unitLabel(service.priceUnit) }}
      </p>
      <div class="grid gap-4 sm:grid-cols-2">
        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium">From</label>
          <DatePicker v-model="from" date-format="d M yy" fluid />
        </div>
        <div class="flex flex-col gap-1.5">
          <label class="text-sm font-medium">To</label>
          <DatePicker v-model="to" date-format="d M yy" :min-date="from" fluid />
        </div>
        <div class="flex flex-col gap-1.5 sm:col-span-2">
          <label class="text-sm font-medium">Guests</label>
          <InputNumber v-model="guests" :min="1" :max="service.capacity ?? 50" show-buttons fluid />
        </div>
        <div class="flex flex-col gap-1.5 sm:col-span-2">
          <label class="text-sm font-medium">Message to the host</label>
          <Textarea v-model="message" rows="3" auto-resize placeholder="Arrival time, dietary needs…" />
        </div>
      </div>
      <Message v-if="error" severity="error" size="small" class="mt-4">{{ error }}</Message>
    </template>
    <template #footer>
      <Button label="Cancel" text @click="visible = false" />
      <Button label="Send request" icon="pi pi-send" :loading="loading" @click="submit" />
    </template>
  </Dialog>
</template>
