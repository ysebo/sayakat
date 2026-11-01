import { http } from './http'

const data = (promise) => promise.then((r) => r.data)

export const authApi = {
  login: (email, password) => data(http.post('/auth/login', { email, password })),
  register: (payload) => data(http.post('/auth/register', payload)),
  me: () => data(http.get('/me')),
}

export const poiApi = {
  list: (params = {}) => data(http.get('/pois', { params })),
  get: (id) => data(http.get(`/pois/${id}`)),
  create: (poi) => data(http.post('/admin/pois', poi)),
  update: (id, poi) => data(http.put(`/admin/pois/${id}`, poi)),
  remove: (id) => http.delete(`/admin/pois/${id}`),
}

export const planApi = {
  create: (request) => data(http.post('/plans', request)),
  get: (id) => data(http.get(`/plans/${id}`)),
  mine: () => data(http.get('/plans')),
  servicesForDay: (id, day, type) =>
    data(http.get(`/plans/${id}/days/${day}/services`, { params: { type, radiusKm: 30 } })),
}

export const serviceApi = {
  near: (lat, lon, radiusKm = 30, type) => data(http.get('/services', { params: { lat, lon, radiusKm, type } })),
  mine: () => data(http.get('/provider/services')),
  create: (s) => data(http.post('/provider/services', s)),
  update: (id, s) => data(http.put(`/provider/services/${id}`, s)),
  deactivate: (id) => http.delete(`/provider/services/${id}`),
}

export const bookingApi = {
  create: (b) => data(http.post('/bookings', b)),
  mine: () => data(http.get('/bookings')),
  cancel: (id) => data(http.post(`/bookings/${id}/cancel`)),
  incoming: () => data(http.get('/provider/bookings')),
  confirm: (id, comment) => data(http.post(`/provider/bookings/${id}/confirm`, { comment })),
  reject: (id, comment) => data(http.post(`/provider/bookings/${id}/reject`, { comment })),
}

export const adminApi = {
  rebuildMatrix: () => data(http.post('/admin/matrix/rebuild')),
  importPois: () => data(http.post('/admin/dataset/pois/import')),
}
