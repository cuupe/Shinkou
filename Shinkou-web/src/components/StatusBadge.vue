<script setup lang="ts">
import { computed } from "vue";
import { statusCn } from "@/utils/format";
const props = defineProps<{ status?: string }>();
const tone = computed(() => {
  const s = props.status || "";
  if (["ACTIVE", "COMPLETED", "SUCCESS", "DONE", "CONVERTED"].includes(s))
    return "bg-emerald-50 text-emerald-700 border-emerald-200";
  if (
    [
      "RUNNING",
      "IN_PROGRESS",
      "PENDING",
      "DRAFT",
      "READY",
      "CONFIRMING",
    ].includes(s)
  )
    return "bg-blue-50 text-blue-700 border-blue-200";
  if (["FAILED", "DELETED", "REJECTED", "DISABLED"].includes(s))
    return "bg-rose-50 text-rose-700 border-rose-200";
  if (["ARCHIVED", "CANCELLED"].includes(s))
    return "bg-slate-100 text-slate-600 border-slate-200";
  return "bg-slate-50 text-slate-700 border-slate-200";
});
</script>
<template>
  <span
    class="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold"
    :class="tone"
    >{{ statusCn(status) }}</span
  >
</template>
