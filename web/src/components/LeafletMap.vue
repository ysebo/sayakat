<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import L from 'leaflet'

const props = defineProps({
  markers: { type: Array, default: () => [] },
  polylines: { type: Array, default: () => [] },
  center: { type: Array, default: () => [41.6, 74.6] },
  zoom: { type: Number, default: 7 },
  fitToContent: { type: Boolean, default: true },
  focus: { type: Object, default: null },
})
const emit = defineEmits(['marker-click', 'map-click'])

const el = ref(null)
let map
let layer

const escape = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => `&#${c.charCodeAt(0)};`)

function render() {
  if (!map) return
  layer.clearLayers()
  const bounds = []
  for (const line of props.polylines) {
    if (line.points.length < 2) continue
    L.polyline(line.points, {
      color: line.color,
      weight: 4,
      opacity: 0.85,
      dashArray: line.dashed ? '6 8' : null,
    }).addTo(layer)
    bounds.push(...line.points)
  }
  for (const m of props.markers) {
    const html = m.label != null
      ? `<div class="poi-marker numbered" style="background:${m.color}">${escape(m.label)}</div>`
      : `<div class="poi-marker" style="background:${m.color}"><i class="pi ${m.icon ?? 'pi-map-marker'}"></i></div>`
    const marker = L.marker([m.lat, m.lon], {
      icon: L.divIcon({ html, className: '', iconSize: [30, 30], iconAnchor: [15, 15], popupAnchor: [0, -14] }),
      title: m.title ?? '',
    }).addTo(layer)
    if (m.popup) marker.bindPopup(m.popup)
    marker.on('click', () => emit('marker-click', m.id))
    bounds.push([m.lat, m.lon])
  }
  if (props.fitToContent && bounds.length > 1) map.fitBounds(bounds, { padding: [40, 40], maxZoom: 11 })
}

onMounted(() => {
  map = L.map(el.value, { zoomControl: true, scrollWheelZoom: true }).setView(props.center, props.zoom)
  const osm = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  })
  const topo = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
    maxZoom: 17,
    attribution: 'Map data &copy; OpenStreetMap contributors, SRTM | Style &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (CC-BY-SA)',
  })
  osm.addTo(map)
  L.control.layers({ Streets: osm, Terrain: topo }, null, { position: 'topright' }).addTo(map)
  layer = L.layerGroup().addTo(map)
  map.on('click', (e) => emit('map-click', { lat: e.latlng.lat, lon: e.latlng.lng }))
  render()
  setTimeout(() => map?.invalidateSize(), 350)
})

onBeforeUnmount(() => map?.remove())

watch(() => [props.markers, props.polylines], render, { deep: true })
watch(
  () => props.focus,
  (f) => f && map?.flyTo([f.lat, f.lon], f.zoom ?? 11, { duration: 0.8 }),
)

defineExpose({ invalidate: () => map?.invalidateSize() })
</script>

<template>
  <div ref="el" class="h-full w-full" />
</template>
