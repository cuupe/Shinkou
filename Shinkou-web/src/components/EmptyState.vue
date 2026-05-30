<script setup lang="ts">
import { Inbox } from "lucide-vue-next";

defineProps<{ title: string; description?: string }>();
</script>

<template>
  <div
    class="card flex min-h-[180px] flex-col items-center justify-center p-6 text-center sm:p-8"
  >
    <div
      class="mb-4 grid h-12 w-12 place-items-center rounded-lg bg-slate-100 text-slate-500"
    >
      <Inbox :size="24" />
    </div>
    <h3 class="text-base font-black text-slate-900">{{ title }}</h3>
    <p
      v-if="description"
      class="mt-2 max-w-md text-sm leading-6 text-slate-500"
    >
      {{ description }}
    </p>
    <slot />
  </div>
</template>
