<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { Download, Eye, FileText, RefreshCw, Trash2 } from "lucide-vue-next";
import { reportApi } from "@/api/report.api";
import type { Report } from "@/types/domain";
import StatusBadge from "@/components/StatusBadge.vue";
import EmptyState from "@/components/EmptyState.vue";
import { fmtDate } from "@/utils/format";

const route = useRoute();
const wid = computed(() => String(route.params.workspaceId));
const pid = computed(() => String(route.params.projectId));

const reports = ref<Report[]>([]);
const selected = ref<Report | null>(null);
const markdown = ref("");
const loading = ref(false);
const reportType = ref("");

onMounted(load);

async function load() {
  loading.value = true;
  try {
    reports.value = await reportApi.list(wid.value, pid.value, {
      reportType: reportType.value || undefined,
    });
    if (reports.value[0]) select(reports.value[0]);
  } finally {
    loading.value = false;
  }
}

async function select(report: Report) {
  selected.value = report;
  markdown.value = "";
  try {
    markdown.value = await reportApi.markdown(wid.value, pid.value, report.id);
  } catch {
    markdown.value = "## 报告预览\n\n当前后端未返回 Markdown 内容。";
  }
}

async function remove(report: Report) {
  if (!confirm("确认删除该报告吗？")) return;
  await reportApi.remove(wid.value, pid.value, report.id);
  await load();
}
</script>

<template>
  <section class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_520px]">
    <main class="card min-w-0 overflow-hidden">
      <div
        class="flex flex-col gap-3 border-b border-slate-200 p-4 lg:flex-row lg:items-center lg:justify-between"
      >
        <div class="mobile-scroll flex gap-2">
          <button
            class="btn"
            :class="!reportType ? 'btn-primary' : 'btn-ghost'"
            @click="
              reportType = '';
              load();
            "
          >
            全部
          </button>
          <button
            class="btn"
            :class="
              reportType === 'IMPACT_ANALYSIS' ? 'btn-primary' : 'btn-ghost'
            "
            @click="
              reportType = 'IMPACT_ANALYSIS';
              load();
            "
          >
            影响分析
          </button>
          <button
            class="btn"
            :class="
              reportType === 'TEST_SUGGESTION' ? 'btn-primary' : 'btn-ghost'
            "
            @click="
              reportType = 'TEST_SUGGESTION';
              load();
            "
          >
            测试建议
          </button>
        </div>
        <button class="btn btn-ghost" @click="load">
          <RefreshCw :size="18" />
          刷新
        </button>
      </div>

      <div v-if="loading" class="p-8 text-slate-500">正在加载报告...</div>
      <EmptyState
        v-else-if="!reports.length"
        title="暂无报告"
        description="可在需求分析完成后生成分析报告。"
      />
      <div v-else class="table-wrap rounded-none border-0">
        <table class="min-w-[760px] text-sm">
          <thead>
            <tr>
              <th class="table-th">报告名称</th>
              <th class="table-th">类型</th>
              <th class="table-th">格式</th>
              <th class="table-th">生成时间</th>
              <th class="table-th">状态</th>
              <th class="table-th">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="report in reports"
              :key="report.id"
              :class="selected?.id === report.id ? 'bg-brand-50/40' : ''"
            >
              <td class="table-td font-bold text-slate-950">
                {{
                  report.reportName ||
                  report.name ||
                  report.title ||
                  "未命名报告"
                }}
              </td>
              <td class="table-td">{{ report.reportType || "-" }}</td>
              <td class="table-td">{{ report.format || "Markdown" }}</td>
              <td class="table-td">
                {{ fmtDate(report.generatedAt || report.createdAt) }}
              </td>
              <td class="table-td">
                <StatusBadge :status="report.status || 'COMPLETED'" />
              </td>
              <td class="table-td">
                <div class="flex flex-wrap gap-3">
                  <button
                    class="inline-flex items-center gap-1 font-semibold text-brand-700"
                    @click="select(report)"
                  >
                    <Eye :size="16" />
                    预览
                  </button>
                  <a
                    class="inline-flex items-center gap-1 font-semibold text-brand-700"
                    :href="reportApi.downloadUrl(wid, pid, report.id)"
                    target="_blank"
                    rel="noreferrer"
                  >
                    <Download :size="16" />
                    下载
                  </a>
                  <button
                    class="inline-flex items-center gap-1 font-semibold text-rose-600"
                    @click="remove(report)"
                  >
                    <Trash2 :size="16" />
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <aside class="card min-w-0 overflow-hidden">
      <div
        class="flex items-center justify-between border-b border-slate-200 px-5 py-4"
      >
        <div>
          <h2 class="flex items-center gap-2 text-lg font-black text-slate-950">
            <FileText :size="20" class="text-brand-600" />
            报告预览
          </h2>
          <p class="mt-1 text-xs text-slate-500">
            Markdown / PDF 下载由后端接口提供。
          </p>
        </div>
      </div>
      <div class="max-h-[720px] overflow-auto p-6 scrollbar-thin">
        <pre
          class="whitespace-pre-wrap break-words text-sm leading-7 text-slate-700"
          >{{ markdown || "请选择一份报告预览。" }}</pre
        >
      </div>
    </aside>
  </section>
</template>
