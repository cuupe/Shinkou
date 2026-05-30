import { http } from "./http";
import type { Project } from "@/types/domain";
export const projectApi = {
  list: (workspaceId: number | string, params?: Record<string, any>) =>
    http.get<any, Project[]>(`/workspaces/${workspaceId}/projects`, { params }),
  detail: (workspaceId: number | string, projectId: number | string) =>
    http.get<any, Project>(`/workspaces/${workspaceId}/projects/${projectId}`),
  create: (
    workspaceId: number | string,
    data: Pick<Project, "name" | "code" | "description">,
  ) => http.post<any, Project>(`/workspaces/${workspaceId}/projects`, data),
  update: (
    workspaceId: number | string,
    projectId: number | string,
    data: Partial<Project>,
  ) =>
    http.patch<any, Project>(
      `/workspaces/${workspaceId}/projects/${projectId}`,
      data,
    ),
  remove: (workspaceId: number | string, projectId: number | string) =>
    http.delete<any, { success: boolean }>(
      `/workspaces/${workspaceId}/projects/${projectId}`,
    ),
};
