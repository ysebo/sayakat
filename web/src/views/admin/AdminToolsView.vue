<script setup>
import { ref } from 'vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Message from 'primevue/message'
import { adminApi } from '@/api'
import { errorMessage } from '@/api/http'

const busy = ref('')
const result = ref(null)
const error = ref('')

async function run(name, action) {
  busy.value = name
  error.value = ''
  result.value = null
  try {
    result.value = await action()
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    busy.value = ''
  }
}
</script>

<template>
  <h1 class="mb-6 text-3xl font-medium">Dataset &amp; travel matrix</h1>

  <div class="grid gap-6 md:grid-cols-2">
    <Card>
      <template #title>Import dataset</template>
      <template #content>
        <p class="text-sm text-surface-600">
          Re-reads <code>data/pois.csv</code> from the API and upserts attractions by external id.
        </p>
      </template>
      <template #footer>
        <Button label="Import POIs" icon="pi pi-upload" :loading="busy === 'import'"
                @click="run('import', adminApi.importPois)" />
      </template>
    </Card>
    <Card>
      <template #title>Rebuild travel matrix</template>
      <template #content>
        <p class="text-sm text-surface-600">
          Recomputes driving time between every attraction and lodging (OSRM if enabled, otherwise a straight-line
          estimate). Run after changing coordinates or adding lodgings.
        </p>
      </template>
      <template #footer>
        <Button label="Rebuild matrix" icon="pi pi-sync" :loading="busy === 'matrix'"
                @click="run('matrix', adminApi.rebuildMatrix)" />
      </template>
    </Card>
  </div>

  <Message v-if="error" severity="error" class="mt-6">{{ error }}</Message>
  <Message v-if="result" severity="success" class="mt-6">
    <pre class="m-0 text-xs">{{ JSON.stringify(result, null, 2) }}</pre>
  </Message>
</template>
