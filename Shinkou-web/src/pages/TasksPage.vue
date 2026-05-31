<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import {
  CheckCircle2,
  ClipboardList,
  Filter,
  Plus,
  Search,
  SlidersHorizontal,
} from "lucide-vue-next";
import { taskApi } from "@/api/task.api";
import type { TaskDraft, WorkTask } from "@/types/domain";
import StatusBadge from "@/components/StatusBadge.vue";
import UiSelect from "@/components/UiSelect.vue";
import { asArrayFiles } from "@/utils/format";

const route = useRoute();
const wid = computed(() => String(route.params.workspaceId));
const pid = computed(() => String(route.params.projectId));

const drafts = ref<TaskDraft[]>([]);
const tasks = ref<WorkTask[]>([]);
const loading = ref(false);
const priority = ref("");
const status = ref("");
const source = ref("");
const priorityOptions = [
  { label: "优先级：全部", value: "" },
  { label: "P1", value: "P1" },
  { label: "P2", value: "P2" },
  { label: "P3", value: "P3" },
];
const statusOptions = [
  { label: "状态：全部", value: "" },
  { label: "待确认", value: "CONFIRMING" },
  { label: "待开发", value: "TODO" },
  { label: "开发中", value: "IN_PROGRESS" },
  { label: "已完成", value: "DONE" },
];
const sourceOptions = [{ label: "来源：全部", value: "" }];

const demoTasks: WorkTask[] = [
  {
    id: 1,
    title: "实现订单优惠券校验逻辑",
    priority: "P1",
    status: "CONFIRMING",
    description:
      "在创建订单时校验优惠券的有效性、适用范围与使用条件，确保优惠正确应用。",
    relatedFiles: ["CouponService.java", "coupon_rules.md"],
  },
  {
    id: 2,
    title: "补充订单接口单元测试",
    priority: "P2",
    status: "CONFIRMING",
    description:
      "为 OrderController 的创建、取消、查询等接口补充单元测试，提升覆盖率。",
    relatedFiles: ["OrderControllerTest.java"],
  },
  {
    id: 3,
    title: "订单列表支持多条件筛选",
    priority: "P1",
    status: "TODO",
    description: "支持按状态、时间范围、用户、支付方式等条件组合筛选订单列表。",
    relatedFiles: ["OrderService.java", "OrderQueryDTO.java"],
  },
  {
    id: 4,
    title: "导出订单数据为 Excel",
    priority: "P2",
    status: "TODO",
    description:
      "在订单列表页面支持将筛选结果导出为 Excel，包含主要字段与明细信息。",
    relatedFiles: ["Order_export.xlsx", "OrderExportService.java"],
  },
  {
    id: 5,
    title: "优惠券发放接口开发",
    priority: "P1",
    status: "IN_PROGRESS",
    description: "实现优惠券发放接口，支持批量发放与领取记录落库，保证幂等性。",
    relatedFiles: ["CouponController.java", "CouponService.java"],
  },
  {
    id: 6,
    title: "订单状态流转与日志记录",
    priority: "P2",
    status: "IN_PROGRESS",
    description: "完善订单状态流转规则，增加状态变更日志，便于追溯。",
    relatedFiles: ["OrderStatusEnum.java", "order_log.md"],
  },
  {
    id: 7,
    title: "订单创建接口开发",
    priority: "P1",
    status: "DONE",
    description: "实现订单创建接口，包含参数校验、库存扣减与金额计算。",
    relatedFiles: ["OrderController.java", "OrderService.java"],
  },
  {
    id: 8,
    title: "优惠券规则表设计与迁移",
    priority: "P2",
    status: "DONE",
    description: "设计优惠券规则相关表结构，并完成数据库迁移脚本与初始化数据。",
    relatedFiles: ["coupon_rule.sql"],
  },
];

const allTasks = computed(() => (tasks.value.length ? tasks.value : demoTasks));
const columns = [
  ["CONFIRMING", "待确认", "border-amber-400"],
  ["TODO", "待开发", "border-brand-500"],
  ["IN_PROGRESS", "开发中", "border-violet-500"],
  ["DONE", "已完成", "border-emerald-500"],
];

const summary = computed(() => ({
  total: allTasks.value.length + drafts.value.length,
  confirming: allTasks.value.filter((item) => item.status === "CONFIRMING")
    .length,
  todo: allTasks.value.filter((item) => item.status === "TODO").length,
  doing: allTasks.value.filter((item) => item.status === "IN_PROGRESS").length,
  done: allTasks.value.filter((item) => item.status === "DONE").length,
}));

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const params = {
      priority: priority.value || undefined,
      status: status.value || undefined,
    };
    const [draftList, taskList] = await Promise.all([
      taskApi.drafts(wid.value, pid.value, params).catch(() => []),
      taskApi.tasks(wid.value, pid.value, params).catch(() => []),
    ]);
    drafts.value = draftList;
    tasks.value = taskList;
  } finally {
    loading.value = false;
  }
}

function byStatus(value: string) {
  return allTasks.value.filter((item) => item.status === value);
}
</script>

<template>
  <section class="space-y-6">
    <header>
      <h1 class="text-3xl font-black text-slate-950">任务看板</h1>
      <p class="mt-2 text-base text-slate-500">
        AI 基于代码库与需求理解生成的开发任务，支持协作确认与进度跟踪
      </p>
    </header>

    <div class="grid gap-5 sm:grid-cols-2 xl:grid-cols-5">
      <article
        v-for="item in [
          ['全部任务', summary.total, ClipboardList, 'brand'],
          ['待确认', summary.confirming, ClipboardList, 'amber'],
          ['待开发', summary.todo, ClipboardList, 'blue'],
          ['开发中', summary.doing, ClipboardList, 'violet'],
          ['已完成', summary.done, CheckCircle2, 'emerald'],
        ]"
        :key="item[0] as string"
        class="card flex items-center justify-between p-6"
      >
        <div>
          <p class="text-sm font-bold text-slate-600">{{ item[0] }}</p>
          <p class="mt-2 text-3xl font-black text-slate-950">{{ item[1] }}</p>
        </div>
        <div
          class="grid h-14 w-14 place-items-center rounded-full bg-brand-50 text-brand-600"
        >
          <component :is="item[2]" :size="25" />
        </div>
      </article>
    </div>

    <div class="grid gap-3 xl:grid-cols-[180px_180px_180px_1fr_auto_auto_auto]">
      <UiSelect
        v-model="priority"
        :options="priorityOptions"
        aria-label="筛选任务优先级"
      />
      <UiSelect
        v-model="status"
        :options="statusOptions"
        aria-label="筛选任务状态"
      />
      <UiSelect
        v-model="source"
        :options="sourceOptions"
        aria-label="筛选任务来源"
      />
      <label class="relative">
        <Search
          :size="18"
          class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
        />
        <input class="input pl-10" placeholder="搜索任务标题或描述..." />
      </label>
      <button class="btn btn-ghost" @click="load">
        <Filter :size="18" />筛选
      </button>
      <button class="btn btn-ghost">
        <SlidersHorizontal :size="18" />排序
      </button>
      <button class="btn btn-primary"><Plus :size="18" />新建任务</button>
    </div>

    <div v-if="loading" class="card p-8 text-slate-500">正在加载任务...</div>

    <div v-else class="grid gap-5 xl:grid-cols-[1fr_320px]">
      <main class="grid gap-4 lg:grid-cols-2 2xl:grid-cols-4">
        <section
          v-for="[key, label, lineClass] in columns"
          :key="key"
          class="card overflow-hidden"
        >
          <div class="border-b border-slate-100 p-4">
            <h2 class="flex items-center gap-2 font-black">
              <span
                class="h-3 w-1 rounded-full border-l-4"
                :class="lineClass"
              />
              {{ label }}
              <span
                class="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500"
                >{{ byStatus(key).length }}</span
              >
            </h2>
          </div>
          <div class="space-y-4 p-4">
            <article
              v-for="task in byStatus(key)"
              :key="task.id"
              class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
            >
              <div class="mb-3 flex items-start justify-between gap-3">
                <h3 class="font-black leading-6">{{ task.title }}</h3>
                <StatusBadge :status="task.priority" />
              </div>
              <p class="min-h-12 text-sm leading-6 text-slate-500">
                {{ task.description }}
              </p>
              <div class="mt-4 flex flex-wrap gap-2">
                <span
                  v-for="file in asArrayFiles(task.relatedFiles).slice(0, 2)"
                  :key="file"
                  class="rounded-md border border-slate-200 bg-slate-50 px-2 py-1 text-xs text-slate-600"
                >
                  {{ file }}
                </span>
              </div>
              <div
                class="mt-4 flex items-center justify-between text-xs text-slate-500"
              >
                <span>验收标准 3 条</span>
                <span>负责人 Zhang Wei</span>
              </div>
            </article>
            <button
              class="w-full rounded-xl border border-dashed border-slate-200 py-3 text-sm font-bold text-brand-600"
            >
              + 新建任务
            </button>
          </div>
        </section>
      </main>

      <aside class="space-y-5">
        <section class="card p-5">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="font-black">任务概览</h2>
            <button class="text-sm font-bold text-slate-500">刷新</button>
          </div>
          <div class="grid grid-cols-[140px_1fr] items-center gap-4">
            <div
              class="grid h-32 w-32 place-items-center rounded-full bg-[conic-gradient(#f59e0b_0_25%,#3b82f6_25%_46%,#8b5cf6_46%_75%,#34d399_75%_100%)]"
            >
              <div
                class="grid h-20 w-20 place-items-center rounded-full bg-white text-center"
              >
                <div>
                  <p class="text-2xl font-black">{{ summary.total }}</p>
                  <p class="text-xs text-slate-500">全部任务</p>
                </div>
              </div>
            </div>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span>待确认</span><span>25%</span>
              </div>
              <div class="flex justify-between">
                <span>待开发</span><span>21%</span>
              </div>
              <div class="flex justify-between">
                <span>开发中</span><span>29%</span>
              </div>
              <div class="flex justify-between">
                <span>已完成</span><span>25%</span>
              </div>
            </div>
          </div>
        </section>
        <section class="card p-5">
          <h2 class="mb-4 font-black">优先级分布</h2>
          <div class="space-y-4">
            <div
              v-for="item in [
                ['P1（高优先级）', '12', '50%', 'bg-red-500'],
                ['P2（中优先级）', '10', '42%', 'bg-brand-500'],
                ['P3（低优先级）', '2', '8%', 'bg-slate-500'],
              ]"
              :key="item[0]"
            >
              <div class="mb-2 flex justify-between text-sm">
                <span>{{ item[0] }}</span
                ><span>{{ item[1] }} {{ item[2] }}</span>
              </div>
              <div class="h-2 rounded-full bg-slate-100">
                <div
                  class="h-2 rounded-full"
                  :class="item[3]"
                  :style="{ width: item[2] }"
                />
              </div>
            </div>
          </div>
        </section>
      </aside>
    </div>
  </section>
</template>
