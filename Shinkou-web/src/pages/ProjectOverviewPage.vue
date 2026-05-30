<script setup lang="ts">
import {
  ArrowRight,
  Bot,
  CheckCircle2,
  Clock3,
  Code2,
  FileText,
  ScrollText,
} from "lucide-vue-next";
import { useProjectStore } from "@/stores/project.store";

const project = useProjectStore();

const metrics = [
  {
    label: "代码文件数",
    value: "1,248",
    sub: "较上次 +42",
    icon: Code2,
    tone: "blue",
  },
  {
    label: "文档数",
    value: "86",
    sub: "较上次 +5",
    icon: FileText,
    tone: "cyan",
  },
  {
    label: "最近 Agent 分析次数",
    value: "12",
    sub: "近 7 天",
    icon: Bot,
    tone: "indigo",
  },
  {
    label: "报告数",
    value: "24",
    sub: "较上次 +3",
    icon: ScrollText,
    tone: "violet",
  },
  {
    label: "最近索引时间",
    value: "2 小时前",
    sub: "2025-05-27 10:30",
    icon: Clock3,
    tone: "purple",
  },
];

const languages = [
  ["Java", "64.2%", "801", "#2563eb"],
  ["TypeScript", "18.7%", "234", "#35c4d6"],
  ["SQL", "9.0%", "112", "#8b5cf6"],
  ["Markdown", "8.1%", "101", "#f6b72b"],
];

const activities = [
  ["上传文件 order-system.zip", "由 Zhang Wei 上传了项目压缩包", "10:30"],
  ["生成报告《优惠券影响分析》", "由 Agent 分析并生成报告", "10:12"],
  ["创建任务「支付超时问题排查」", "由 Zhang Wei 创建任务", "09:45"],
  ["索引重建完成", "成功处理 1,248 个文件", "08:30"],
  ["新增文件 12 个", "检测到 12 个新增或变更文件", "昨天 18:20"],
];

const recentAnalyses = [
  [
    "优惠券影响分析",
    "分析优惠券、折扣、订单、结算等相关模块的影响范围",
    "已完成",
  ],
  ["支付超时问题排查", "定位支付超时的根因并提供优化建议", "进行中"],
  ["订单状态流转分析", "分析订单状态从创建到完成的完整流转路径", "已完成"],
];
</script>

<template>
  <section class="space-y-7">
    <header>
      <h1 class="text-3xl font-black text-slate-950">项目概览</h1>
      <p class="mt-2 text-base text-slate-500">
        {{
          project.currentProject?.name || "项目"
        }}
        整体健康状况、资源统计与最近活动
      </p>
    </header>

    <div class="grid gap-6 sm:grid-cols-2 xl:grid-cols-5">
      <article
        v-for="item in metrics"
        :key="item.label"
        class="card flex items-center gap-5 p-6"
      >
        <div
          class="grid h-16 w-16 shrink-0 place-items-center rounded-full bg-brand-50 text-brand-600"
        >
          <component :is="item.icon" :size="32" />
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-500">{{ item.label }}</p>
          <p class="mt-1 text-2xl font-black text-slate-950">
            {{ item.value }}
          </p>
          <p class="mt-1 text-sm text-slate-500">{{ item.sub }}</p>
        </div>
      </article>
    </div>

    <div class="grid gap-6 xl:grid-cols-[1.05fr_0.95fr_1.2fr]">
      <section class="card overflow-hidden">
        <div class="border-b border-slate-100 px-6 py-5">
          <h2 class="text-lg font-black">技术栈分布</h2>
        </div>
        <div class="grid gap-6 p-6 sm:grid-cols-[220px_1fr] sm:items-center">
          <div
            class="relative mx-auto grid h-52 w-52 place-items-center rounded-full bg-[conic-gradient(#2563eb_0_64%,#35c4d6_64%_83%,#8b5cf6_83%_92%,#f6b72b_92%_100%)]"
          >
            <div
              class="grid h-32 w-32 place-items-center rounded-full bg-white text-center shadow-inner"
            >
              <div>
                <p class="text-2xl font-black">1,248</p>
                <p class="mt-1 text-sm text-slate-500">代码文件</p>
              </div>
            </div>
          </div>
          <div class="space-y-4">
            <div
              v-for="[name, pct, count, color] in languages"
              :key="name"
              class="grid grid-cols-[1fr_70px_52px] items-center gap-3 text-sm"
            >
              <div class="flex items-center gap-3">
                <span
                  class="h-3 w-3 rounded-full"
                  :style="{ background: color }"
                />
                <span class="font-semibold text-slate-700">{{ name }}</span>
              </div>
              <span class="text-right font-semibold text-slate-600">{{
                pct
              }}</span>
              <span class="text-right text-slate-500">{{ count }}</span>
            </div>
          </div>
        </div>
        <RouterLink
          class="flex items-center justify-end gap-2 border-t border-slate-100 px-6 py-4 text-sm font-bold text-brand-600"
          to="./files"
        >
          查看全部语言统计
          <ArrowRight :size="16" />
        </RouterLink>
      </section>

      <section class="card overflow-hidden">
        <div class="border-b border-slate-100 px-6 py-5">
          <h2 class="text-lg font-black">最近活动</h2>
        </div>
        <div class="space-y-4 p-6">
          <div
            v-for="[title, desc, time] in activities"
            :key="title"
            class="grid grid-cols-[32px_1fr_auto] gap-3"
          >
            <span
              class="grid h-8 w-8 place-items-center rounded-full bg-brand-50 text-brand-600"
            >
              <CheckCircle2 :size="17" />
            </span>
            <div>
              <p class="font-bold text-slate-900">{{ title }}</p>
              <p class="mt-1 text-sm text-slate-500">{{ desc }}</p>
            </div>
            <span class="text-sm text-slate-500">{{ time }}</span>
          </div>
        </div>
        <RouterLink
          class="flex items-center gap-2 border-t border-slate-100 px-6 py-4 text-sm font-bold text-brand-600"
          to="./runs"
        >
          查看全部活动
          <ArrowRight :size="16" />
        </RouterLink>
      </section>

      <section class="card overflow-hidden">
        <div class="border-b border-slate-100 px-6 py-5">
          <h2 class="text-lg font-black">项目健康度</h2>
        </div>
        <div class="grid gap-6 p-6 sm:grid-cols-[180px_1fr] sm:items-center">
          <div
            class="relative mx-auto grid h-40 w-40 place-items-center rounded-full bg-[conic-gradient(#2563eb_0_92%,#e8eef8_92%_100%)]"
          >
            <div
              class="grid h-28 w-28 place-items-center rounded-full bg-white text-center"
            >
              <div>
                <p class="text-3xl font-black">92</p>
                <p class="text-sm text-slate-500">健康分</p>
              </div>
            </div>
          </div>
          <div class="space-y-4">
            <div
              v-for="item in [
                ['索引完整性', '100%'],
                ['文档覆盖率', '88%'],
                ['代码解析率', '94%'],
                ['依赖解析', '91%'],
              ]"
              :key="item[0]"
            >
              <div class="mb-2 flex justify-between text-sm font-bold">
                <span>{{ item[0] }}</span>
                <span>{{ item[1] }}</span>
              </div>
              <div class="h-2 rounded-full bg-slate-100">
                <div
                  class="h-2 rounded-full bg-emerald-500"
                  :style="{ width: item[1] }"
                />
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <section class="card p-6">
      <div class="mb-5 flex items-center justify-between">
        <h2 class="text-lg font-black">最近分析</h2>
        <RouterLink
          class="flex items-center gap-2 text-sm font-bold text-brand-600"
          to="./analysis"
        >
          查看全部分析
          <ArrowRight :size="16" />
        </RouterLink>
      </div>
      <div class="grid gap-5 xl:grid-cols-3">
        <article
          v-for="[title, desc, status] in recentAnalyses"
          :key="title"
          class="rounded-xl border border-slate-200 p-5"
        >
          <div class="flex items-start justify-between gap-3">
            <h3 class="font-black">{{ title }}</h3>
            <span
              class="rounded-md bg-emerald-50 px-2 py-1 text-xs font-bold text-emerald-600"
              >{{ status }}</span
            >
          </div>
          <p class="mt-3 min-h-12 text-sm leading-6 text-slate-500">
            {{ desc }}
          </p>
          <div class="mt-5 grid grid-cols-2 gap-3">
            <button class="btn btn-ghost">查看报告</button>
            <button class="btn btn-primary">继续分析</button>
          </div>
        </article>
      </div>
    </section>
  </section>
</template>
