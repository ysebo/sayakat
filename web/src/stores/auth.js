import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api'
import { getToken, setToken, setUnauthorizedHandler } from '@/api/http'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const user = ref(null)
  const ready = ref(false)

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const role = computed(() => user.value?.role ?? null)

  function apply(response) {
    token.value = response.accessToken
    user.value = response.user
    setToken(response.accessToken)
  }

  async function login(email, password) {
    apply(await authApi.login(email, password))
  }

  async function register(payload) {
    apply(await authApi.register(payload))
  }

  function logout() {
    token.value = null
    user.value = null
    setToken(null)
  }

  async function init() {
    if (ready.value) return
    if (token.value) {
      try {
        user.value = await authApi.me()
      } catch {
        logout()
      }
    }
    ready.value = true
  }

  function homeRoute() {
    if (role.value === 'PROVIDER') return { name: 'provider-bookings' }
    if (role.value === 'ADMIN') return { name: 'admin-pois' }
    return { name: 'trips' }
  }

  setUnauthorizedHandler(logout)

  return { token, user, ready, isAuthenticated, role, login, register, logout, init, homeRoute }
})
