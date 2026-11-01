import axios from 'axios'

const TOKEN_KEY = 'sayakat.token'

export const http = axios.create({ baseURL: '/api' })

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
  }
}

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let onUnauthorized = () => {}
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && getToken()) onUnauthorized()
    return Promise.reject(error)
  },
)

export function errorMessage(error) {
  const data = error?.response?.data
  if (data?.detail) return data.detail
  if (data?.errors) return data.errors.map((e) => e.defaultMessage).join(', ')
  if (error?.response?.status === 401) return 'Please sign in'
  if (error?.response?.status === 403) return 'You do not have access to this'
  return error?.message ?? 'Something went wrong'
}
