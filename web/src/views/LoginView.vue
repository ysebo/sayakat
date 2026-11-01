<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import AuthCard from '@/components/AuthCard.vue'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/api/http'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    router.push(route.query.redirect || auth.homeRoute())
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthCard title="Welcome back" subtitle="Sign in to see your trips and bookings">
    <form class="space-y-4" @submit.prevent="submit">
      <div class="flex flex-col gap-1.5">
        <label for="email" class="text-sm font-medium">Email</label>
        <InputText id="email" v-model="email" type="email" autocomplete="email" required />
      </div>
      <div class="flex flex-col gap-1.5">
        <label for="password" class="text-sm font-medium">Password</label>
        <Password input-id="password" v-model="password" :feedback="false" toggle-mask fluid required
                  autocomplete="current-password" />
      </div>
      <Message v-if="error" severity="error" size="small">{{ error }}</Message>
      <Button type="submit" label="Sign in" :loading="loading" severity="contrast" class="w-full !py-2.5" />
    </form>
    <p class="mt-6 text-sm text-stone-500">
      New here? <RouterLink :to="{ name: 'register' }" class="font-medium text-ink underline underline-offset-4">Create an account</RouterLink>
    </p>
  </AuthCard>
</template>
