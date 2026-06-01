<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import {
  Bell,
  Bot,
  Building2,
  Check,
  ChevronDown,
  ChevronsLeft,
  FileCode2,
  FileText,
  Folder,
  Gauge,
  Home,
  ListChecks,
  LogOut,
  Menu,
  RefreshCw,
  Search,
  Settings,
  Sparkles,
  UserRound,
  X,
} from "lucide-vue-next";
import ShinkouLogo from "@/components/ShinkouLogo.vue";
import UiSelect from "@/components/UiSelect.vue";
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
const shellRoot = ref<HTMLElement | null>(null);
const activePopover = ref<"model" | "notifications" | "user" | null>(null);
const projectLoadError = ref("");
const loggingOut = ref(false);
const selectedModel = ref(
  localStorage.getItem("shinkou:selected-model") || "gpt-4o",
);
const notifications = ref([
  {
    id: "n1",
    title: "项目索引已完成",
    description: "Order System Demo 新增 42 个文件，索引状态正常。",
    time: "10 分钟前",
    read: false,
  },
  {
    id: "n2",
    title: "分析报告待查看",
    description: "优惠券影响分析已生成报告，可进入报告中心查看。",
    time: "今天 09:42",
    read: false,
  },
  {
    id: "n3",
    title: "模型配置建议检查",
    description: "当前聊天模型使用默认参数，可在模型配置中调整。",
    time: "昨天 18:20",
    read: true,
  },
]);

const modelOptions = [
  {
    label: "GPT-4o",
    value: "gpt-4o",
    description: "综合分析、代码理解与报告生成",
  },
  {
    label: "GPT-4o mini",
    value: "gpt-4o-mini",
    description: "轻量任务、快速摘要与交互预览",
  },
  {
    label: "Claude 3.5 Sonnet",
    value: "claude-3-5-sonnet",
    description: "长上下文审阅与复杂变更推理",
  },
];

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
const projectOptions = computed(() =>
  projects.value.map((project) => ({
    label: project.name,
    value: String(project.id),
    description: project.code,
  })),
);
const selectedModelInfo = computed(
  () =>
    modelOptions.find((model) => model.value === selectedModel.value) ||
    modelOptions[0],
);
const unreadCount = computed(
  () => notifications.value.filter((item) => !item.read).length,
);
const currentWorkspace = computed(() =>
  auth.workspaces.find((workspace) => String(workspace.id) === workspaceId.value),
);
const userRoleLabel = computed(() => {
  const role = currentWorkspace.value?.role || "MEMBER";
  const roleMap: Record<string, string> = {
    OWNER: "所有者",
    ADMIN: "系统管理员",
    MEMBER: "成员",
    VIEWER: "观察者",
  };
  return roleMap[role] || role;
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
const isCodeIdeRoute = computed(() => route.path.endsWith("/code"));
const searchText = ref("");

onMounted(() => {
  loadProjects();
  window.addEventListener("pointerdown", closePopoverOnOutsideClick);
});
onBeforeUnmount(() => {
  window.removeEventListener("pointerdown", closePopoverOnOutsideClick);
});
watch(workspaceId, loadProjects);
watch(
  () => route.fullPath,
  () => {
    activePopover.value = null;
    mobileOpen.value = false;
  },
);

async function loadProjects() {
  if (!workspaceId.value) return;
  projectLoadError.value = "";
  projects.value = await projectApi.list(workspaceId.value).catch((err) => {
    projectLoadError.value =
      err.message ||
      "项目列表获取失败，请确认后端服务已启动并检查 Vite 代理配置。";
    return [];
  });

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

function selectProject(value: string | number) {
  const nextProjectId = String(value);
  if (!nextProjectId) {
    router.push(`/workspaces/${workspaceId.value}/projects`);
    return;
  }
  localStorage.setItem(`lastProjectId:${workspaceId.value}`, nextProjectId);
  router.push(
    `/workspaces/${workspaceId.value}/projects/${nextProjectId}/overview`,
  );
}

function closePopoverOnOutsideClick(event: PointerEvent) {
  if (!shellRoot.value?.contains(event.target as Node)) return;
  const target = event.target as HTMLElement;
  if (!target.closest("[data-popover-root]")) {
    activePopover.value = null;
  }
}

function togglePopover(name: "model" | "notifications" | "user") {
  activePopover.value = activePopover.value === name ? null : name;
}

function chooseModel(value: string) {
  selectedModel.value = value;
  localStorage.setItem("shinkou:selected-model", value);
  activePopover.value = null;
}

function markAllNotificationsRead() {
  notifications.value = notifications.value.map((item) => ({
    ...item,
    read: true,
  }));
}

function runGlobalSearch() {
  const query = searchText.value.trim();
  if (!query) return;
  router.push({
    path: `${projectBase.value}/code`,
    query: { q: query },
  });
}

async function logout() {
  if (loggingOut.value) return;
  loggingOut.value = true;
  try {
    await auth.logout();
    activePopover.value = null;
    router.push("/login");
  } catch (err: any) {
    alert(err.message || "退出登录失败，请稍后重试");
  } finally {
    loggingOut.value = false;
  }
}
</script>

<template>
  <div ref="shellRoot" class="min-h-screen bg-[#f8fbff] text-slate-950">
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
          class="flex h-12 min-w-0 items-center gap-3 rounded-lg px-4 text-[15px] font-bold text-slate-700 hover:bg-slate-50"
          active-class="!bg-[#edf3ff] !text-brand-600"
        >
          <component :is="item.icon" :size="21" class="shrink-0" />
          <span class="one-line">{{ item.label }}</span>
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
      <div class="flex h-[76px] min-w-0 items-center gap-2 px-3 sm:px-5 xl:gap-4 xl:px-6">
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

        <UiSelect
          class="hidden w-[220px] shrink-0 xl:block 2xl:w-[260px]"
          :model-value="selectedProjectId"
          :options="projectOptions"
          placeholder="选择项目"
          aria-label="选择项目"
          @change="selectProject"
        />

        <form
          class="relative mx-auto hidden min-w-[180px] max-w-[420px] flex-1 2xl:block 2xl:max-w-[500px]"
          @submit.prevent="runGlobalSearch"
        >
          <Search
            :size="19"
            class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
          />
          <input
            v-model="searchText"
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
        </form>

        <div class="ml-auto flex min-w-0 shrink-0 items-center gap-1.5 sm:gap-2 xl:gap-3">
          <div class="relative hidden xl:block" data-popover-root>
            <button
              class="flex h-10 max-w-[190px] items-center gap-2 rounded-full border border-slate-200 bg-white px-4 text-sm font-bold text-slate-900 transition hover:border-brand-200 hover:bg-brand-50"
              type="button"
              :aria-expanded="activePopover === 'model'"
              @click="togglePopover('model')"
            >
              <Sparkles :size="17" class="text-brand-600" />
              <span class="one-line">模型：{{ selectedModelInfo.label }}</span>
              <ChevronDown
                :size="15"
                class="text-slate-500 transition"
                :class="activePopover === 'model' ? 'rotate-180' : ''"
              />
            </button>
            <Transition
              enter-active-class="transition duration-150 ease-out"
              enter-from-class="-translate-y-1 opacity-0"
              enter-to-class="translate-y-0 opacity-100"
              leave-active-class="transition duration-100 ease-in"
              leave-from-class="translate-y-0 opacity-100"
              leave-to-class="-translate-y-1 opacity-0"
            >
              <div
                v-if="activePopover === 'model'"
                class="absolute right-0 top-12 z-[80] w-[min(calc(100vw-1rem),330px)] rounded-lg border border-slate-200 bg-white p-2 shadow-[0_18px_42px_rgba(15,23,42,0.14)]"
              >
                <div class="px-3 py-2">
                  <p class="text-sm font-black text-slate-900">选择当前模型</p>
                  <p class="mt-1 text-xs font-medium text-slate-500">
                    该选择会影响顶部工作台交互，详细参数在模型配置中维护。
                  </p>
                </div>
                <button
                  v-for="model in modelOptions"
                  :key="model.value"
                  class="flex w-full items-start gap-3 rounded-md px-3 py-2.5 text-left transition hover:bg-slate-50"
                  type="button"
                  @click="chooseModel(model.value)"
                >
                  <span
                    class="mt-0.5 grid h-5 w-5 place-items-center rounded-full border"
                    :class="
                      selectedModel === model.value
                        ? 'border-brand-600 bg-brand-600 text-white'
                        : 'border-slate-300 text-transparent'
                    "
                  >
                    <Check :size="13" />
                  </span>
                  <span class="min-w-0 flex-1">
                    <span class="block text-sm font-bold text-slate-900">
                      {{ model.label }}
                    </span>
                    <span class="mt-0.5 block text-xs leading-5 text-slate-500">
                      {{ model.description }}
                    </span>
                  </span>
                </button>
                <RouterLink
                  class="mt-1 flex items-center gap-2 rounded-md border-t border-slate-100 px-3 py-2.5 text-sm font-bold text-brand-600 hover:bg-brand-50"
                  :to="`/workspaces/${workspaceId}/settings/models`"
                >
                  <Settings :size="16" />
                  打开模型配置
                </RouterLink>
              </div>
            </Transition>
          </div>
          <div class="relative hidden sm:block" data-popover-root>
            <button
              class="relative grid h-10 w-10 place-items-center rounded-full text-slate-700 transition hover:bg-slate-100"
              type="button"
              :aria-expanded="activePopover === 'notifications'"
              @click="togglePopover('notifications')"
            >
              <Bell :size="20" />
              <span
                v-if="unreadCount"
                class="absolute right-2 top-2 h-2.5 w-2.5 rounded-full bg-red-500 ring-2 ring-white"
              />
            </button>
            <Transition
              enter-active-class="transition duration-150 ease-out"
              enter-from-class="-translate-y-1 opacity-0"
              enter-to-class="translate-y-0 opacity-100"
              leave-active-class="transition duration-100 ease-in"
              leave-from-class="translate-y-0 opacity-100"
              leave-to-class="-translate-y-1 opacity-0"
            >
              <div
                v-if="activePopover === 'notifications'"
                class="absolute right-0 top-12 z-[80] w-[min(calc(100vw-1rem),360px)] rounded-lg border border-slate-200 bg-white shadow-[0_18px_42px_rgba(15,23,42,0.14)]"
              >
                <div
                  class="flex items-center justify-between border-b border-slate-100 px-4 py-3"
                >
                  <div>
                    <p class="text-sm font-black text-slate-900">通知中心</p>
                    <p class="mt-0.5 text-xs text-slate-500">
                      {{ unreadCount ? `${unreadCount} 条未读` : "暂无未读通知" }}
                    </p>
                  </div>
                  <button
                    class="text-xs font-bold text-brand-600 disabled:text-slate-300"
                    type="button"
                    :disabled="!unreadCount"
                    @click="markAllNotificationsRead"
                  >
                    全部已读
                  </button>
                </div>
                <div class="max-h-[320px] overflow-y-auto p-2 scrollbar-thin">
                  <article
                    v-for="item in notifications"
                    :key="item.id"
                    class="rounded-md px-3 py-3"
                    :class="item.read ? 'bg-white' : 'bg-brand-50/70'"
                  >
                    <div class="flex items-start gap-3">
                      <span
                        class="mt-1 h-2 w-2 rounded-full"
                        :class="item.read ? 'bg-slate-300' : 'bg-red-500'"
                      />
                      <div class="min-w-0 flex-1">
                        <p class="text-sm font-bold text-slate-900">
                          {{ item.title }}
                        </p>
                        <p class="mt-1 text-xs leading-5 text-slate-500">
                          {{ item.description }}
                        </p>
                        <p class="mt-2 text-xs font-semibold text-slate-400">
                          {{ item.time }}
                        </p>
                      </div>
                    </div>
                  </article>
                </div>
              </div>
            </Transition>
          </div>
          <div class="relative" data-popover-root>
            <button
              class="flex max-w-[180px] items-center gap-2 rounded-full py-1 pl-1 pr-2 transition hover:bg-slate-100"
              type="button"
              :aria-expanded="activePopover === 'user'"
              @click="togglePopover('user')"
            >
              <div
                class="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-brand-500 text-sm font-black text-white"
              >
                {{ userInitial }}
              </div>
              <span
                class="hidden max-w-[120px] truncate text-sm font-bold text-slate-900 sm:inline"
              >
                {{ auth.user?.name || auth.user?.email || "Shinkou User" }}
              </span>
              <ChevronDown
                :size="16"
                class="hidden text-slate-500 transition sm:block"
                :class="activePopover === 'user' ? 'rotate-180' : ''"
              />
            </button>
            <Transition
              enter-active-class="transition duration-150 ease-out"
              enter-from-class="-translate-y-1 opacity-0"
              enter-to-class="translate-y-0 opacity-100"
              leave-active-class="transition duration-100 ease-in"
              leave-from-class="translate-y-0 opacity-100"
              leave-to-class="-translate-y-1 opacity-0"
            >
              <div
                v-if="activePopover === 'user'"
                class="absolute right-0 top-12 z-[80] w-[min(calc(100vw-1rem),280px)] overflow-hidden rounded-lg border border-slate-200 bg-white shadow-[0_18px_42px_rgba(15,23,42,0.14)]"
              >
                <div class="border-b border-slate-100 px-4 py-4">
                  <div class="flex items-center gap-3">
                    <div
                      class="grid h-11 w-11 place-items-center rounded-full bg-brand-500 text-base font-black text-white"
                    >
                      {{ userInitial }}
                    </div>
                    <div class="min-w-0">
                      <p class="truncate text-sm font-black text-slate-900">
                        {{ auth.user?.name || "Shinkou User" }}
                      </p>
                      <p class="mt-0.5 truncate text-xs text-slate-500">
                        {{ auth.user?.email || "未绑定邮箱" }}
                      </p>
                    </div>
                  </div>
                  <div
                    class="mt-3 inline-flex items-center gap-2 rounded-md bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-600"
                  >
                    <UserRound :size="13" />
                    {{ userRoleLabel }}
                  </div>
                </div>
                <div class="p-2">
                  <RouterLink
                    class="flex items-center gap-2 rounded-md px-3 py-2.5 text-sm font-bold text-slate-700 hover:bg-slate-50"
                    to="/workspace-select"
                  >
                    <Building2 :size="16" />
                    切换工作区
                  </RouterLink>
                  <button
                    class="flex w-full items-center gap-2 rounded-md px-3 py-2.5 text-left text-sm font-bold text-slate-700 hover:bg-slate-50"
                    type="button"
                    @click="loadProjects"
                  >
                    <RefreshCw :size="16" />
                    刷新项目
                  </button>
                  <RouterLink
                    class="flex items-center gap-2 rounded-md px-3 py-2.5 text-sm font-bold text-slate-700 hover:bg-slate-50"
                    :to="`/workspaces/${workspaceId}/settings/models`"
                  >
                    <Settings :size="16" />
                    模型配置
                  </RouterLink>
                </div>
                <div class="border-t border-slate-100 p-2">
                  <button
                    class="flex w-full items-center gap-2 rounded-md px-3 py-2.5 text-left text-sm font-bold text-red-600 hover:bg-red-50"
                    type="button"
                    :disabled="loggingOut"
                    @click="logout"
                  >
                    <LogOut :size="16" />
                    {{ loggingOut ? "退出中..." : "退出登录" }}
                  </button>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>

      <nav
        v-if="mobileOpen"
        class="border-t border-slate-100 bg-white p-3 lg:hidden"
      >
        <UiSelect
          class="mb-3"
          :model-value="selectedProjectId"
          :options="projectOptions"
          placeholder="选择项目"
          aria-label="选择项目"
          @change="selectProject"
        />
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex min-w-0 items-center gap-3 rounded-lg px-3 py-3 text-sm font-bold text-slate-700"
          active-class="bg-brand-50 text-brand-700"
          @click="mobileOpen = false"
        >
          <component :is="item.icon" :size="18" class="shrink-0" />
          <span class="one-line">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </header>

    <main class="lg:pl-[240px] lg:pt-[76px]">
      <div
        v-if="
          !hasSelectedProject &&
          route.path !== `/workspaces/${workspaceId}/projects`
        "
        :class="
          isCodeIdeRoute
            ? 'h-[calc(100vh-76px)] overflow-hidden'
            : 'mx-auto max-w-[1660px] px-4 py-6 sm:px-7 lg:px-8'
        "
      >
        <div
          v-if="!isCodeIdeRoute"
          class="mb-5 rounded-xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm font-semibold text-amber-800"
        >
          当前工作区还没有可选项目，内部页面会展示示例数据。创建或修正项目数据后，顶部项目选择器会自动出现项目。
        </div>
        <div
          v-if="projectLoadError && !isCodeIdeRoute"
          class="mb-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700"
        >
          {{ projectLoadError }}
        </div>
        <RouterView />
      </div>
      <div
        v-else
        :class="
          isCodeIdeRoute
            ? 'h-[calc(100vh-76px)] overflow-hidden'
            : 'mx-auto max-w-[1660px] px-4 py-6 sm:px-7 lg:px-8'
        "
      >
        <div
          v-if="projectLoadError && !isCodeIdeRoute"
          class="mb-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700"
        >
          {{ projectLoadError }}
        </div>
        <RouterView />
      </div>
    </main>
  </div>
</template>
