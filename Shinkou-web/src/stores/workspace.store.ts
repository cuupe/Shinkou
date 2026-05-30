import { defineStore } from "pinia";
import { workspaceApi } from "@/api/workspace.api";
import type { WorkspaceDetail } from "@/types/domain";
export const useWorkspaceStore = defineStore("workspace", {
  state: () => ({
    currentWorkspaceId: null as number | null,
    currentWorkspace: null as WorkspaceDetail | null,
  }),
  actions: {
    async load(workspaceId: number | string) {
      this.currentWorkspaceId = Number(workspaceId);
      this.currentWorkspace = await workspaceApi.detail(workspaceId);
    },
  },
});
