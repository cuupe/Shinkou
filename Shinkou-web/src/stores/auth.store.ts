import { defineStore } from "pinia";
import { http } from "@/api/http";
import { workspaceApi } from "@/api/workspace.api";
import type { CurrentUser, WorkspaceBrief } from "@/types/domain";

type MeResponse = { user: CurrentUser; workspaces: WorkspaceBrief[] };

function clearStoredSession() {
  sessionStorage.removeItem("Shinkou_session_active");
  localStorage.removeItem("Shinkou_access_token");
  localStorage.removeItem("Shinkou_user");
  localStorage.removeItem("Shinkou_workspaces");
  localStorage.removeItem("accessToken");
  localStorage.removeItem("authUser");
  localStorage.removeItem("workspaces");
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    sessionActive:
      sessionStorage.getItem("Shinkou_session_active") === "true" ||
      Boolean(
        localStorage.getItem("Shinkou_access_token") ||
          localStorage.getItem("accessToken"),
      ),
    user: null as CurrentUser | null,
    workspaces: [] as WorkspaceBrief[],
    hydrated: false,
  }),
  getters: { isAuthed: (s) => s.sessionActive && Boolean(s.user) },
  actions: {
    setSession(data: {
      accessToken?: string;
      user: CurrentUser;
      workspaces: WorkspaceBrief[];
    }) {
      this.sessionActive = true;
      this.user = data.user;
      this.workspaces = data.workspaces;
      this.hydrated = true;
      sessionStorage.setItem("Shinkou_session_active", "true");
      localStorage.removeItem("Shinkou_access_token");
      localStorage.removeItem("accessToken");
      localStorage.removeItem("Shinkou_user");
      localStorage.removeItem("authUser");
      localStorage.removeItem("Shinkou_workspaces");
      localStorage.removeItem("workspaces");
    },
    async refreshMe() {
      const data = await (
        await import("@/api/http")
      ).http.get<any, MeResponse>("/auth/me");
      this.sessionActive = true;
      this.user = data.user;
      this.workspaces = data.workspaces;
      this.hydrated = true;
      sessionStorage.setItem("Shinkou_session_active", "true");
    },
    async refreshWorkspaces() {
      this.workspaces = await workspaceApi.my();
    },
    clearSession() {
      this.sessionActive = false;
      this.user = null;
      this.workspaces = [];
      this.hydrated = false;
      clearStoredSession();
    },
    async logout() {
      await http.post<any, { success: boolean }>("/auth/logout");
      this.clearSession();
    },
  },
});
