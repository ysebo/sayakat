<script setup>
import { satelliteTile } from '@/utils/format'

defineProps({ title: String, subtitle: String })

const tiles = [
  [42.42, 77.25, 10],
  [41.83, 75.13, 11],
  [42.33, 78.22, 12],
  [42.57, 74.49, 12],
]
</script>

<template>
  <div class="grid min-h-[calc(100svh-64px)] lg:grid-cols-2">
    <div class="flex items-center justify-center px-4 py-12">
      <div class="w-full max-w-sm">
        <h1 class="text-3xl font-medium">{{ title }}</h1>
        <p class="mt-2 mb-8 text-stone-500">{{ subtitle }}</p>
        <slot />
      </div>
    </div>
    <div class="relative hidden overflow-hidden bg-ink lg:block">
      <div class="grid h-full grid-cols-2 grid-rows-2 gap-1 opacity-90">
        <img v-for="t in tiles" :key="t.join()" :src="satelliteTile(...t)" alt="" class="h-full w-full object-cover" />
      </div>
      <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-black/10 to-transparent" />
      <p class="absolute bottom-10 left-10 right-10 text-3xl font-medium leading-snug text-white">
        “Саякат” —
        <span class="font-serif italic text-teal-200">a journey, planned with care.</span>
      </p>
    </div>
  </div>
</template>
