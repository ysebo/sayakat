export const CATEGORIES = {
  CITY: { label: 'City', icon: 'pi-building', color: '#44403c' },
  NATURE: { label: 'Nature', icon: 'pi-sun', color: '#4d7c0f' },
  LAKE: { label: 'Lakes', icon: 'pi-globe', color: '#0e7490' },
  HIKING: { label: 'Hiking', icon: 'pi-flag', color: '#15803d' },
  VIEWPOINT: { label: 'Viewpoints', icon: 'pi-eye', color: '#6d28d9' },
  HISTORY: { label: 'History', icon: 'pi-history', color: '#a16207' },
  CULTURE: { label: 'Culture', icon: 'pi-star', color: '#be123c' },
  MUSEUM: { label: 'Museums', icon: 'pi-book', color: '#7c2d12' },
  BAZAAR: { label: 'Bazaars', icon: 'pi-shopping-cart', color: '#c2410c' },
  HOT_SPRING: { label: 'Hot springs', icon: 'pi-heart', color: '#be185d' },
}

export const INTEREST_OPTIONS = Object.entries(CATEGORIES)
  .filter(([key]) => key !== 'CITY')
  .map(([value, c]) => ({ value, ...c }))

export const PACES = [
  { value: 'RELAXED', label: 'Relaxed', hint: '2 places a day', hours: '09:00 – 18:00' },
  { value: 'MODERATE', label: 'Balanced', hint: '3 places a day', hours: '09:00 – 19:00' },
  { value: 'INTENSE', label: 'Intense', hint: '5 places a day', hours: '08:00 – 20:00' },
]

export const SERVICE_TYPES = [
  { value: 'ACCOMMODATION', label: 'Stay', icon: 'pi-home' },
  { value: 'GUIDE', label: 'Guide', icon: 'pi-user' },
  { value: 'TRANSPORT', label: 'Transport', icon: 'pi-car' },
]

export const PRICE_UNITS = [
  { value: 'PER_NIGHT', label: 'per night' },
  { value: 'PER_DAY', label: 'per day' },
  { value: 'PER_TRIP', label: 'per trip' },
  { value: 'PER_PERSON', label: 'per person' },
]

export const BOOKING_SEVERITY = { PENDING: 'warn', CONFIRMED: 'success', REJECTED: 'danger', CANCELLED: 'secondary' }

export const DAY_COLORS = ['#0d9488', '#ea580c', '#4f46e5', '#db2777', '#65a30d', '#0284c7', '#9333ea', '#ca8a04']

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

export const category = (key) => CATEGORIES[key] ?? { label: key, icon: 'pi-map-marker', color: '#44403c' }

export const som = (value) => (value == null ? '—' : `${new Intl.NumberFormat('en-US').format(value)} KGS`)

export const compactSom = (value) =>
  value == null ? '—' : `${new Intl.NumberFormat('en-US', { notation: 'compact', maximumFractionDigits: 1 }).format(value)}`

export const unitLabel = (unit) => PRICE_UNITS.find((u) => u.value === unit)?.label ?? ''

export function season(from, to) {
  if (from === 1 && to === 12) return 'All year'
  return `${MONTHS[from - 1]} – ${MONTHS[to - 1]}`
}

export function duration(minutes) {
  if (minutes == null) return '—'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return h ? `${h}h${m ? ` ${m}m` : ''}` : `${m}m`
}

export function date(value, opts = { weekday: 'short', day: 'numeric', month: 'short' }) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', opts)
}

export const escapeHtml = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => `&#${c.charCodeAt(0)};`)

export function toIso(d) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function satelliteTile(lat, lon, zoom = 13) {
  const n = 2 ** zoom
  const x = Math.floor(((lon + 180) / 360) * n)
  const rad = (lat * Math.PI) / 180
  const y = Math.floor(((1 - Math.log(Math.tan(rad) + 1 / Math.cos(rad)) / Math.PI) / 2) * n)
  return `https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${zoom}/${y}/${x}`
}
