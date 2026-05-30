<script setup lang="ts">
import {
  CheckCircle2,
  Clock3,
  FileSearch,
  Play,
  ScrollText,
} from "lucide-vue-next";

const runs = [
  [
    "理解需求",
    "已提取核心实体：订单、优惠券、折扣、结算、日志",
    "已完成",
    "00:04",
  ],
  ["浏览项目目录", "识别订单、优惠券、前端结算页等相关模块", "已完成", "00:06"],
  ["搜索相关代码", "命中文件 28 个，筛选出高相关文件 12 个", "已完成", "00:08"],
  [
    "读取代码片段",
    "读取 12 个文件的关键片段，共计 2,436 行代码",
    "已完成",
    "00:15",
  ],
  ["生成影响分析", "正在分析影响范围、接口变更、风险点", "进行中", "00:12"],
  ["生成 PDF 报告", "分析完成后将生成完整报告", "等待中", "--:--"],
];
</script>

<template>
  <section class="space-y-6">
    <header>
      <h1 class="text-3xl font-black text-slate-950">运行轨迹</h1>
      <p class="mt-2 text-base text-slate-500">
        查看 Agent 分析任务的执行步骤、工具调用和耗时
      </p>
    </header>

    <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
      <main class="card p-6">
        <div class="mb-6 flex items-center justify-between">
          <h2 class="text-xl font-black">Agent 执行轨迹</h2>
          <button class="btn btn-ghost !w-auto">收起</button>
        </div>
        <ol class="relative space-y-7 border-l border-slate-200 pl-8">
          <li v-for="(run, index) in runs" :key="run[0]" class="relative">
            <span
              class="absolute -left-[43px] grid h-8 w-8 place-items-center rounded-full text-sm font-black"
              :class="
                run[2] === '已完成'
                  ? 'bg-brand-600 text-white'
                  : run[2] === '进行中'
                    ? 'bg-brand-700 text-white'
                    : 'border border-slate-300 bg-white text-slate-500'
              "
            >
              <CheckCircle2 v-if="run[2] === '已完成'" :size="18" />
              <template v-else>{{ index + 1 }}</template>
            </span>
            <div
              class="grid gap-3 lg:grid-cols-[180px_1fr_auto_auto] lg:items-start"
            >
              <h3 class="font-black text-slate-950">{{ run[0] }}</h3>
              <div
                class="rounded-xl border border-slate-200 bg-white p-4 text-sm leading-6 text-slate-600"
              >
                {{ run[1] }}
              </div>
              <span
                class="text-sm font-bold"
                :class="
                  run[2] === '已完成'
                    ? 'text-emerald-600'
                    : run[2] === '进行中'
                      ? 'text-brand-600'
                      : 'text-slate-500'
                "
              >
                {{ run[2] }}
              </span>
              <span class="text-sm text-slate-500">{{ run[3] }}</span>
            </div>
          </li>
        </ol>
      </main>

      <aside class="space-y-5">
        <section class="card p-5">
          <h2 class="mb-4 text-lg font-black">任务摘要</h2>
          <div class="grid grid-cols-2 gap-4">
            <div class="rounded-xl bg-slate-50 p-4">
              <p class="text-sm text-slate-500">总耗时</p>
              <p class="mt-1 text-2xl font-black">00:45</p>
            </div>
            <div class="rounded-xl bg-slate-50 p-4">
              <p class="text-sm text-slate-500">预计剩余</p>
              <p class="mt-1 text-2xl font-black">00:10</p>
            </div>
          </div>
        </section>

        <section class="card p-5">
          <h2 class="mb-4 text-lg font-black">工具调用</h2>
          <div class="space-y-3">
            <div
              v-for="item in [
                ['list_project_tree', FileSearch],
                ['search_text', FileSearch],
                ['read_file_lines', FileSearch],
                ['generate_pdf_report', ScrollText],
              ]"
              :key="item[0] as string"
              class="flex items-center justify-between rounded-xl border border-slate-200 p-3"
            >
              <div class="flex items-center gap-3">
                <component :is="item[1]" :size="18" class="text-brand-600" />
                <span class="mono text-sm font-semibold">{{ item[0] }}</span>
              </div>
              <span
                class="rounded-md bg-emerald-50 px-2 py-1 text-xs font-bold text-emerald-600"
                >成功</span
              >
            </div>
          </div>
        </section>

        <button class="btn btn-primary w-full">
          <Play :size="18" />
          继续运行
        </button>
      </aside>
    </div>
  </section>
</template>
