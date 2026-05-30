<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  workspaceName: string;
  projectName?: string;
  active:
    | "projects"
    | "files"
    | "analysis"
    | "tasks"
    | "reports"
    | "workspace-select";
  workspaceId?: string;
  projectId?: string;
}>();

const navItems = computed(() => [
  {
    label: "项目概览",
    key: "projects",
    href: props.workspaceId
      ? `/workspaces/${props.workspaceId}/projects`
      : "/workspace-select",
  },
  {
    label: "文件管理",
    key: "files",
    href:
      props.workspaceId && props.projectId
        ? `/workspaces/${props.workspaceId}/projects/${props.projectId}/files`
        : "#",
  },
  {
    label: "智能分析",
    key: "analysis",
    href:
      props.workspaceId && props.projectId
        ? `/workspaces/${props.workspaceId}/projects/${props.projectId}/analysis`
        : "#",
  },
  {
    label: "任务看板",
    key: "tasks",
    href:
      props.workspaceId && props.projectId
        ? `/workspaces/${props.workspaceId}/projects/${props.projectId}/tasks`
        : "#",
  },
  {
    label: "报告中心",
    key: "reports",
    href:
      props.workspaceId && props.projectId
        ? `/workspaces/${props.workspaceId}/projects/${props.projectId}/reports`
        : "#",
  },
]);
</script>

<template>
  <div class="min-h-screen bg-slate-100 text-ink">
    <header
      class="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur"
    >
      <div
        class="mx-auto flex max-w-[1440px] items-center justify-between px-8 py-4"
      >
        <div class="flex items-center gap-4">
          <div
            class="grid h-12 w-12 place-items-center rounded-2xl bg-brand-600 text-white"
          >
            D
          </div>
          <div>
            <p class="text-xs uppercase tracking-[0.24em] text-slate-400">
              Shinkou 企业版
            </p>
            <p class="text-lg font-semibold">
              {{ props.workspaceName || "工作区" }}
            </p>
          </div>
        </div>

        <div class="flex flex-1 items-center justify-center px-6">
          <div class="relative w-full max-w-[560px]">
            <input
              class="h-12 w-full rounded-full border border-slate-300 bg-slate-50 px-5 pr-12 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10"
              placeholder="搜索文件、代码、文档或功能..."
            />
            <span
              class="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-slate-400"
              >⌘K</span
            >
          </div>
        </div>

        <div class="flex items-center gap-5">
          <button
            class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-700 transition hover:bg-slate-50"
          >
            GPT-4o
          </button>
          <button class="text-slate-500 transition hover:text-ink">🔔</button>
          <div
            class="flex items-center gap-3 rounded-full border border-slate-200 bg-white px-4 py-2 shadow-sm"
          >
            <span class="h-9 w-9 rounded-full bg-slate-200"></span>
            <span class="text-sm">Zhang Wei</span>
          </div>
        </div>
      </div>
    </header>

    <div
      class="mx-auto grid max-w-[1440px] grid-cols-[280px_1fr] gap-6 px-8 py-8"
    >
      <aside
        class="space-y-7 rounded-[28px] border border-slate-200 bg-white p-6 shadow-soft"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-400">项目概览</p>
            <h2 class="mt-2 text-xl font-semibold">
              { props.projectName || 'Order System Demo' }
            </h2>
          </div>
          <span
            class="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-700"
            >{{ props.active.toUpperCase() }}</span
          >
        </div>

        <nav class="space-y-2">
          <a
            v-for="item in navItems"
            :key="item.key"
            :href="item.href"
            class="block rounded-2xl px-4 py-3 text-sm font-medium transition hover:bg-slate-100"
            :class="
              item.key === props.active
                ? 'bg-brand-600 text-white'
                : 'text-slate-700'
            "
          >
            {{ item.label }}
          </a>
        </nav>

        <div class="rounded-3xl bg-slate-50 p-5">
          <p class="mb-2 text-sm font-semibold text-slate-700">项目健康度</p>
          <div class="space-y-3 text-sm text-slate-600">
            <div class="flex items-center justify-between">
              <span>索引完整性</span>
              <span class="font-semibold text-brand-600">100%</span>
            </div>
            <div class="flex items-center justify-between">
              <span>文档覆盖率</span>
              <span class="font-semibold text-brand-600">88%</span>
            </div>
            <div class="flex items-center justify-between">
              <span>代码解析率</span>
              <span class="font-semibold text-brand-600">94%</span>
            </div>
            <div class="flex items-center justify-between">
              <span>依赖解析</span>
              <span class="font-semibold text-brand-600">91%</span>
            </div>
          </div>
        </div>
      </aside>

      <section class="space-y-6">
        <slot />
      </section>
    </div>
  </div>
</template>
