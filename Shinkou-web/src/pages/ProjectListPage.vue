<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  FolderPlus,
  RefreshCw,
  Search,
  Trash2,
  UploadCloud,
} from "lucide-vue-next";
import { projectApi } from "@/api/project.api";
import type { Project } from "@/types/domain";
import EmptyState from "@/components/EmptyState.vue";
import StatusBadge from "@/components/StatusBadge.vue";
import UiSelect from "@/components/UiSelect.vue";
import UiModal from "@/components/UiModal.vue";
import { fmtDate } from "@/utils/format";

const route = useRoute();
const router = useRouter();
const workspaceId = computed(() => String(route.params.workspaceId));
const loading = ref(false);
const error = ref("");
const projects = ref<Project[]>([]);
const keyword = ref("");
const status = ref("ACTIVE");
const modalOpen = ref(false);
const saving = ref(false);
const form = reactive({ name: "", code: "", description: "" });
const statusOptions = [
  { label: "正常项目", value: "ACTIVE" },
  { label: "已归档", value: "ARCHIVED" },
  { label: "已删除", value: "DELETED" },
  { label: "全部状态", value: "" },
];

let timer: ReturnType<typeof setTimeout>;

watch([keyword, status], () => {
  clearTimeout(timer);
  timer = setTimeout(load, 300);
});

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    projects.value = await projectApi.list(workspaceId.value, {
      keyword: keyword.value || undefined,
      status: status.value || undefined,
    });
  } catch (err: any) {
    error.value = err.message || "项目列表获取失败";
  } finally {
    loading.value = false;
  }
}

async function createProject() {
  saving.value = true;
  try {
    const project = await projectApi.create(workspaceId.value, form);
    modalOpen.value = false;
    Object.assign(form, { name: "", code: "", description: "" });
    await load();
    router.push(
      `/workspaces/${workspaceId.value}/projects/${project.id}/overview`,
    );
  } catch (err: any) {
    alert(err.message || "创建失败");
  } finally {
    saving.value = false;
  }
}

async function removeProject(project: Project) {
  if (!confirm(`确认删除项目「${project.name}」吗？`)) return;
  await projectApi.remove(workspaceId.value, project.id);
  await load();
}
</script>

<template>
  <section class="space-y-6">
    <header
      class="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"
    >
      <div>
        <h1 class="text-3xl font-black text-slate-950">项目管理</h1>
        <p class="mt-2 max-w-2xl text-base text-slate-500">
          创建项目、上传代码包并建立文件索引。
        </p>
      </div>
      <button
        class="btn btn-primary sm:w-auto"
        type="button"
        @click="modalOpen = true"
      >
        <FolderPlus :size="18" />
        创建项目
      </button>
    </header>

    <div class="card grid gap-3 p-4 lg:grid-cols-[1fr_180px_auto]">
      <label class="relative block">
        <Search
          :size="17"
          class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
        />
        <input
          v-model="keyword"
          class="input pl-10"
          placeholder="搜索项目名称或 code"
        />
      </label>
      <UiSelect
        v-model="status"
        :options="statusOptions"
        aria-label="筛选项目状态"
      />
      <button class="btn btn-ghost lg:w-auto" type="button" @click="load">
        <RefreshCw :size="17" />
        刷新
      </button>
    </div>

    <div v-if="loading" class="card p-8 text-slate-500">正在加载项目...</div>
    <EmptyState v-else-if="error" title="项目加载失败" :description="error" />
    <EmptyState
      v-else-if="!projects.length"
      title="暂无项目"
      description="创建第一个项目后即可上传 ZIP 并发起影响分析。"
    >
      <button
        class="btn btn-primary mt-4 sm:w-auto"
        type="button"
        @click="modalOpen = true"
      >
        <FolderPlus :size="17" />
        创建项目
      </button>
    </EmptyState>

    <div v-else class="grid gap-5 lg:grid-cols-2 xl:grid-cols-3">
      <article
        v-for="project in projects"
        :key="project.id"
        class="card flex flex-col p-5"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <h2 class="truncate text-lg font-black">{{ project.name }}</h2>
            <p class="mono mt-1 truncate text-xs text-slate-500">
              {{ project.code }}
            </p>
          </div>
          <StatusBadge :status="project.status" />
        </div>

        <p
          class="mt-4 line-clamp-2 min-h-[44px] text-sm leading-6 text-slate-500"
        >
          {{ project.description || "暂无描述" }}
        </p>

        <div class="mt-5 grid grid-cols-2 gap-3 text-sm">
          <div class="rounded-lg bg-slate-50 p-3">
            <div class="text-slate-400">文件数</div>
            <div class="mt-1 font-black">{{ project.fileCount || 0 }}</div>
          </div>
          <div class="rounded-lg bg-slate-50 p-3">
            <div class="text-slate-400">更新时间</div>
            <div class="mt-1 truncate font-semibold">
              {{ fmtDate(project.updatedAt) }}
            </div>
          </div>
        </div>

        <div class="mt-5 flex flex-col gap-2 sm:flex-row">
          <button
            class="btn btn-primary flex-1"
            type="button"
            @click="
              router.push(
                `/workspaces/${workspaceId}/projects/${project.id}/overview`,
              )
            "
          >
            <UploadCloud :size="17" />
            进入项目
          </button>
          <button
            class="btn btn-ghost sm:w-auto"
            type="button"
            @click="removeProject(project)"
          >
            <Trash2 :size="17" />
            删除
          </button>
        </div>
      </article>
    </div>

    <UiModal :open="modalOpen" title="创建项目" @close="modalOpen = false">
      <form class="space-y-4" @submit.prevent="createProject">
        <div>
          <label class="label">项目名称</label>
          <input
            v-model="form.name"
            required
            class="input"
            placeholder="Order System Demo"
          />
        </div>
        <div>
          <label class="label">项目 code</label>
          <input
            v-model="form.code"
            required
            class="input"
            placeholder="order-system-demo"
          />
        </div>
        <div>
          <label class="label">项目描述</label>
          <textarea
            v-model="form.description"
            class="input min-h-28"
            placeholder="项目用途和业务背景"
          />
        </div>
        <div class="flex flex-col justify-end gap-3 sm:flex-row">
          <button
            type="button"
            class="btn btn-ghost sm:w-auto"
            @click="modalOpen = false"
          >
            取消
          </button>
          <button class="btn btn-primary sm:w-auto" :disabled="saving">
            {{ saving ? "创建中..." : "创建并进入" }}
          </button>
        </div>
      </form>
    </UiModal>
  </section>
</template>
