<script setup lang="ts">
import { X } from "lucide-vue-next";

defineProps<{ open: boolean; title: string }>();
defineEmits<{ close: [] }>();
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-end justify-center bg-slate-950/40 p-3 sm:items-center sm:p-4"
    >
      <div
        class="card max-h-[calc(100vh-1.5rem)] w-full max-w-xl overflow-hidden"
      >
        <div
          class="flex items-center justify-between border-b border-slate-200 px-4 py-3 sm:px-5 sm:py-4"
        >
          <h3 class="text-lg font-black">{{ title }}</h3>
          <button
            class="icon-btn"
            type="button"
            aria-label="关闭弹窗"
            @click="$emit('close')"
          >
            <X :size="18" />
          </button>
        </div>
        <div
          class="max-h-[calc(100vh-6rem)] overflow-auto p-4 scrollbar-thin sm:p-5"
        >
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>
