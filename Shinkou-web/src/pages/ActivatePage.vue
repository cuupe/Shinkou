<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { RouterLink, useRouter } from "vue-router";
import {
  Building2,
  CheckCircle2,
  ChevronDown,
  Copy,
  Eye,
  EyeOff,
  Info,
  Lock,
  LockKeyhole,
  ShieldCheck,
  UserPlus,
} from "lucide-vue-next";
import AuthVisualPanel from "../components/AuthVisualPanel.vue";
import ShinkouLogo from "../components/ShinkouLogo.vue";
import { useAuthStore } from "../stores/auth.store";
import {
  activateAccount,
  getInvitation,
  persistAuth,
  resolveNextPath,
} from "../services/auth";
import { ApiError } from "../api/http";

const params = new URLSearchParams(window.location.search);
const token = params.get("token") ?? "inv_demo_admin_token";
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  password: "",
  confirmPassword: "",
});

const invitationQuery = useQuery({
  queryKey: ["invitation", token],
  queryFn: () => getInvitation(token),
  enabled: Boolean(token),
});

const invitation = computed(() => invitationQuery.data.value);
const workspaceName = computed(
  () => invitation.value?.workspace.name ?? "Shinkou Engineering",
);
const workspaceCode = computed(
  () => invitation.value?.workspace.code ?? "shinkou-engineering",
);
const invitationEmail = computed(
  () => invitation.value?.email ?? "zhangsan@shinkou.com",
);
const invitationName = computed(() => invitation.value?.name ?? "张三");
const invitationDepartment = computed(
  () => invitation.value?.department ?? "研发中心",
);
const invitationPosition = computed(
  () => invitation.value?.position ?? "后端工程师",
);
const invitationRole = computed(() => {
  const role = invitation.value?.role ?? "MEMBER";
  return role === "MEMBER" ? "MEMBER / 普通成员" : role;
});

const passwordChecks = computed(() => ({
  length: form.password.length >= 8 && form.password.length <= 32,
  lower: /[a-z]/.test(form.password),
  number: /\d/.test(form.password),
  special: /[!@#$%^&*]/.test(form.password),
}));

const passwordValid = computed(() =>
  Object.values(passwordChecks.value).every(Boolean),
);
const passwordsMatch = computed(() => form.password === form.confirmPassword);

const activateMutation = useMutation({
  mutationFn: activateAccount,
  onSuccess(response) {
    persistAuth(response);
    authStore.setSession(response);
    router.replace(resolveNextPath(response.workspaces));
  },
});

const errorMessage = computed(() => {
  const error = activateMutation.error.value || invitationQuery.error.value;
  if (!error) return "";
  if (error instanceof ApiError) return error.message;
  return "操作失败，请稍后重试";
});

function copyToken() {
  navigator.clipboard?.writeText(token);
}

function submitActivate() {
  if (
    !passwordValid.value ||
    !passwordsMatch.value ||
    activateMutation.isPending.value
  )
    return;
  activateMutation.mutate({
    invitationToken: token,
    password: form.password,
    confirmPassword: form.confirmPassword,
  });
}
</script>

<template>
  <main class="auth-page auth-page--activate">
    <div class="auth-shell auth-shell--activate">
      <AuthVisualPanel class="auth-shell__visual" variant="activate" />

      <section class="auth-form-stage auth-form-stage--activate">
        <div class="mb-7 lg:hidden">
          <ShinkouLogo size="lg" />
        </div>

        <form
          class="auth-card auth-activate-card"
          @submit.prevent="submitActivate"
        >
          <div>
            <h1 class="auth-card-title auth-card-title--sm">激活账号</h1>
            <p class="auth-card-subtitle">
              您已收到加入工作区的邀请，请完成以下信息以激活账号。
            </p>
          </div>

          <div class="auth-steps">
            <div class="auth-step auth-step--active">
              <span>1</span>
              验证邀请
            </div>
            <div class="auth-step-line" />
            <div class="auth-step">
              <span>2</span>
              完善资料
            </div>
            <div class="auth-step-line" />
            <div class="auth-step">
              <span>3</span>
              设置密码
            </div>
          </div>

          <section class="auth-workspace-card">
            <div class="flex min-w-0 items-center gap-5">
              <div class="auth-workspace-icon">
                <Building2 :size="34" />
              </div>
              <div class="min-w-0">
                <p class="text-sm font-semibold text-slate-600">将加入工作区</p>
                <h2 class="mt-1 truncate text-2xl font-black text-slate-950">
                  {{ workspaceName }}
                </h2>
                <p class="mt-2 flex items-center gap-2 text-sm text-slate-500">
                  工作区代码：
                  <span class="truncate">{{ workspaceCode }}</span>
                  <button
                    class="text-slate-500 hover:text-brand-600"
                    type="button"
                    aria-label="复制工作区代码"
                    @click="copyToken"
                  >
                    <Copy :size="18" />
                  </button>
                </p>
              </div>
            </div>
            <div class="shrink-0">
              <p class="mb-2 text-sm font-semibold text-slate-600">角色</p>
              <span class="auth-role-badge">{{ invitationRole }}</span>
            </div>
          </section>

          <div class="mt-7 grid gap-x-7 gap-y-5">
            <label class="auth-field-row">
              <span class="auth-label">邀请码 / 激活码</span>
              <span class="auth-input-wrap">
                <input class="auth-input" :value="token" readonly />
                <span class="auth-verified">
                  <CheckCircle2 :size="18" />
                  验证通过
                </span>
              </span>
            </label>

            <label class="auth-field-row">
              <span class="auth-label">企业邮箱</span>
              <span class="auth-input-wrap">
                <input
                  class="auth-input auth-input--readonly"
                  :value="invitationEmail"
                  readonly
                />
                <Lock :size="18" class="auth-inline-icon" />
              </span>
            </label>

            <label class="auth-field-row">
              <span class="auth-label">姓名</span>
              <input class="auth-input" :value="invitationName" readonly />
            </label>

            <label class="auth-field-row">
              <span class="auth-label">部门</span>
              <span class="auth-input-wrap">
                <input
                  class="auth-input"
                  :value="invitationDepartment"
                  readonly
                />
                <ChevronDown :size="18" class="auth-inline-icon" />
              </span>
            </label>

            <label class="auth-field-row">
              <span class="auth-label">职位</span>
              <input class="auth-input" :value="invitationPosition" readonly />
            </label>

            <label class="auth-field-row">
              <span class="auth-label">设置密码</span>
              <span class="auth-input-wrap">
                <LockKeyhole :size="20" />
                <input
                  v-model="form.password"
                  class="auth-input auth-input--password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  placeholder="请输入 8-32 位密码"
                />
                <button
                  class="auth-input-action"
                  type="button"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword"
                >
                  <EyeOff v-if="showPassword" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </span>
            </label>
          </div>

          <div class="auth-password-rules">
            <span :class="{ 'is-pass': passwordChecks.length }"
              ><CheckCircle2 :size="15" />8-32 个字符</span
            >
            <span :class="{ 'is-pass': passwordChecks.lower }"
              ><CheckCircle2 :size="15" />包含大小写字母</span
            >
            <span :class="{ 'is-pass': passwordChecks.number }"
              ><CheckCircle2 :size="15" />包含数字</span
            >
            <span :class="{ 'is-pass': passwordChecks.special }"
              ><CheckCircle2 :size="15" />包含特殊字符（如 !@#$%^&*）</span
            >
          </div>

          <label class="auth-field-row mt-4">
            <span class="auth-label">确认密码</span>
            <span class="auth-input-wrap">
              <LockKeyhole :size="20" />
              <input
                v-model="form.confirmPassword"
                class="auth-input auth-input--password"
                :type="showConfirmPassword ? 'text' : 'password'"
                autocomplete="new-password"
                placeholder="请再次输入密码"
              />
              <button
                class="auth-input-action"
                type="button"
                :aria-label="showConfirmPassword ? '隐藏密码' : '显示密码'"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <EyeOff v-if="showConfirmPassword" :size="20" />
                <Eye v-else :size="20" />
              </button>
            </span>
          </label>

          <p
            v-if="form.confirmPassword && !passwordsMatch"
            class="mt-3 text-sm font-semibold text-red-600"
          >
            两次输入的密码不一致
          </p>
          <p
            v-if="errorMessage"
            class="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-600"
          >
            {{ errorMessage }}
          </p>

          <button
            class="auth-primary-button mt-5"
            type="submit"
            :disabled="
              activateMutation.isPending.value ||
              !passwordValid ||
              !passwordsMatch
            "
          >
            <UserPlus :size="22" />
            {{
              activateMutation.isPending.value ? "激活中..." : "激活并进入系统"
            }}
          </button>

          <RouterLink
            class="mt-5 block text-center text-base font-semibold text-brand-600 hover:text-brand-700"
            to="/login"
          >
            返回登录
          </RouterLink>

          <div class="auth-bottom-note auth-bottom-note--inside">
            <Info :size="18" />
            若您的账号已存在，激活后将自动加入该工作区
          </div>
        </form>
      </section>
    </div>
  </main>
</template>
