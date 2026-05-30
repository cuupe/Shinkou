import { http } from "./http";
import type { TaskDraft, WorkTask } from "@/types/domain";
export const taskApi = {
  drafts: (
    workspaceId: number | string,
    projectId: number | string,
    params?: Record<string, any>,
  ) =>
    http.get<any, TaskDraft[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/task-drafts`,
      { params },
    ),
  updateDraft: (
    workspaceId: number | string,
    projectId: number | string,
    draftId: number | string,
    data: Partial<TaskDraft>,
  ) =>
    http.patch<any, TaskDraft>(
      `/workspaces/${workspaceId}/projects/${projectId}/task-drafts/${draftId}`,
      data,
    ),
  convertDraft: (
    workspaceId: number | string,
    projectId: number | string,
    draftId: number | string,
  ) =>
    http.post<any, WorkTask>(
      `/workspaces/${workspaceId}/projects/${projectId}/task-drafts/${draftId}/convert`,
    ),
  tasks: (
    workspaceId: number | string,
    projectId: number | string,
    params?: Record<string, any>,
  ) =>
    http.get<any, WorkTask[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks`,
      { params },
    ),
  create: (
    workspaceId: number | string,
    projectId: number | string,
    data: Partial<WorkTask>,
  ) =>
    http.post<any, WorkTask>(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks`,
      data,
    ),
  update: (
    workspaceId: number | string,
    projectId: number | string,
    taskId: number | string,
    data: Partial<WorkTask>,
  ) =>
    http.patch<any, WorkTask>(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}`,
      data,
    ),
  remove: (
    workspaceId: number | string,
    projectId: number | string,
    taskId: number | string,
  ) =>
    http.delete<any, { success: boolean }>(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}`,
    ),
};
