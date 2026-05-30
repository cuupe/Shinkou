<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  AlertTriangle,
  Bot,
  ClipboardList,
  Download,
  FileCode2,
  FileText,
  GitBranch,
  Play,
  Settings2,
} from "lucide-vue-next";
import { agentApi } from "@/api/agent.api";
import type {
  AffectedFile,
  AgentAnalyzeResponse,
  Risk,
  TaskDraft,
  ToolCall,
} from "@/types/domain";
import StatusBadge from "@/components/StatusBadge.vue";

const route = useRoute();
const router = useRouter();
const wid = computed(() => String(route.params.workspaceId));
const pid = computed(() => String(route.params.projectId));

const requirement = ref(
  "在订单系统中新增优惠券折扣功能：\n用户在结算时可输入优惠券码，系统校验后应用折扣并计算最终支付金额。\n需要支持按订单金额百分比或固定金额的优惠，优惠不可叠加使用，并记录优惠使用日志。",
);
const analyzing = ref(false);
const result = ref<AgentAnalyzeResponse | null>(null);
const steps = ref<ToolCall[]>([]);
const affected = ref<AffectedFile[]>([]);
const risks = ref<Risk[]>([]);
const tasks = ref<TaskDraft[]>([]);

const trace = computed(() => {
  if (steps.value.length) {
    return steps.value.map((step, index) => ({
      title: step.toolName,
      desc: step.errorMessage || "工具调用已完成",
      status: step.status,
      time: step.latencyMs ? `${step.latencyMs}ms` : `00:0${index + 4}`,
    }));
  }
  return [
    {
      title: "理解需求",
      desc: "已提取核心实体：订单、优惠券、折扣、结算、日志",
      status: "COMPLETED",
      time: "00:04",
    },
    {
      title: "浏览项目目录",
      desc: "扫描项目结构，识别订单、优惠券、结算页等相关模块",
      status: "COMPLETED",
      time: "00:06",
    },
    {
      title: "搜索相关代码",
      desc: "关键词：coupon、discount、checkout、order",
      status: "COMPLETED",
      time: "00:08",
    },
    {
      title: "读取代码片段",
      desc: "读取 12 个文件的关键片段，共计 2,436 行代码",
      status: "COMPLETED",
      time: "00:15",
    },
    {
      title: "生成影响分析",
      desc: "正在分析影响范围、接口变更、风险点...",
      status: analyzing.value ? "RUNNING" : "PENDING",
      time: "00:12",
    },
    {
      title: "生成 PDF 报告",
      desc: "分析完成后将生成完整报告",
      status: "PENDING",
      time: "--:--",
    },
  ];
});

async function analyze() {
  if (!requirement.value.trim()) return;
  analyzing.value = true;
  try {
    const response = await agentApi.analyze(
      wid.value,
      pid.value,
      requirement.value.trim(),
    );
    result.value = response;
    steps.value = response.steps || [];
    affected.value = (response.affectedFiles || []).map((item: any) => ({
      ...item,
      filePath: item.filePath || item.path,
    }));
    risks.value = response.risks || [];
    tasks.value = response.tasks || [];
    if (response.sessionId) hydrate(response.sessionId);
  } catch (error: any) {
    alert(error.message || "分析失败");
  } finally {
    analyzing.value = false;
  }
}

async function hydrate(sessionId: string) {
  const [toolCalls, affectedFiles, riskItems, taskDrafts] = await Promise.all([
    agentApi
      .toolCalls(wid.value, pid.value, sessionId)
      .catch(() => steps.value),
    agentApi
      .affectedFiles(wid.value, pid.value, sessionId)
      .catch(() => affected.value),
    agentApi.risks(wid.value, pid.value, sessionId).catch(() => risks.value),
    agentApi
      .taskDrafts(wid.value, pid.value, sessionId)
      .catch(() => tasks.value),
  ]);
  steps.value = toolCalls;
  affected.value = affectedFiles.map((item: any) => ({
    ...item,
    filePath: item.filePath || item.path,
  }));
  risks.value = riskItems;
  tasks.value = taskDrafts;
}

function openFile(file: AffectedFile) {
  router.push(
    `/workspaces/${wid.value}/projects/${pid.value}/files?path=${encodeURIComponent(file.filePath)}`,
  );
}

async function genReport() {
  if (!result.value?.sessionId) return;
  await agentApi.generateReport(wid.value, pid.value, result.value.sessionId);
  router.push(`/workspaces/${wid.value}/projects/${pid.value}/reports`);
}
</script>

<template>
  <section class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_440px]">
    <main class="space-y-6">
      <header>
        <h1 class="text-3xl font-black text-slate-950">需求影响分析</h1>
        <p class="mt-2 text-base text-slate-500">
          基于代码库和文档，分析需求变更的影响范围与风险
        </p>
      </header>

      <section class="card p-5">
        <div class="mb-3 flex items-center gap-3">
          <h2 class="font-black">输入需求描述</h2>
          <span
            class="rounded-md bg-brand-50 px-2 py-1 text-xs font-bold text-brand-600"
            >示例</span
          >
        </div>
        <textarea
          v-model="requirement"
          class="min-h-36 w-full resize-y rounded-xl border border-brand-400 bg-white p-4 text-sm leading-7 outline-none focus:ring-4 focus:ring-brand-500/10"
          maxlength="2000"
        />
        <div class="mt-4 grid gap-3 lg:grid-cols-[180px_auto_1fr_auto_auto]">
          <button class="btn btn-ghost">深度分析（推荐）</button>
          <button class="btn btn-ghost">
            <Settings2 :size="17" />
            高级选项
          </button>
          <div />
          <button
            class="btn btn-primary"
            :disabled="analyzing"
            @click="analyze"
          >
            <Play :size="17" />
            {{ analyzing ? "分析中..." : "开始分析" }}
          </button>
          <button class="btn btn-ghost" @click="genReport">
            <Download :size="17" />
            导出 PDF
          </button>
        </div>
      </section>

      <nav
        class="mobile-scroll flex gap-8 border-b border-slate-200 text-sm font-bold"
      >
        <button
          v-for="item in [
            ['需求摘要', Bot],
            ['影响文件', FileCode2],
            ['影响接口', GitBranch],
            ['风险点', AlertTriangle],
            ['测试建议', FileText],
            ['任务草稿', ClipboardList],
          ]"
          :key="item[0] as string"
          class="flex shrink-0 items-center gap-2 border-b-2 border-transparent px-1 py-4 text-slate-500 first:border-brand-600 first:text-brand-600"
        >
          <component :is="item[1]" :size="17" />
          {{ item[0] }}
        </button>
      </nav>

      <div class="grid gap-5 lg:grid-cols-2">
        <section class="card p-5">
          <h2 class="mb-4 flex items-center gap-2 text-lg font-black">
            <FileText :size="19" />
            需求摘要
          </h2>
          <dl class="space-y-3 text-sm">
            <div class="grid grid-cols-[100px_1fr] gap-3">
              <dt class="text-slate-500">需求主题</dt>
              <dd class="font-semibold">新增优惠券折扣功能</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-3">
              <dt class="text-slate-500">主要目标</dt>
              <dd>
                在线路流程中支持优惠券校验与折扣计算，确保优惠规则正确应用并记录日志。
              </dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-3">
              <dt class="text-slate-500">影响范围</dt>
              <dd>
                后端订单服务、优惠券服务、结算流程、前端结算页面、日志与报表模块
              </dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-3">
              <dt class="text-slate-500">影响级别</dt>
              <dd>
                <span
                  class="rounded-md bg-amber-50 px-2 py-1 text-xs font-bold text-amber-600"
                  >中等</span
                >
              </dd>
            </div>
          </dl>
        </section>

        <section class="card p-5">
          <h2 class="mb-4 text-lg font-black">关键数据模型</h2>
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-slate-200 text-left text-slate-500">
                <th class="py-2">实体/表</th>
                <th class="py-2">影响说明</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in [
                  ['orders', '可能新增字段 coupon_id, discount_amount'],
                  ['coupons', '优惠券信息与规则校验'],
                  ['order_discounts', '优惠使用记录（新增表）'],
                  ['coupon_usage_logs', '优惠使用日志（新增表）'],
                ]"
                :key="row[0]"
                class="border-b border-slate-100"
              >
                <td class="py-2 font-semibold">{{ row[0] }}</td>
                <td class="py-2 text-slate-600">{{ row[1] }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>

      <section class="card overflow-hidden">
        <div class="border-b border-slate-100 px-5 py-4">
          <h2 class="text-lg font-black">
            影响文件（{{ affected.length || 4 }}）
          </h2>
        </div>
        <div class="table-wrap rounded-none border-0">
          <table class="min-w-[920px] w-full text-sm">
            <thead>
              <tr>
                <th class="table-th">文件路径</th>
                <th class="table-th">影响范围</th>
                <th class="table-th">影响原因</th>
                <th class="table-th">建议操作</th>
                <th class="table-th">置信度</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="file in affected.length
                  ? affected
                  : [
                      {
                        filePath:
                          'backend/src/main/java/com/demo/order/OrderService.java',
                        lineStart: 128,
                        lineEnd: 186,
                        reason: '订单计算逻辑需接入优惠券校验与折扣计算',
                        confidence: 0.96,
                      },
                      {
                        filePath:
                          'backend/src/main/java/com/demo/order/OrderController.java',
                        lineStart: 45,
                        lineEnd: 78,
                        reason: '新增优惠券校验接口与入参校验',
                        confidence: 0.91,
                      },
                      {
                        filePath:
                          'backend/src/main/java/com/demo/coupon/CouponService.java',
                        lineStart: 1,
                        lineEnd: 210,
                        reason: '优惠券校验、规则计算与使用记录',
                        confidence: 0.94,
                      },
                      {
                        filePath: 'frontend/src/pages/checkout/Checkout.tsx',
                        lineStart: 88,
                        lineEnd: 173,
                        reason: '结算页新增优惠券输入与展示逻辑',
                        confidence: 0.72,
                      },
                    ]"
                :key="file.filePath"
              >
                <td class="table-td">
                  <button
                    class="mono font-semibold text-slate-800"
                    @click="openFile(file as AffectedFile)"
                  >
                    {{ file.filePath }}
                  </button>
                </td>
                <td class="table-td">
                  {{ file.lineStart }}-{{ file.lineEnd }}
                </td>
                <td class="table-td">{{ file.reason }}</td>
                <td class="table-td">
                  <span
                    class="rounded-md bg-brand-50 px-2 py-1 text-xs font-bold text-brand-600"
                    >修改</span
                  >
                </td>
                <td class="table-td">
                  <span class="text-brand-600">●●●●</span
                  ><span class="text-slate-300">●</span>
                  <span class="ml-2"
                    >{{ Math.round((file.confidence || 0.8) * 100) }}%</span
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <div class="grid gap-5 lg:grid-cols-2">
        <section class="card p-5">
          <h2 class="mb-4 text-lg font-black">风险点</h2>
          <div class="space-y-3">
            <div
              v-for="risk in risks.length
                ? risks
                : [
                    {
                      riskLevel: 'HIGH',
                      description: '优惠叠加规则可能导致金额计算异常',
                      suggestion: '在订单计算层统一做互斥校验',
                    },
                    {
                      riskLevel: 'MEDIUM',
                      description: '历史订单与新折扣字段兼容',
                      suggestion: '补充迁移脚本与默认值',
                    },
                  ]"
              :key="risk.description"
              class="rounded-xl border border-slate-200 p-4"
            >
              <StatusBadge :status="risk.riskLevel || 'MEDIUM'" />
              <p class="mt-3 font-semibold">{{ risk.description }}</p>
              <p class="mt-2 text-sm text-slate-500">{{ risk.suggestion }}</p>
            </div>
          </div>
        </section>

        <section class="card p-5">
          <h2 class="mb-4 text-lg font-black">任务草稿</h2>
          <div class="space-y-3">
            <div
              v-for="task in tasks.length
                ? tasks
                : [
                    {
                      id: 1,
                      title: '补充优惠券校验逻辑',
                      priority: 'P1',
                      description: '实现优惠券有效性、适用范围与使用条件校验',
                    },
                    {
                      id: 2,
                      title: '新增折扣使用日志',
                      priority: 'P2',
                      description: '记录订单使用优惠券的关键字段',
                    },
                  ]"
              :key="task.id"
              class="rounded-xl border border-slate-200 p-4"
            >
              <div class="flex items-start justify-between gap-3">
                <h3 class="font-black">{{ task.title }}</h3>
                <StatusBadge :status="task.priority" />
              </div>
              <p class="mt-2 text-sm text-slate-500">{{ task.description }}</p>
            </div>
          </div>
        </section>
      </div>
    </main>

    <aside class="card h-fit p-6 xl:sticky xl:top-[100px]">
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-xl font-black">Agent 执行轨迹</h2>
        <button class="btn btn-ghost !min-h-9 !w-auto !rounded-full !px-3">
          收起
        </button>
      </div>
      <ol class="relative space-y-7 border-l border-slate-200 pl-7">
        <li v-for="(step, index) in trace" :key="step.title" class="relative">
          <span
            class="absolute -left-[40px] grid h-7 w-7 place-items-center rounded-full text-sm font-black"
            :class="
              step.status === 'COMPLETED'
                ? 'bg-brand-600 text-white'
                : step.status === 'RUNNING'
                  ? 'bg-brand-700 text-white'
                  : 'border border-slate-300 bg-white text-slate-500'
            "
          >
            {{ step.status === "COMPLETED" ? "✓" : index + 1 }}
          </span>
          <div class="mb-2 flex items-center justify-between gap-3">
            <h3 class="font-black">{{ step.title }}</h3>
            <div class="flex gap-3 text-sm">
              <span
                :class="
                  step.status === 'COMPLETED'
                    ? 'text-emerald-600'
                    : step.status === 'RUNNING'
                      ? 'text-brand-600'
                      : 'text-slate-500'
                "
              >
                {{
                  step.status === "COMPLETED"
                    ? "已完成"
                    : step.status === "RUNNING"
                      ? "进行中"
                      : "等待中"
                }}
              </span>
              <span class="text-slate-500">{{ step.time }}</span>
            </div>
          </div>
          <div
            class="rounded-xl border border-slate-200 p-3 text-sm leading-6 text-slate-500"
          >
            {{ step.desc }}
          </div>
        </li>
      </ol>
      <div
        class="mt-8 grid grid-cols-2 gap-4 rounded-xl border border-slate-200 p-4"
      >
        <div>
          <p class="text-sm text-slate-500">总耗时</p>
          <p class="mt-1 text-xl font-black">00:45</p>
        </div>
        <div>
          <p class="text-sm text-slate-500">预计剩余</p>
          <p class="mt-1 text-xl font-black">00:10</p>
        </div>
      </div>
    </aside>
  </section>
</template>
