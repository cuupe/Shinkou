import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth.store";

function defaultAuthedPath() {
  const workspaceId = localStorage.getItem("lastWorkspaceId");
  return workspaceId
    ? `/workspaces/${workspaceId}/projects`
    : "/workspace-select";
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: () =>
        localStorage.getItem("accessToken") ? defaultAuthedPath() : "/login",
    },
    {
      path: "/login",
      component: () => import("@/pages/LoginPage.vue"),
      meta: { guest: true },
    },
    {
      path: "/activate",
      component: () => import("@/pages/ActivatePage.vue"),
      meta: { guest: true },
    },
    {
      path: "/workspace-select",
      component: () => import("@/pages/WorkspaceSelectPage.vue"),
      meta: { auth: true },
    },
    {
      path: "/workspaces/:workspaceId",
      component: () => import("@/layouts/WorkspaceLayout.vue"),
      meta: { auth: true },
      children: [
        {
          path: "projects",
          component: () => import("@/pages/ProjectListPage.vue"),
        },
        {
          path: "overview",
          component: () => import("@/pages/ProjectOverviewPage.vue"),
        },
        {
          path: "files",
          component: () => import("@/pages/FilesPage.vue"),
        },
        {
          path: "code",
          component: () => import("@/pages/CodeSearchPage.vue"),
        },
        {
          path: "analysis",
          component: () => import("@/pages/AnalysisPage.vue"),
        },
        {
          path: "tasks",
          component: () => import("@/pages/TasksPage.vue"),
        },
        {
          path: "reports",
          component: () => import("@/pages/ReportsPage.vue"),
        },
        {
          path: "runs",
          component: () => import("@/pages/RunTracePage.vue"),
        },
        {
          path: "settings/models",
          component: () => import("@/pages/ModelConfigPage.vue"),
        },
        {
          path: "projects/:projectId",
          component: () => import("@/layouts/ProjectLayout.vue"),
          children: [
            {
              path: "",
              redirect: (to) =>
                `/workspaces/${to.params.workspaceId}/projects/${to.params.projectId}/overview`,
            },
            {
              path: "overview",
              component: () => import("@/pages/ProjectOverviewPage.vue"),
            },
            {
              path: "files",
              component: () => import("@/pages/FilesPage.vue"),
            },
            {
              path: "code",
              component: () => import("@/pages/CodeSearchPage.vue"),
            },
            {
              path: "analysis",
              component: () => import("@/pages/AnalysisPage.vue"),
            },
            {
              path: "tasks",
              component: () => import("@/pages/TasksPage.vue"),
            },
            {
              path: "reports",
              component: () => import("@/pages/ReportsPage.vue"),
            },
            {
              path: "runs",
              component: () => import("@/pages/RunTracePage.vue"),
            },
          ],
        },
      ],
    },
    {
      path: "/:pathMatch(.*)*",
      component: () => import("@/pages/NotFoundPage.vue"),
    },
  ],
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();

  if (to.meta.guest && auth.accessToken) {
    return defaultAuthedPath();
  }

  if (!to.meta.auth) return true;

  if (!auth.accessToken) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  if (!auth.hydrated && auth.workspaces.length === 0) {
    try {
      await auth.refreshMe();
    } catch {
      auth.logout();
      return { path: "/login", query: { redirect: to.fullPath } };
    }
  }

  const workspaceId = to.params.workspaceId
    ? Number(to.params.workspaceId)
    : null;
  if (workspaceId) {
    const hasAccess = auth.workspaces.some(
      (workspace) =>
        Number(workspace.id) === workspaceId && workspace.status === "ACTIVE",
    );
    if (!hasAccess) {
      return {
        path: "/workspace-select",
        query: { denied: String(workspaceId) },
      };
    }
    localStorage.setItem("lastWorkspaceId", String(workspaceId));
  }

  return true;
});

export default router;
