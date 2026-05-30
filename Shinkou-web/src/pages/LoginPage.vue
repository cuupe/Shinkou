<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useMutation } from "@tanstack/vue-query";
import { RouterLink, useRoute, useRouter } from "vue-router";
import {
  Eye,
  EyeOff,
  Info,
  Lock,
  LockKeyhole,
  Mail,
  Send,
} from "lucide-vue-next";
import AuthVisualPanel from "../components/AuthVisualPanel.vue";
import ShinkouLogo from "../components/ShinkouLogo.vue";
import { useAuthStore } from "../stores/auth.store";
import { login, persistAuth, resolveNextPath } from "../services/auth";
import { ApiError } from "../api/http";

const form = reactive({
  email: "",
  password: "",
  remember: true,
});

const showPassword = ref(false);
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const loginMutation = useMutation({
  mutationFn: login,
  onSuccess(response) {
    persistAuth(response);
    authStore.setSession(response);
    const redirect =
      typeof route.query.redirect === "string"
        ? route.query.redirect
        : resolveNextPath(response.workspaces);
    router.replace(redirect);
  },
});

const errorMessage = computed(() => {
  const error = loginMutation.error.value;
  if (!error) return "";
  if (error instanceof ApiError) return error.message;
  return "登录失败，请稍后重试";
});

function submitLogin() {
  if (!form.email || !form.password || loginMutation.isPending.value) return;
  loginMutation.mutate({ email: form.email, password: form.password });
}
</script>

<template>
  <main class="auth-page auth-page--login">
    <div class="auth-shell auth-shell--login">
      <AuthVisualPanel class="auth-shell__visual" variant="login" />

      <section class="auth-form-stage">
        <div class="mb-8 flex justify-center lg:hidden">
          <ShinkouLogo size="lg" />
        </div>

        <form class="auth-card auth-login-card" @submit.prevent="submitLogin">
          <div>
            <h1 class="auth-card-title">登录 Shinkou</h1>
            <p class="auth-card-subtitle">
              仅限企业内部成员使用，请使用企业账号登录
            </p>
          </div>

          <div class="mt-10 space-y-7">
            <label class="block">
              <span class="auth-label">企业邮箱</span>
              <span class="auth-input-wrap">
                <Mail :size="23" />
                <input
                  v-model.trim="form.email"
                  class="auth-input"
                  type="email"
                  placeholder="请输入企业邮箱"
                  autocomplete="email"
                />
              </span>
            </label>

            <label class="block">
              <span class="auth-label">密码</span>
              <span class="auth-input-wrap">
                <LockKeyhole :size="23" />
                <input
                  v-model="form.password"
                  class="auth-input auth-input--password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                />
                <button
                  class="auth-input-action"
                  type="button"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword"
                >
                  <EyeOff v-if="showPassword" :size="22" />
                  <Eye v-else :size="22" />
                </button>
              </span>
            </label>
          </div>

          <div class="mt-7 flex items-center justify-between gap-4 text-base">
            <label
              class="inline-flex items-center gap-3 font-semibold text-slate-900"
            >
              <input
                v-model="form.remember"
                class="auth-checkbox"
                type="checkbox"
              />
              记住我
            </label>
            <a
              class="font-semibold text-brand-600 hover:text-brand-700"
              href="mailto:admin@shinkou.local"
            >
              忘记密码？
            </a>
          </div>

          <p
            v-if="errorMessage"
            class="mt-6 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-600"
          >
            {{ errorMessage }}
          </p>

          <button
            class="auth-primary-button mt-7"
            type="submit"
            :disabled="loginMutation.isPending.value"
          >
            {{ loginMutation.isPending.value ? "登录中..." : "登录" }}
          </button>

          <div class="auth-inline-note mt-2">
            <Info :size="18" />
            若您加入多个工作区，登录后将在下一步选择工作区
          </div>

          <div class="auth-divider">
            <span>或</span>
          </div>

          <RouterLink class="auth-outline-button" to="/activate">
            <Send :size="22" />
            通过邀请激活账号
          </RouterLink>
        </form>

        <div class="auth-bottom-note">
          <Lock :size="19" />
          仅限受邀企业成员使用
        </div>
      </section>
    </div>
  </main>
</template>
