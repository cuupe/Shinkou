<script setup lang="ts">
import { ref } from "vue";
import {
  CheckSquare,
  ChevronDown,
  ChevronRight,
  Copy,
  FileCode2,
  Folder,
  GitBranch,
  Search,
  SlidersHorizontal,
  X,
} from "lucide-vue-next";

const keyword = ref("coupon / createOrder / payAmount");
const files = [
  "order-service",
  "src",
  "main",
  "java",
  "com",
  "demo",
  "order",
  "service",
  "OrderService.java",
  "CouponService.java",
  "PaymentService.java",
];

const results = [
  {
    file: "order-service/src/main/java/com/demo/order/service/OrderService.java",
    score: "0.96",
    line: "137-147",
  },
  {
    file: "order-service/src/main/java/com/demo/order/service/CouponService.java",
    score: "0.92",
    line: "78-104",
  },
  {
    file: "order-service/src/main/java/com/demo/order/controller/OrderController.java",
    score: "0.78",
    line: "52-66",
  },
];
</script>

<template>
  <section class="space-y-5">
    <header>
      <h1 class="text-3xl font-black text-slate-950">代码探索</h1>
      <p class="mt-2 text-base text-slate-500">
        搜索代码、查看引用与相关上下文，快速定位实现细节
      </p>
    </header>

    <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_640px]">
      <main class="space-y-4">
        <div
          class="grid gap-3 rounded-xl border border-brand-300 bg-white p-2 shadow-sm lg:grid-cols-[1fr_120px]"
        >
          <label class="relative">
            <Search
              :size="19"
              class="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            />
            <input
              v-model="keyword"
              class="h-12 w-full rounded-lg border-0 pl-11 pr-10 text-base font-semibold outline-none"
            />
            <button
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400"
            >
              <X :size="18" />
            </button>
          </label>
          <button class="btn btn-primary h-12">
            <Search :size="18" />
            搜索
          </button>
        </div>

        <div class="flex flex-wrap gap-2">
          <button
            v-for="tag in ['全部', 'Java', 'TypeScript', 'SQL', '文档']"
            :key="tag"
            class="btn !min-h-9 !rounded-full !px-5"
            :class="tag === '全部' ? 'btn-primary' : 'btn-ghost'"
          >
            {{ tag }}
          </button>
          <button class="btn btn-ghost !min-h-9 !rounded-full !px-5">
            <SlidersHorizontal :size="16" />
            更多筛选
          </button>
        </div>

        <div
          class="grid min-h-[760px] overflow-hidden rounded-xl border border-slate-200 bg-white lg:grid-cols-[300px_minmax(0,1fr)]"
        >
          <aside class="border-r border-slate-200">
            <div
              class="flex h-12 items-center justify-between border-b border-slate-100 px-4"
            >
              <h2 class="font-black">文件树</h2>
              <GitBranch :size="17" class="text-slate-500" />
            </div>
            <div class="p-4">
              <label class="relative mb-4 block">
                <Search
                  :size="16"
                  class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                />
                <input
                  class="input h-10 min-h-10 pl-9"
                  placeholder="搜索文件或目录"
                />
              </label>
              <div class="space-y-1 text-sm">
                <div
                  v-for="(file, index) in files"
                  :key="file"
                  class="flex items-center gap-2 rounded-lg px-2 py-1.5"
                  :class="
                    file === 'OrderService.java'
                      ? 'bg-brand-50 text-brand-700'
                      : 'text-slate-700'
                  "
                >
                  <ChevronDown v-if="index < 8" :size="14" />
                  <ChevronRight v-else :size="14" class="opacity-40" />
                  <Folder
                    v-if="!file.endsWith('.java')"
                    :size="16"
                    class="text-slate-400"
                  />
                  <FileCode2 v-else :size="16" class="text-brand-500" />
                  <span class="truncate">{{ file }}</span>
                </div>
              </div>
            </div>
          </aside>

          <section class="min-w-0">
            <div
              class="flex h-12 items-center justify-between border-b border-slate-100 px-4"
            >
              <div
                class="flex items-center gap-2 rounded-t-lg bg-brand-50 px-3 py-2 text-sm font-bold text-brand-700"
              >
                <FileCode2 :size="16" />
                OrderService.java
                <X :size="14" />
              </div>
              <div class="flex gap-2">
                <button class="btn btn-ghost !min-h-9 !px-3">
                  <SlidersHorizontal :size="16" />
                  格式化
                </button>
                <button class="btn btn-ghost !min-h-9 !px-3">
                  <Copy :size="16" />
                  复制
                </button>
              </div>
            </div>
            <div
              class="border-b border-slate-100 px-5 py-3 text-sm text-slate-500"
            >
              src / main / java / com / demo / order / service /
              OrderService.java
            </div>
            <pre
              class="max-h-[690px] overflow-auto bg-white p-5 text-sm leading-7 text-slate-800 scrollbar-thin"
            ><code>/**
 * 创建订单
 * @param req 订单创建请求
 * @return 订单信息
 */
public Order createOrder(CreateOrderRequest req) {
    BigDecimal payAmount = req.getPayAmount();
    Order order = new Order();
    order.setUserId(req.getUserId());
    order.setPayAmount(payAmount);
    order.setStatus(OrderStatus.CREATED);

    // 优惠券抵扣逻辑
    if (StringUtils.isNotBlank(req.getCouponCode())) {
        Coupon coupon = couponService.getValidCoupon(
            req.getUserId(), req.getCouponCode(), payAmount);
        if (coupon != null) {
            BigDecimal discount = coupon.calculateDiscount(payAmount);
            order.setCouponId(coupon.getId());
            order.setDiscountAmount(discount);
            order.setPayAmount(payAmount.subtract(discount));
        } else {
            throw new BizException("优惠券无效或不满足使用条件");
        }
    }

    orderMapper.insert(order);
    return order;
}</code></pre>
          </section>
        </div>
      </main>

      <aside class="rounded-xl border border-slate-200 bg-white">
        <div
          class="flex h-14 items-center gap-7 border-b border-slate-100 px-5"
        >
          <button
            class="border-b-2 border-brand-600 py-4 text-sm font-black text-brand-600"
          >
            搜索结果 <span class="rounded-full bg-brand-50 px-2">12</span>
          </button>
          <button class="py-4 text-sm font-bold text-slate-500">
            引用证据 <span class="rounded-full bg-slate-100 px-2">18</span>
          </button>
          <button class="py-4 text-sm font-bold text-slate-500">
            相关文件 <span class="rounded-full bg-slate-100 px-2">9</span>
          </button>
        </div>
        <div
          class="flex items-center justify-between border-b border-slate-100 px-5 py-4"
        >
          <label
            class="flex items-center gap-2 text-sm font-semibold text-slate-600"
          >
            <CheckSquare :size="18" class="text-brand-600" />
            全选（已选择 2 条）
          </label>
          <button class="btn btn-ghost !min-h-9 !px-3">
            相关度排序
            <ChevronDown :size="16" />
          </button>
        </div>
        <div class="space-y-4 p-5">
          <article
            v-for="(result, index) in results"
            :key="result.file"
            class="rounded-xl border border-slate-200 p-5"
          >
            <div class="flex items-start justify-between gap-3">
              <label class="flex min-w-0 items-center gap-3">
                <input
                  type="checkbox"
                  class="h-4 w-4 accent-brand-600"
                  :checked="index < 2"
                />
                <span class="truncate text-sm font-semibold text-slate-600">{{
                  result.file
                }}</span>
              </label>
              <span
                class="rounded-lg bg-emerald-50 px-2 py-1 text-sm font-black text-emerald-600"
                >{{ result.score }}</span
              >
            </div>
            <p class="mt-3 text-sm font-semibold text-slate-500">
              行 {{ result.line }}
            </p>
            <pre class="mt-3 overflow-auto text-sm leading-6 text-slate-700">
if (StringUtils.isNotBlank(req.getCouponCode())) {
    Coupon coupon = couponService.getValidCoupon(...)
    order.setPayAmount(payAmount.subtract(discount));
}</pre
            >
            <div class="mt-4 flex justify-between">
              <span
                class="rounded-md bg-brand-50 px-2 py-1 text-xs font-bold text-brand-600"
                >方法体匹配</span
              >
              <button
                class="flex items-center gap-1 text-sm font-bold text-brand-600"
              >
                查看上下文
                <ChevronRight :size="16" />
              </button>
            </div>
          </article>
        </div>
        <div class="sticky bottom-0 border-t border-slate-100 bg-white p-5">
          <button class="btn btn-primary w-full">加入分析上下文</button>
        </div>
      </aside>
    </div>
  </section>
</template>
