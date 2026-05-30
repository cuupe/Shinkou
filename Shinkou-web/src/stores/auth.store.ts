import { defineStore } from "pinia";
import { workspaceApi } from "@/api/workspace.api";
import type { CurrentUser, WorkspaceBrief } from "@/types/domain";

type MeResponse = { user: CurrentUser; workspaces: WorkspaceBrief[] };

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: localStorage.getItem("accessToken") as string | null,
    user: JSON.parse(
      localStorage.getItem("authUser") || "null",
    ) as CurrentUser | null,
    workspaces: JSON.parse(
      localStorage.getItem("workspaces") || "[]",
    ) as WorkspaceBrief[],
    hydrated: false,
  }),
  getters: { isAuthed: (s) => Boolean(s.accessToken) },
  actions: {
    setSession(data: {
      accessToken: string;
      user: CurrentUser;
      workspaces: WorkspaceBrief[];
    }) {
      this.accessToken = data.accessToken;
      this.user = data.user;
      this.workspaces = data.workspaces;
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("authUser", JSON.stringify(data.user));
      localStorage.setItem("workspaces", JSON.stringify(data.workspaces));
    },
    async refreshMe() {
      if (!this.accessToken) return;
      const data = await (
        await import("@/api/http")
      ).http.get<any, MeResponse>("/auth/me");
      this.user = data.user;
      this.workspaces = data.workspaces;
      this.hydrated = true;
      localStorage.setItem("authUser", JSON.stringify(data.user));
      localStorage.setItem("workspaces", JSON.stringify(data.workspaces));
    },
    async refreshWorkspaces() {
      this.workspaces = await workspaceApi.my();
      localStorage.setItem("workspaces", JSON.stringify(this.workspaces));
    },
    logout() {
      this.accessToken = null;
      this.user = null;
      this.workspaces = [];
      localStorage.removeItem("accessToken");
      localStorage.removeItem("authUser");
      localStorage.removeItem("workspaces");
    },
  },
});
