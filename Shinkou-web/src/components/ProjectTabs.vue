<script setup lang="ts">
import { computed } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { BarChart3, FileCode2, ListTodo, ScrollText } from "lucide-vue-next";

const route = useRoute();
const workspaceId = computed(() => route.params.workspaceId);
const projectId = computed(() => route.params.projectId);
const tabs = computed(() => [
  {
    key: "files",
    label: "文件管理",
    icon: FileCode2,
    to: `/workspaces/${workspaceId.value}/projects/${projectId.value}/files`,
  },
  {
    key: "analysis",
    label: "智能分析",
    icon: BarChart3,
    to: `/workspaces/${workspaceId.value}/projects/${projectId.value}/analysis`,
  },
  {
    key: "tasks",
    label: "任务看板",
    icon: ListTodo,
    to: `/workspaces/${workspaceId.value}/projects/${projectId.value}/tasks`,
  },
  {
    key: "reports",
    label: "报告中心",
    icon: ScrollText,
    to: `/workspaces/${workspaceId.value}/projects/${projectId.value}/reports`,
  },
]);
</script>

<template>
  <nav class="mobile-scroll flex gap-1 border-b border-slate-200">
    <RouterLink
      v-for="tab in tabs"
      :key="tab.key"
      :to="tab.to"
      class="-mb-px inline-flex shrink-0 items-center gap-2 border-b-2 px-3 py-3 text-sm font-bold sm:px-4"
      :class="
        route.path.includes('/' + tab.key)
          ? 'border-brand-600 text-brand-700'
          : 'border-transparent text-slate-500 hover:text-slate-900'
      "
    >
      <component :is="tab.icon" :size="16" />
      {{ tab.label }}
    </RouterLink>
  </nav>
</template>
