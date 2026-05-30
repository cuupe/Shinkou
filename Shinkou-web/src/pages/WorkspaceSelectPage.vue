<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ArrowRight, Building2, RefreshCw } from "lucide-vue-next";
import { useAuthStore } from "@/stores/auth.store";
import EmptyState from "@/components/EmptyState.vue";
import ShinkouLogo from "@/components/ShinkouLogo.vue";

const auth = useAuthStore();
const router = useRouter();
const loading = ref(false);
const error = ref("");

const activeWorkspaces = computed(() =>
  auth.workspaces.filter((workspace) => workspace.status === "ACTIVE"),
);

onMounted(async () => {
  if (!auth.workspaces.length) {
    await loadWorkspaces();
  }
});

async function loadWorkspaces() {
  loading.value = true;
  error.value = "";
  try {
    await auth.refreshWorkspaces();
  } catch (err: any) {
    error.value = err.message || "工作区获取失败";
  } finally {
    loading.value = false;
  }
}

function enter(id: number) {
  localStorage.setItem("lastWorkspaceId", String(id));
  router.push(`/workspaces/${id}/projects`);
}
</script>

<template>
  <main class="min-h-screen bg-slate-50 px-4 py-6 sm:px-6 lg:px-8">
    <section class="mx-auto max-w-6xl">
      <header
        class="mb-6 flex flex-col gap-4 sm:mb-8 sm:flex-row sm:items-end sm:justify-between"
      >
        <div>
          <ShinkouLogo class="mb-6" size="lg" />
          <div
            class="mb-4 inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-3 py-1 text-xs font-bold text-slate-600"
          >
            <Building2 :size="14" />
            工作区
          </div>
          <h1 class="text-2xl font-black tracking-normal sm:text-3xl">
            选择工作区
          </h1>
          <p
            class="mt-2 max-w-2xl text-sm leading-6 text-slate-500 sm:text-base"
          >
            项目、代码文件、智能分析和报告都归属于工作区。进入后会自动打开默认项目的内部工作台。
          </p>
        </div>
        <button
          class="btn btn-ghost sm:w-auto"
          type="button"
          @click="loadWorkspaces"
        >
          <RefreshCw :size="16" />
          刷新
        </button>
      </header>

      <div v-if="loading" class="card p-8 text-slate-500">
        正在加载工作区...
      </div>
      <EmptyState v-else-if="error" title="获取工作区失败" :description="error">
        <button
          class="btn btn-primary mt-4 sm:w-auto"
          type="button"
          @click="loadWorkspaces"
        >
          <RefreshCw :size="16" />
          重试
        </button>
      </EmptyState>
      <EmptyState
        v-else-if="!activeWorkspaces.length"
        title="暂无可用工作区"
        description="请联系管理员为你的企业邮箱创建邀请或启用工作区。"
      />
      <div v-else class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <button
          v-for="workspace in activeWorkspaces"
          :key="workspace.id"
          class="card group flex min-h-[180px] flex-col p-5 text-left transition hover:border-brand-500/40 hover:shadow-md"
          type="button"
          @click="enter(workspace.id)"
        >
          <div class="flex items-center justify-between gap-3">
            <div
              class="grid h-11 w-11 place-items-center rounded-lg bg-brand-50"
            >
              <Building2 :size="21" class="text-brand-700" />
            </div>
            <span
              class="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-600"
            >
              {{ workspace.role }}
            </span>
          </div>
          <h2 class="mt-5 text-lg font-black text-slate-950">
            {{ workspace.name }}
          </h2>
          <p class="mt-2 line-clamp-2 text-sm leading-6 text-slate-500">
            {{ workspace.description || workspace.code }}
          </p>
          <span
            class="mt-auto inline-flex items-center gap-2 pt-5 text-sm font-bold text-brand-700"
          >
            进入工作区
            <ArrowRight
              :size="16"
              class="transition group-hover:translate-x-0.5"
            />
          </span>
        </button>
      </div>
    </section>
  </main>
</template>
