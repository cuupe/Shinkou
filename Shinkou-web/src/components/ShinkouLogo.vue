<script setup lang="ts">
withDefaults(
  defineProps<{
    size?: "sm" | "md" | "lg";
    dark?: boolean;
  }>(),
  {
    size: "md",
    dark: false,
  },
);
</script>

<template>
  <div
    class="shinkou-logo"
    :class="[`shinkou-logo--${size}`, { 'shinkou-logo--dark': dark }]"
  >
    <span class="shinkou-mark" aria-hidden="true">
      <span class="shinkou-mark__left" />
      <span class="shinkou-mark__mid" />
      <span class="shinkou-mark__right" />
    </span>
    <span class="shinkou-logo__text">Shinkou</span>
  </div>
</template>
