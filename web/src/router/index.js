import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { layout: 'public' } },
  { path: '/explore', name: 'explore', component: () => import('@/views/ExploreView.vue'), meta: { layout: 'public' } },
  { path: '/plan', name: 'planner', component: () => import('@/views/PlannerView.vue'), meta: { layout: 'public' } },
  {
    path: '/trips',
    name: 'trips',
    component: () => import('@/views/TripsView.vue'),
    meta: { layout: 'public', roles: ['TOURIST'] },
  },
  {
    path: '/trips/:id',
    name: 'trip',
    component: () => import('@/views/PlanView.vue'),
    props: true,
    meta: { layout: 'public', roles: ['TOURIST'] },
  },
  {
    path: '/bookings',
    name: 'bookings',
    component: () => import('@/views/BookingsView.vue'),
    meta: { layout: 'public', roles: ['TOURIST'] },
  },
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { layout: 'public', guestOnly: true } },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { layout: 'public', guestOnly: true } },

  {
    path: '/provider/services',
    name: 'provider-services',
    component: () => import('@/views/provider/ProviderServicesView.vue'),
    meta: { layout: 'dashboard', roles: ['PROVIDER'] },
  },
  {
    path: '/provider/bookings',
    name: 'provider-bookings',
    component: () => import('@/views/provider/ProviderBookingsView.vue'),
    meta: { layout: 'dashboard', roles: ['PROVIDER'] },
  },
  {
    path: '/admin/pois',
    name: 'admin-pois',
    component: () => import('@/views/admin/AdminPoisView.vue'),
    meta: { layout: 'dashboard', roles: ['ADMIN'] },
  },
  {
    path: '/admin/tools',
    name: 'admin-tools',
    component: () => import('@/views/admin/AdminToolsView.vue'),
    meta: { layout: 'dashboard', roles: ['ADMIN'] },
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to, from, saved) => saved ?? { top: 0 },
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.init()
  if (to.meta.guestOnly && auth.isAuthenticated) return auth.homeRoute()
  if (to.meta.roles) {
    if (!auth.isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
    if (!to.meta.roles.includes(auth.role)) return auth.homeRoute()
  }
})

export default router
