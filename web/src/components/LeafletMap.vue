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
  basemap: { type: String, default: 'light' },
  interactive: { type: Boolean, default: true },
  showLayers: { type: Boolean, default: true },
})
const emit = defineEmits(['marker-click', 'map-click'])

const el = ref(null)
let map
let layer

const escape = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => `&#${c.charCodeAt(0)};`)

const BASEMAPS = {
  light: () =>
    L.layerGroup([
      L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 16,
        attribution: 'Tiles &copy; Esri — Esri, HERE, Garmin, &copy; OpenStreetMap contributors',
      }),
      L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 16,
      }),
    ]),
  satellite: () =>
    L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
      maxZoom: 18,
      attribution: 'Imagery &copy; Esri, Maxar, Earthstar Geographics',
    }),
  terrain: () =>
    L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
      maxZoom: 17,
      attribution: '&copy; OpenStreetMap contributors, SRTM | &copy; <a href="https://opentopomap.org">OpenTopoMap</a>',
    }),
}

function pinHtml(m) {
  if (m.label != null) return `<div class="map-pin" style="background:${m.color}">${escape(m.label)}</div>`
  if (m.dot) return `<div class="map-pin dot" style="background:${m.color}"></div>`
  return `<div class="map-pin" style="background:${m.color}"><i class="pi ${m.icon ?? 'pi-map-marker'}" style="font-size:11px"></i></div>`
}

function render() {
  if (!map) return
  layer.clearLayers()
  const bounds = []
  for (const line of props.polylines) {
    if (line.points.length < 2) continue
    L.polyline(line.points, { color: '#ffffff', weight: 7, opacity: 0.9 }).addTo(layer)
    L.polyline(line.points, {
      color: line.color,
      weight: 3.5,
      opacity: 1,
      dashArray: line.dashed ? '6 8' : null,
      lineCap: 'round',
    }).addTo(layer)
    bounds.push(...line.points)
  }
  for (const m of props.markers) {
    const size = m.dot ? 14 : 28
    const marker = L.marker([m.lat, m.lon], {
      icon: L.divIcon({ html: pinHtml(m), className: '', iconSize: [size, size], iconAnchor: [size / 2, size / 2], popupAnchor: [0, -size / 2] }),
      title: m.title ?? '',
    }).addTo(layer)
    if (m.popup) marker.bindPopup(m.popup)
    marker.on('click', () => emit('marker-click', m.id))
    bounds.push([m.lat, m.lon])
  }
  if (props.fitToContent && bounds.length > 1) map.fitBounds(bounds, { padding: [48, 48], maxZoom: 11 })
}

onMounted(() => {
  map = L.map(el.value, {
    zoomControl: props.interactive,
    scrollWheelZoom: props.interactive,
    dragging: props.interactive,
    doubleClickZoom: props.interactive,
    attributionControl: true,
  }).setView(props.center, props.zoom)
  const layers = { Map: BASEMAPS.light(), Satellite: BASEMAPS.satellite(), Terrain: BASEMAPS.terrain() }
  const initial = { light: 'Map', satellite: 'Satellite', terrain: 'Terrain' }[props.basemap] ?? 'Map'
  layers[initial].addTo(map)
  if (props.interactive && props.showLayers) L.control.layers(layers, null, { position: 'topright' }).addTo(map)
  layer = L.layerGroup().addTo(map)
  map.on('click', (e) => emit('map-click', { lat: e.latlng.lat, lon: e.latlng.lng }))
  render()
  setTimeout(() => map?.invalidateSize(), 350)
})

onBeforeUnmount(() => map?.remove())

watch(() => [props.markers, props.polylines], render, { deep: true })
watch(
  () => props.focus,
  (f) => f && map?.flyTo([f.lat, f.lon], f.zoom ?? 11, { duration: f.duration ?? 0.8 }),
)

defineExpose({
  invalidate: () => map?.invalidateSize(),
  flyTo: (lat, lon, zoom, duration = 3) => map?.flyTo([lat, lon], zoom, { duration }),
})
</script>

<template>
  <div ref="el" class="h-full w-full" />
</template>
