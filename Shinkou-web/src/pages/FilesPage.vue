<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  BrainCircuit,
  Code2,
  Database,
  FileCode2,
  RefreshCw,
  Search,
  UploadCloud,
} from "lucide-vue-next";
import { fileApi } from "@/api/file.api";
import type {
  FileContent,
  FileSearchHit,
  FileTreeNode,
  ProjectFile,
} from "@/types/domain";
import FileTree from "@/components/FileTree.vue";
import EmptyState from "@/components/EmptyState.vue";
import { fmtSize } from "@/utils/format";

const route = useRoute();
const router = useRouter();
const wid = computed(() => String(route.params.workspaceId));
const pid = computed(() => String(route.params.projectId));

const files = ref<ProjectFile[]>([]);
const tree = ref<FileTreeNode[]>([]);
const activePath = ref("");
const content = ref<FileContent | null>(null);
const hits = ref<FileSearchHit[]>([]);
const keyword = ref("");
const loading = ref(false);
const uploading = ref(false);
const error = ref("");

const indexedCount = computed(
  () => files.value.filter((file) => file.indexed).length,
);
const languageCount = computed(
  () => new Set(files.value.map((file) => file.language).filter(Boolean)).size,
);

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const [fileList, fileTree] = await Promise.all([
      fileApi.list(wid.value, pid.value),
      fileApi.tree(wid.value, pid.value).catch(() => []),
    ]);
    files.value = fileList;
    tree.value = fileTree;
  } catch (err: any) {
    error.value = err.message || "文件加载失败";
  } finally {
    loading.value = false;
  }
}

async function upload(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  uploading.value = true;
  try {
    await fileApi.uploadZip(wid.value, pid.value, file);
    await load();
  } catch (err: any) {
    alert(err.message || "上传失败");
  } finally {
    uploading.value = false;
    input.value = "";
  }
}

async function openPath(path: string) {
  activePath.value = path;
  try {
    content.value = await fileApi.content(wid.value, pid.value, path);
  } catch (err: any) {
    alert(err.message || "读取文件失败");
  }
}

async function search() {
  const value = keyword.value.trim();
  if (!value) return;
  hits.value = await fileApi.search(wid.value, pid.value, value);
}

function goAnalysis() {
  router.push(`/workspaces/${wid.value}/projects/${pid.value}/analysis`);
}
</script>

<template>
  <section class="space-y-5">
    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div class="card p-5">
        <div class="flex items-center justify-between gap-3">
          <div>
            <div class="text-sm font-semibold text-slate-500">项目文件</div>
            <div class="mt-2 text-3xl font-black text-slate-950">
              {{ files.length }}
            </div>
          </div>
          <div
            class="grid h-12 w-12 place-items-center rounded-xl bg-brand-50 text-brand-600"
          >
            <FileCode2 :size="24" />
          </div>
        </div>
      </div>
      <div class="card p-5">
        <div class="flex items-center justify-between gap-3">
          <div>
            <div class="text-sm font-semibold text-slate-500">已索引</div>
            <div class="mt-2 text-3xl font-black text-slate-950">
              {{ indexedCount }}
            </div>
          </div>
          <div
            class="grid h-12 w-12 place-items-center rounded-xl bg-emerald-50 text-emerald-600"
          >
            <Database :size="24" />
          </div>
        </div>
      </div>
      <div class="card p-5">
        <div class="flex items-center justify-between gap-3">
          <div>
            <div class="text-sm font-semibold text-slate-500">语言数</div>
            <div class="mt-2 text-3xl font-black text-slate-950">
              {{ languageCount }}
            </div>
          </div>
          <div
            class="grid h-12 w-12 place-items-center rounded-xl bg-indigo-50 text-indigo-600"
          >
            <Code2 :size="24" />
          </div>
        </div>
      </div>
      <div
        class="card flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center xl:flex-col xl:items-stretch"
      >
        <div>
          <div class="text-sm font-semibold text-slate-500">上传代码 ZIP</div>
          <div class="mt-2 text-sm font-bold text-slate-700">
            {{ uploading ? "上传处理中..." : "支持重新索引" }}
          </div>
        </div>
        <label class="btn btn-primary cursor-pointer">
          <UploadCloud :size="18" />
          <input type="file" class="hidden" accept=".zip" @change="upload" />
          上传
        </label>
      </div>
    </div>

    <div class="card p-4">
      <div class="grid gap-3 lg:grid-cols-[1fr_auto_auto_auto]">
        <div class="relative">
          <Search
            :size="18"
            class="pointer-events-none absolute left-3 top-1/2 z-10 -translate-y-1/2 text-slate-400"
          />
          <input
            v-model="keyword"
            class="input !pl-10"
            placeholder="搜索代码内容，例如 coupon / createOrder / payAmount"
            @keyup.enter="search"
          />
        </div>
        <button class="btn btn-primary" @click="search">
          <Search :size="18" />
          搜索
        </button>
        <button class="btn btn-ghost" @click="load">
          <RefreshCw :size="18" />
          刷新索引
        </button>
        <button class="btn btn-soft" @click="goAnalysis">
          <BrainCircuit :size="18" />
          发起需求分析
        </button>
      </div>

      <div v-if="hits.length" class="mt-4 grid gap-3">
        <button
          v-for="hit in hits"
          :key="hit.path + hit.line"
          class="rounded-xl border border-slate-200 p-3 text-left hover:border-brand-300 hover:bg-brand-50"
          @click="openPath(hit.path)"
        >
          <div class="mono break-all text-sm font-bold text-brand-700">
            {{ hit.path }}:{{ hit.line }}
          </div>
          <div class="mono mt-1 break-all text-sm text-slate-600">
            {{ hit.snippet }}
          </div>
        </button>
      </div>
    </div>

    <div v-if="loading" class="card p-8 text-slate-500">
      正在加载文件索引...
    </div>
    <EmptyState v-else-if="error" title="文件加载失败" :description="error" />
    <div v-else class="grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)]">
      <aside class="card max-h-[720px] overflow-auto p-4 scrollbar-thin">
        <h3 class="mb-3 text-base font-black text-slate-950">文件树</h3>
        <FileTree
          v-if="tree.length"
          :nodes="tree"
          :active-path="activePath"
          @select="openPath($event.path)"
        />
        <div
          v-else
          class="rounded-xl border border-dashed border-slate-200 p-4 text-sm text-slate-500"
        >
          暂无文件树，先上传项目 ZIP。
        </div>

        <h3 class="mb-3 mt-6 text-base font-black text-slate-950">文件列表</h3>
        <div class="space-y-1">
          <button
            v-for="file in files.slice(0, 200)"
            :key="file.id"
            class="w-full rounded-lg px-2 py-2 text-left text-sm hover:bg-slate-100"
            :class="
              activePath === file.filePath
                ? 'bg-brand-50 font-semibold text-brand-700'
                : ''
            "
            @click="openPath(file.filePath)"
          >
            <div class="mono truncate">{{ file.filePath }}</div>
            <div class="mt-0.5 text-xs text-slate-400">
              {{ file.language || file.fileExt || "-" }} ·
              {{ fmtSize(file.size) }}
            </div>
          </button>
        </div>
      </aside>

      <main class="card min-w-0 overflow-hidden">
        <div class="border-b border-slate-200 px-5 py-4">
          <h3 class="break-all font-black text-slate-950">
            {{ activePath || "文件内容预览" }}
          </h3>
          <p class="mt-1 text-xs text-slate-500">
            选择文件后读取完整内容。需要精确片段时可在后端使用 read-lines 接口。
          </p>
        </div>
        <pre
          v-if="content"
          class="max-h-[700px] overflow-auto bg-slate-950 p-5 text-sm leading-6 text-slate-100 scrollbar-thin"
        ><code>{{ content.content }}</code></pre>
        <EmptyState
          v-else
          title="未选择文件"
          description="从左侧文件树或搜索结果中选择一个文件查看内容。"
        />
      </main>
    </div>
  </section>
</template>
