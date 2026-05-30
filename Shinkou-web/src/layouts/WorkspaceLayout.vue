<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import {
  Bell,
  Bot,
  ChevronDown,
  ChevronsLeft,
  FileCode2,
  FileText,
  Folder,
  Gauge,
  Home,
  ListChecks,
  Menu,
  Search,
  Settings,
  Sparkles,
  X,
} from "lucide-vue-next";
import ShinkouLogo from "@/components/ShinkouLogo.vue";
import { projectApi } from "@/api/project.api";
import { useAuthStore } from "@/stores/auth.store";
import { useProjectStore } from "@/stores/project.store";
import type { Project } from "@/types/domain";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const projectStore = useProjectStore();
const mobileOpen = ref(false);
const projects = ref<Project[]>([]);

const workspaceId = computed(() => String(route.params.workspaceId || ""));
const routeProjectId = computed(() =>
  route.params.projectId ? String(route.params.projectId) : "",
);
const rememberedProjectId = computed(
  () => localStorage.getItem(`lastProjectId:${workspaceId.value}`) || "",
);
const selectedProjectId = computed(
  () =>
    routeProjectId.value ||
    rememberedProjectId.value ||
    (projects.value[0] ? String(projects.value[0].id) : ""),
);
const hasSelectedProject = computed(() => Boolean(selectedProjectId.value));
const projectBase = computed(() =>
  hasSelectedProject.value
    ? `/workspaces/${workspaceId.value}/projects/${selectedProjectId.value}`
    : `/workspaces/${workspaceId.value}`,
);
const activeProject = computed(() => {
  if (
    projectStore.currentProject &&
    String(projectStore.currentProject.id) === selectedProjectId.value
  ) {
    return projectStore.currentProject;
  }
  return (
    projects.value.find(
      (item) => String(item.id) === selectedProjectId.value,
    ) || null
  );
});

const navItems = computed(() => [
  { label: "项目概览", icon: Home, to: `${projectBase.value}/overview` },
  { label: "文件管理", icon: Folder, to: `${projectBase.value}/files` },
  { label: "代码探索", icon: FileCode2, to: `${projectBase.value}/code` },
  { label: "智能体分析", icon: Bot, to: `${projectBase.value}/analysis` },
  { label: "任务看板", icon: ListChecks, to: `${projectBase.value}/tasks` },
  { label: "报告中心", icon: FileText, to: `${projectBase.value}/reports` },
  { label: "运行轨迹", icon: Gauge, to: `${projectBase.value}/runs` },
  {
    label: "模型配置",
    icon: Settings,
    to: `/workspaces/${workspaceId.value}/settings/models`,
  },
]);

const userInitial = computed(() =>
  (auth.user?.name || auth.user?.email || "S").slice(0, 1).toUpperCase(),
);

onMounted(loadProjects);
watch(workspaceId, loadProjects);

async function loadProjects() {
  if (!workspaceId.value) return;
  projects.value = await projectApi.list(workspaceId.value).catch(() => []);

  const firstProjectId = projects.value[0] ? String(projects.value[0].id) : "";
  const rememberedExists = projects.value.some(
    (item) => String(item.id) === rememberedProjectId.value,
  );
  const nextProjectId =
    routeProjectId.value ||
    (rememberedExists ? rememberedProjectId.value : firstProjectId);

  if (nextProjectId) {
    localStorage.setItem(`lastProjectId:${workspaceId.value}`, nextProjectId);
  }

  if (
    nextProjectId &&
    route.path === `/workspaces/${workspaceId.value}/projects`
  ) {
    router.replace(
      `/workspaces/${workspaceId.value}/projects/${nextProjectId}/overview`,
    );
  }
}

function selectProject(event: Event) {
  const value = (event.target as HTMLSelectElement).value;
  if (!value) {
    router.push(`/workspaces/${workspaceId.value}/projects`);
    return;
  }
  localStorage.setItem(`lastProjectId:${workspaceId.value}`, value);
  router.push(`/workspaces/${workspaceId.value}/projects/${value}/overview`);
}
</script>

<template>
  <div class="min-h-screen bg-[#f8fbff] text-slate-950">
    <aside
      class="fixed inset-y-0 left-0 z-30 hidden w-[240px] border-r border-slate-200 bg-white lg:block"
    >
      <div class="flex h-[76px] items-center px-7">
        <ShinkouLogo size="md" />
      </div>

      <nav class="space-y-2 px-4 py-3">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex h-12 items-center gap-3 rounded-lg px-4 text-[15px] font-bold text-slate-700 hover:bg-slate-50"
          active-class="!bg-[#edf3ff] !text-brand-600"
        >
          <component :is="item.icon" :size="21" />
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="absolute bottom-5 left-4 right-4">
        <button class="btn btn-ghost w-full justify-start">
          <ChevronsLeft :size="18" />
          收起侧边栏
        </button>
      </div>
    </aside>

    <header
      class="sticky top-0 z-20 border-b border-slate-200 bg-white/95 backdrop-blur lg:fixed lg:left-[240px] lg:right-0"
    >
      <div class="flex h-[76px] items-center gap-4 px-4 sm:px-6">
        <button
          class="icon-btn lg:hidden"
          type="button"
          @click="mobileOpen = !mobileOpen"
        >
          <X v-if="mobileOpen" :size="19" />
          <Menu v-else :size="19" />
        </button>

        <RouterLink class="lg:hidden" :to="`${projectBase}/overview`">
          <ShinkouLogo size="md" />
        </RouterLink>

        <select
          class="hidden h-12 w-[260px] rounded-xl border border-slate-200 bg-white px-4 text-[15px] font-semibold text-slate-900 outline-none lg:block"
          :value="selectedProjectId"
          @change="selectProject"
        >
          <option value="">选择项目</option>
          <option
            v-for="project in projects"
            :key="project.id"
            :value="project.id"
          >
            {{ project.name }}
          </option>
        </select>

        <label class="relative mx-auto hidden w-full max-w-[500px] lg:block">
          <Search
            :size="19"
            class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
          />
          <input
            class="h-12 w-full rounded-xl border border-slate-200 bg-white pl-11 pr-14 text-sm outline-none placeholder:text-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10"
            :placeholder="
              activeProject
                ? `搜索 ${activeProject.name} 的文件、代码、文档或功能...`
                : '搜索文件、代码、文档或功能...'
            "
          />
          <span
            class="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-semibold text-slate-400"
            >⌘ K</span
          >
        </label>

        <div class="ml-auto flex items-center gap-3">
          <div
            class="hidden h-10 items-center gap-2 rounded-full border border-slate-200 bg-white px-4 text-sm font-bold text-slate-900 sm:flex"
          >
            <Sparkles :size="17" class="text-brand-600" />
            模型：GPT-4o
          </div>
          <button
            class="relative hidden h-10 w-10 place-items-center rounded-full text-slate-700 hover:bg-slate-100 sm:grid"
          >
            <Bell :size="20" />
            <span
              class="absolute right-2 top-2 h-2 w-2 rounded-full bg-red-500"
            />
          </button>
          <div class="flex items-center gap-2">
            <div
              class="grid h-9 w-9 place-items-center rounded-full bg-brand-500 text-sm font-black text-white"
            >
              {{ userInitial }}
            </div>
            <span
              class="hidden max-w-[120px] truncate text-sm font-bold text-slate-900 sm:inline"
            >
              {{ auth.user?.name || auth.user?.email || "Shinkou User" }}
            </span>
            <ChevronDown :size="16" class="hidden text-slate-500 sm:block" />
          </div>
        </div>
      </div>

      <nav
        v-if="mobileOpen"
        class="border-t border-slate-100 bg-white p-3 lg:hidden"
      >
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex items-center gap-3 rounded-lg px-3 py-3 text-sm font-bold text-slate-700"
          active-class="bg-brand-50 text-brand-700"
          @click="mobileOpen = false"
        >
          <component :is="item.icon" :size="18" />
          {{ item.label }}
        </RouterLink>
      </nav>
    </header>

    <main class="lg:pl-[240px] lg:pt-[76px]">
      <div
        v-if="
          !hasSelectedProject &&
          route.path !== `/workspaces/${workspaceId}/projects`
        "
        class="mx-auto max-w-[1660px] px-4 py-6 sm:px-7 lg:px-8"
      >
        <div
          class="mb-5 rounded-xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm font-semibold text-amber-800"
        >
          当前工作区还没有可选项目，内部页面会展示示例数据。创建或修正项目数据后，顶部项目选择器会自动出现项目。
        </div>
        <RouterView />
      </div>
      <div v-else class="mx-auto max-w-[1660px] px-4 py-6 sm:px-7 lg:px-8">
        <RouterView />
      </div>
    </main>
  </div>
</template>
