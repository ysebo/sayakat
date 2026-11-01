<script setup>
defineProps({ title: String, subtitle: String })
</script>

<template>
  <div class="relative flex min-h-[calc(100vh-73px)] items-center justify-center overflow-hidden px-4 py-12">
    <div class="absolute inset-0 -z-10 bg-gradient-to-br from-lake-50 via-snow to-ochre-100" />
    <div class="w-full max-w-md rounded-3xl bg-white p-8 shadow-xl ring-1 ring-lake-100">
      <h1 class="text-3xl font-semibold">{{ title }}</h1>
      <p class="mt-1 mb-6 text-lake-900/60">{{ subtitle }}</p>
      <slot />
    </div>
  </div>
</template>
