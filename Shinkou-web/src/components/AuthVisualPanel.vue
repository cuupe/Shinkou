<script setup lang="ts">
import {
  BarChart3,
  Building2,
  GitBranch,
  ShieldCheck,
  TrendingUp,
  Users,
} from "lucide-vue-next";
import ShinkouLogo from "./ShinkouLogo.vue";

const props = withDefaults(
  defineProps<{
    variant?: "login" | "activate";
  }>(),
  {
    variant: "login",
  },
);

const loginFeatures = [
  {
    title: "全局影响分析",
    description: "自动关联需求、代码、测试与发布，精准评估变更影响范围。",
    icon: GitBranch,
  },
  {
    title: "降低交付风险",
    description: "可视化依赖与风险，提前识别潜在问题，保障项目交付质量。",
    icon: ShieldCheck,
  },
  {
    title: "提升研发效能",
    description: "沉淀知识资产，优化协作流程，让团队专注于创造价值。",
    icon: BarChart3,
  },
];

const activateFeatures = [
  {
    title: "企业级安全与合规",
    description: "统一身份管理，数据隔离，审计可追溯",
    icon: ShieldCheck,
  },
  {
    title: "多工作区协同管理",
    description: "在不同团队与项目间灵活协作",
    icon: Users,
  },
  {
    title: "研发效能全面提升",
    description: "从需求到交付，全链路可视化与度量",
    icon: TrendingUp,
  },
];

const features = props.variant === "login" ? loginFeatures : activateFeatures;
</script>

<template>
  <aside class="auth-visual-panel">
    <div>
      <ShinkouLogo size="lg" />

      <div v-if="variant === 'activate'" class="auth-pill mt-20">
        <Users :size="18" />
        仅限受邀成员激活账号
      </div>

      <div :class="variant === 'login' ? 'mt-12' : 'mt-8'">
        <h1 class="auth-hero-title">
          <template v-if="variant === 'login'"
            >研发需求变更影响分析平台</template
          >
          <template v-else
            >欢迎加入 <span>Shinkou</span><br />企业级研发效能平台</template
          >
        </h1>
        <p class="auth-hero-copy">
          <template v-if="variant === 'login'"
            >洞察变更影响，降低交付风险，提升研发效能</template
          >
          <template v-else
            >您已被邀请加入指定工作区。请完成账号激活，开启高效、安全的协作与交付之旅。</template
          >
        </p>
      </div>

      <div :class="variant === 'login' ? 'mt-10 space-y-4' : 'mt-10 space-y-5'">
        <div
          v-for="item in features"
          :key="item.title"
          class="auth-feature-card"
        >
          <div class="auth-feature-icon">
            <component :is="item.icon" :size="variant === 'login' ? 34 : 26" />
          </div>
          <div>
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <div
      class="auth-illustration"
      :class="{ 'auth-illustration--compact': variant === 'activate' }"
      aria-hidden="true"
    >
      <div class="auth-grid-plane" />
      <div class="auth-platform">
        <div class="auth-cube">
          <Building2 v-if="variant === 'activate'" :size="46" />
          <ShinkouLogo v-else size="sm" dark />
        </div>
      </div>
      <div class="auth-chip auth-chip--left">
        <GitBranch :size="24" />
      </div>
      <div class="auth-chip auth-chip--right">
        <BarChart3 :size="24" />
      </div>
      <div class="auth-chip auth-chip--bottom">
        <ShieldCheck :size="26" />
      </div>
      <div class="auth-line auth-line--one" />
      <div class="auth-line auth-line--two" />
    </div>
  </aside>
</template>
