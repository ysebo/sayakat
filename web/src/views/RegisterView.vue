<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import SelectButton from 'primevue/selectbutton'
import AuthCard from '@/components/AuthCard.vue'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/api/http'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = ref({
  fullName: '',
  email: '',
  password: '',
  role: route.query.role === 'PROVIDER' ? 'PROVIDER' : 'TOURIST',
  providerName: '',
})
const roles = [
  { label: 'Traveller', value: 'TOURIST' },
  { label: 'Local provider', value: 'PROVIDER' },
]
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(form.value)
    router.push(auth.homeRoute())
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthCard title="Join Sayakat" subtitle="Plan journeys or host travellers">
    <form class="space-y-4" @submit.prevent="submit">
      <SelectButton v-model="form.role" :options="roles" option-label="label" option-value="value" :allow-empty="false" fluid />
      <div class="flex flex-col gap-1.5">
        <label for="name" class="text-sm font-medium">Full name</label>
        <InputText id="name" v-model="form.fullName" required autocomplete="name" />
      </div>
      <div v-if="form.role === 'PROVIDER'" class="flex flex-col gap-1.5">
        <label for="provider" class="text-sm font-medium">Business name</label>
        <InputText id="provider" v-model="form.providerName" placeholder="e.g. Son-Kul Yurt Camp" />
      </div>
      <div class="flex flex-col gap-1.5">
        <label for="email" class="text-sm font-medium">Email</label>
        <InputText id="email" v-model="form.email" type="email" required autocomplete="email" />
      </div>
      <div class="flex flex-col gap-1.5">
        <label for="password" class="text-sm font-medium">Password</label>
        <Password input-id="password" v-model="form.password" toggle-mask fluid required autocomplete="new-password"
                  prompt-label="At least 8 characters" />
      </div>
      <Message v-if="error" severity="error" size="small">{{ error }}</Message>
      <Button type="submit" label="Create account" :loading="loading" class="w-full" />
    </form>
    <p class="mt-6 text-center text-sm text-lake-900/70">
      Already have an account? <RouterLink :to="{ name: 'login' }" class="font-medium text-lake-700">Sign in</RouterLink>
    </p>
  </AuthCard>
</template>
