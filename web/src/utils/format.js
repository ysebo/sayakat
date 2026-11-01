export const CATEGORIES = {
  CITY: { label: 'City', icon: 'pi-building', color: '#14365c' },
  NATURE: { label: 'Nature', icon: 'pi-sun', color: '#5b8a3c' },
  LAKE: { label: 'Lakes', icon: 'pi-globe', color: '#2c6aa0' },
  HIKING: { label: 'Hiking', icon: 'pi-flag', color: '#3f6428' },
  VIEWPOINT: { label: 'Viewpoints', icon: 'pi-eye', color: '#7a5c99' },
  HISTORY: { label: 'History', icon: 'pi-history', color: '#a8741f' },
  CULTURE: { label: 'Culture', icon: 'pi-star', color: '#b8432f' },
  MUSEUM: { label: 'Museums', icon: 'pi-book', color: '#6b4f2a' },
  BAZAAR: { label: 'Bazaars', icon: 'pi-shopping-cart', color: '#d07a1e' },
  HOT_SPRING: { label: 'Hot springs', icon: 'pi-heart', color: '#c0587a' },
}

export const INTEREST_OPTIONS = Object.entries(CATEGORIES)
  .filter(([key]) => key !== 'CITY')
  .map(([value, c]) => ({ value, ...c }))

export const PACES = [
  { value: 'RELAXED', label: 'Relaxed', hint: 'up to 2 places a day, 09:00–18:00' },
  { value: 'MODERATE', label: 'Moderate', hint: 'up to 3 places a day, 09:00–19:00' },
  { value: 'INTENSE', label: 'Intense', hint: 'up to 5 places a day, 08:00–20:00' },
]

export const SERVICE_TYPES = [
  { value: 'ACCOMMODATION', label: 'Accommodation', icon: 'pi-home' },
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

export const DAY_COLORS = ['#2c6aa0', '#d9a441', '#5b8a3c', '#b8432f', '#7a5c99', '#d07a1e', '#14365c', '#c0587a']

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']

export const category = (key) => CATEGORIES[key] ?? { label: key, icon: 'pi-map-marker', color: '#14365c' }

export const som = (value) => (value == null ? '—' : `${new Intl.NumberFormat('en-US').format(value)} KGS`)

export const unitLabel = (unit) => PRICE_UNITS.find((u) => u.value === unit)?.label ?? ''

export function season(from, to) {
  if (from === 1 && to === 12) return 'All year'
  return `${MONTHS[from - 1]} – ${MONTHS[to - 1]}`
}

export function duration(minutes) {
  if (minutes == null) return '—'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return h ? `${h} h${m ? ` ${m} min` : ''}` : `${m} min`
}

export function date(value, opts = { weekday: 'short', day: 'numeric', month: 'short' }) {
  if (!value) return ''
  return new Date(`${value}T00:00:00`).toLocaleDateString('en-GB', opts)
}

export function addDays(isoDate, days) {
  const d = new Date(`${isoDate}T00:00:00`)
  d.setDate(d.getDate() + days)
  return toIso(d)
}

export const escapeHtml = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => `&#${c.charCodeAt(0)};`)

export function toIso(d) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
