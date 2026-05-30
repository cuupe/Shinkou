import { defineStore } from "pinia";
import { projectApi } from "@/api/project.api";
import type { Project } from "@/types/domain";
export const useProjectStore = defineStore("project", {
  state: () => ({
    currentProjectId: null as number | null,
    currentProject: null as Project | null,
  }),
  actions: {
    async load(workspaceId: number | string, projectId: number | string) {
      this.currentProjectId = Number(projectId);
      this.currentProject = await projectApi.detail(workspaceId, projectId);
    },
  },
});
