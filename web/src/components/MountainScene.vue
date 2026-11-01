<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const offset = ref(0)
const onScroll = () => (offset.value = Math.min(window.scrollY, 600))
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <svg class="absolute inset-0 h-full w-full" viewBox="0 0 1440 800" preserveAspectRatio="xMidYMax slice" aria-hidden="true">
    <defs>
      <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#0b2240" />
        <stop offset="0.55" stop-color="#1d4e89" />
        <stop offset="1" stop-color="#e9b86a" />
      </linearGradient>
      <linearGradient id="ridge1" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#7fa6cc" />
        <stop offset="1" stop-color="#3d6c9a" />
      </linearGradient>
      <linearGradient id="ridge2" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#2c5a85" />
        <stop offset="1" stop-color="#14365c" />
      </linearGradient>
      <linearGradient id="meadow" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0" stop-color="#5b8a3c" />
        <stop offset="1" stop-color="#2f4d1e" />
      </linearGradient>
    </defs>

    <rect width="1440" height="800" fill="url(#sky)" />
    <circle cx="1080" cy="300" r="70" fill="#f3c66b" opacity="0.9" />
    <circle cx="1080" cy="300" r="120" fill="#f3c66b" opacity="0.15" />

    <g :transform="`translate(0 ${offset * 0.15})`">
      <path
        d="M0 470 L120 380 L210 430 L330 300 L430 400 L520 340 L640 250 L760 360 L860 310 L980 390 L1090 290 L1200 380 L1310 320 L1440 400 L1440 800 L0 800 Z"
        fill="url(#ridge1)"
      />
      <path
        d="M330 300 L372 342 L350 340 L330 360 L312 338 L292 344 Z M640 250 L690 300 L662 296 L640 318 L618 296 L594 302 Z M1090 290 L1132 330 L1108 328 L1090 346 L1070 326 L1052 330 Z"
        fill="#f7f3ea"
        opacity="0.9"
      />
    </g>

    <g :transform="`translate(0 ${offset * 0.3})`">
      <path
        d="M0 560 L160 450 L280 520 L420 420 L560 520 L700 460 L860 540 L1000 450 L1160 530 L1300 470 L1440 520 L1440 800 L0 800 Z"
        fill="url(#ridge2)"
      />
    </g>

    <g :transform="`translate(0 ${offset * 0.45})`">
      <path d="M0 640 Q360 580 720 630 T1440 610 L1440 800 L0 800 Z" fill="url(#meadow)" />
      <g transform="translate(250 600)">
        <path d="M0 30 L0 8 Q30 -18 60 8 L60 30 Z" fill="#f7f3ea" />
        <path d="M-4 10 Q30 -26 64 10" fill="none" stroke="#b8432f" stroke-width="5" />
        <rect x="24" y="14" width="12" height="16" fill="#b8432f" />
      </g>
    </g>
  </svg>
</template>
