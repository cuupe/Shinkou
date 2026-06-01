<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import {
  Box,
  CheckCircle2,
  Edit3,
  Layers,
  MessageCircle,
  Play,
  RefreshCw,
  SlidersHorizontal,
  Trash2,
} from "lucide-vue-next";
import { modelConfigApi } from "@/api/modelConfig.api";
import UiSelect from "@/components/UiSelect.vue";
import type { ModelConfig, ToolConfig } from "@/types/domain";

const route = useRoute();
const wid = computed(() => String(route.params.workspaceId));
const configs = ref<ModelConfig[]>([]);
const tools = ref<ToolConfig[]>([]);
const loading = ref(false);
const testing = ref(false);
const chatProviderOptions = [
  { label: "OpenAI", value: "OpenAI" },
  { label: "Azure OpenAI", value: "Azure OpenAI" },
];
const permissionOptions = [
  { label: "只读", value: "READ" },
  { label: "写入", value: "WRITE" },
];

const form = reactive<ModelConfig>({
  type: "CHAT",
  provider: "OpenAI",
  model: "gpt-4o",
  temperature: 0.2,
  maxTokens: 4096,
  timeoutSeconds: 60,
  enabled: true,
});

const chatConfig = computed(() =>
  configs.value.find((item) => item.type === "CHAT"),
);
const embeddingConfig = computed(() =>
  configs.value.find((item) => item.type === "EMBEDDING"),
);
const rerankerConfig = computed(() =>
  configs.value.find((item) => item.type === "RERANKER"),
);
const embeddingProviderOptions = computed(() =>
  singleOption(embeddingConfig.value?.provider || "OpenAI"),
);
const embeddingModelOptions = computed(() =>
  singleOption(embeddingConfig.value?.model || "text-embedding-3-large"),
);
const rerankerProviderOptions = computed(() =>
  singleOption(rerankerConfig.value?.provider || "Jina AI"),
);
const rerankerModelOptions = computed(() =>
  singleOption(
    rerankerConfig.value?.model || "jina-reranker-v2-base-multilingual",
  ),
);

const fallbackTools = computed<ToolConfig[]>(() =>
  tools.value.length
    ? tools.value
    : [
        {
          name: "list_project_tree",
          description: "列出项目目录结构与文件树",
          permissionLevel: "READ",
          enabled: true,
          requireConfirm: false,
        },
        {
          name: "search_text",
          description: "在项目中搜索关键词原文文本",
          permissionLevel: "READ",
          enabled: true,
          requireConfirm: true,
        },
        {
          name: "read_file_lines",
          description: "按行读取文件内容（支持范围）",
          permissionLevel: "READ",
          enabled: true,
          requireConfirm: true,
        },
        {
          name: "generate_pdf_report",
          description: "生成分析报告（PDF）",
          permissionLevel: "WRITE",
          enabled: true,
          requireConfirm: true,
        },
        {
          name: "create_task_draft",
          description: "创建任务草稿（待人工发布）",
          permissionLevel: "WRITE",
          enabled: true,
          requireConfirm: true,
        },
      ],
);

function singleOption(value: string | number) {
  return [{ label: String(value), value: String(value) }];
}

function permissionValue(value?: string) {
  if (value === "写入") return "WRITE";
  if (value === "只读") return "READ";
  return value || "READ";
}

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const [configList, toolList] = await Promise.all([
      modelConfigApi.list(wid.value).catch(() => []),
      modelConfigApi.tools(wid.value).catch(() => []),
    ]);
    configs.value = configList;
    tools.value = toolList;
  } finally {
    loading.value = false;
  }
}

async function save() {
  await modelConfigApi.create(wid.value, form);
  await load();
}

async function toggleTool(tool: ToolConfig) {
  if (!tool.id) {
    tool.enabled = !tool.enabled;
    return;
  }
  await modelConfigApi.updateTool(wid.value, tool.id, {
    enabled: !tool.enabled,
  });
  await load();
}

async function runTest() {
  testing.value = true;
  try {
    const first = configs.value.find((item) => item.id);
    if (first?.id) await modelConfigApi.test(wid.value, first.id);
  } finally {
    testing.value = false;
  }
}
</script>

<template>
  <section class="space-y-6">
    <header>
      <h1 class="text-3xl font-black text-slate-950">模型配置</h1>
      <p class="mt-2 text-base text-slate-500">
        配置聊天、向量嵌入、重排序模型及工具，管理密钥与参数。
      </p>
    </header>

    <div v-if="loading" class="card p-8 text-slate-500">正在加载配置...</div>

    <div
      class="grid gap-5 lg:grid-cols-2 2xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_360px]"
    >
      <section class="card p-5">
        <h2 class="mb-5 flex min-w-0 items-center gap-2 text-lg font-black">
          <MessageCircle :size="21" class="shrink-0 text-brand-600" />
          <span class="one-line">聊天模型（Chat Model）</span>
        </h2>
        <div class="space-y-4">
          <label class="block">
            <span class="label">提供商</span>
            <UiSelect
              v-model="form.provider"
              :options="chatProviderOptions"
              aria-label="选择聊天模型提供商"
            />
          </label>
          <label class="block">
            <span class="label">模型</span>
            <input
              v-model="form.model"
              class="input"
              :placeholder="chatConfig?.model || 'gpt-4o'"
            />
          </label>
          <label class="block">
            <span class="label">温度（Temperature）</span>
            <div class="grid grid-cols-[1fr_64px] gap-3">
              <input
                v-model.number="form.temperature"
                class="accent-brand-600"
                type="range"
                min="0"
                max="2"
                step="0.1"
              />
              <input
                v-model.number="form.temperature"
                class="input h-10 min-h-10 text-center"
                type="number"
                step="0.1"
              />
            </div>
          </label>
          <label class="block">
            <span class="label">最大输出 Token（Max Tokens）</span>
            <input
              v-model.number="form.maxTokens"
              class="input"
              type="number"
            />
          </label>
          <label class="block">
            <span class="label">请求超时（秒）</span>
            <input
              v-model.number="form.timeoutSeconds"
              class="input"
              type="number"
            />
          </label>
          <button
            class="flex items-center gap-2 text-sm font-bold text-brand-600"
          >
            高级选项
            <SlidersHorizontal :size="16" />
          </button>
        </div>
      </section>

      <section class="card p-5">
        <h2 class="mb-5 flex min-w-0 items-center gap-2 text-lg font-black">
          <Layers :size="21" class="shrink-0 text-brand-600" />
          <span class="one-line">向量模型（Embedding Model）</span>
        </h2>
        <div class="space-y-4">
          <label class="block">
            <span class="label">提供商</span>
            <UiSelect
              :model-value="embeddingConfig?.provider || 'OpenAI'"
              :options="embeddingProviderOptions"
              aria-label="向量模型提供商"
            />
          </label>
          <label class="block">
            <span class="label">模型</span>
            <UiSelect
              :model-value="embeddingConfig?.model || 'text-embedding-3-large'"
              :options="embeddingModelOptions"
              aria-label="向量模型"
            />
          </label>
          <label class="block">
            <span class="label">维度（维度将自动识别）</span>
            <input
              class="input bg-slate-50"
              :value="embeddingConfig?.dimension || 3072"
              readonly
            />
          </label>
          <label class="block">
            <span class="label">请求超时（秒）</span>
            <input
              class="input"
              :value="embeddingConfig?.timeoutSeconds || 45"
            />
          </label>
          <button
            class="flex items-center gap-2 text-sm font-bold text-brand-600"
          >
            高级选项
            <SlidersHorizontal :size="16" />
          </button>
        </div>
      </section>

      <section class="card p-5">
        <h2 class="mb-5 flex min-w-0 items-center gap-2 text-lg font-black">
          <SlidersHorizontal :size="21" class="shrink-0 text-brand-600" />
          <span class="one-line">重排序模型（Reranker Model）</span>
        </h2>
        <div class="space-y-4">
          <label class="block">
            <span class="label">提供商</span>
            <UiSelect
              :model-value="rerankerConfig?.provider || 'Jina AI'"
              :options="rerankerProviderOptions"
              aria-label="重排序模型提供商"
            />
          </label>
          <label class="block">
            <span class="label">模型</span>
            <UiSelect
              :model-value="
                rerankerConfig?.model || 'jina-reranker-v2-base-multilingual'
              "
              :options="rerankerModelOptions"
              aria-label="重排序模型"
            />
          </label>
          <label class="block">
            <span class="label">Top K（候选数）</span>
            <input
              class="input text-right"
              :value="rerankerConfig?.topK || 50"
            />
          </label>
          <label class="block">
            <span class="label">Rerank Top N（重排返回数）</span>
            <input
              class="input text-right"
              :value="rerankerConfig?.rerankTopN || 10"
            />
          </label>
          <label class="block">
            <span class="label">请求超时（秒）</span>
            <input
              class="input text-right"
              :value="rerankerConfig?.timeoutSeconds || 60"
            />
          </label>
        </div>
      </section>

      <aside class="card p-5">
        <div class="mb-5 flex min-w-0 items-center justify-between gap-3">
          <h2 class="one-line text-lg font-black">连通性检查</h2>
          <button
            class="flex shrink-0 items-center gap-2 text-sm font-bold text-slate-500"
            @click="load"
          >
            <RefreshCw :size="16" />
            刷新
          </button>
        </div>
        <div class="space-y-5">
          <div
            v-for="item in [
              ['聊天模型', 'OpenAI / gpt-4o', '612ms'],
              ['向量模型', 'OpenAI / text-embedding-3-large', '428ms'],
              [
                '重排序模型',
                'Jina / jina-reranker-v2-base-multilingual',
                '756ms',
              ],
              ['工具服务', '', '132ms'],
            ]"
            :key="item[0]"
            class="flex min-w-0 items-start justify-between gap-3"
          >
            <div class="flex min-w-0 gap-3">
              <span
                class="grid h-10 w-10 place-items-center rounded-full bg-slate-100 text-slate-600"
              >
                <Box :size="18" />
              </span>
              <div class="min-w-0">
                <p class="font-bold leading-6">
                  {{ item[0] }}
                  <span v-if="item[1]" class="text-slate-500 break-words"
                    >（{{ item[1] }}）</span
                  >
                </p>
                <p class="mt-1 text-sm text-slate-500">延迟：{{ item[2] }}</p>
              </div>
            </div>
            <span
              class="rounded-md bg-emerald-50 px-3 py-1 text-sm font-bold text-emerald-600"
              >成功</span
            >
          </div>
        </div>
        <button
          class="btn btn-primary mt-7 w-full"
          :disabled="testing"
          @click="runTest"
        >
          <Play :size="18" />
          {{ testing ? "检查中..." : "一键连通性检查" }}
        </button>
        <p class="mt-3 text-center text-sm text-slate-500">
          上次检查：2025-05-20 10:21:38
        </p>
      </aside>
    </div>

    <section class="card overflow-hidden">
      <div class="border-b border-slate-100 px-5 py-4">
        <h2 class="flex min-w-0 items-center gap-2 text-lg font-black">
          <SlidersHorizontal :size="20" class="shrink-0 text-brand-600" />
          <span class="one-line">工具配置</span>
        </h2>
        <p class="mt-1 text-sm text-slate-500">
          配置可用工具及其权限与使用策略。
        </p>
      </div>
      <div class="table-wrap rounded-none border-0">
        <table class="min-w-[920px] w-full text-sm">
          <thead>
            <tr>
              <th class="table-th">工具名称</th>
              <th class="table-th">描述</th>
              <th class="table-th">权限等级</th>
              <th class="table-th">是否启用</th>
              <th class="table-th">是否需确认</th>
              <th class="table-th">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tool in fallbackTools" :key="tool.id || tool.name">
              <td class="table-td mono font-bold">{{ tool.name }}</td>
              <td class="table-td">{{ tool.description }}</td>
              <td class="table-td">
                <UiSelect
                  class="w-28"
                  :model-value="permissionValue(tool.permissionLevel)"
                  :options="permissionOptions"
                  size="sm"
                  aria-label="选择工具权限等级"
                  @change="tool.permissionLevel = String($event)"
                />
              </td>
              <td class="table-td">
                <button
                  class="h-6 w-11 rounded-full p-0.5 transition"
                  :class="tool.enabled ? 'bg-brand-600' : 'bg-slate-300'"
                  @click="toggleTool(tool)"
                >
                  <span
                    class="block h-5 w-5 rounded-full bg-white transition"
                    :class="tool.enabled ? 'translate-x-5' : ''"
                  />
                </button>
              </td>
              <td class="table-td">
                <button
                  class="h-6 w-11 rounded-full p-0.5 transition"
                  :class="tool.requireConfirm ? 'bg-brand-600' : 'bg-slate-300'"
                >
                  <span
                    class="block h-5 w-5 rounded-full bg-white transition"
                    :class="tool.requireConfirm ? 'translate-x-5' : ''"
                  />
                </button>
              </td>
              <td class="table-td">
                <div class="flex gap-4">
                  <button
                    class="inline-flex items-center gap-1 font-bold text-brand-600"
                  >
                    <Edit3 :size="15" />
                    编辑
                  </button>
                  <button
                    class="inline-flex items-center gap-1 font-bold text-red-500"
                  >
                    <Trash2 :size="15" />
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="flex flex-col gap-3 border-t border-slate-100 p-5 lg:flex-row lg:justify-between">
        <button class="btn btn-ghost lg:!w-auto">添加工具</button>
        <div class="grid gap-3 sm:grid-cols-2 lg:flex">
          <button class="btn btn-ghost lg:!w-auto">恢复默认配置</button>
          <button class="btn btn-primary lg:!w-auto" @click="save">
            <CheckCircle2 :size="18" />
            保存配置
          </button>
        </div>
      </div>
    </section>
  </section>
</template>
