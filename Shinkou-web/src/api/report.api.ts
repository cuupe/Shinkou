import { http } from "./http";
import type { Report } from "@/types/domain";
export const reportApi = {
  list: (
    workspaceId: number | string,
    projectId: number | string,
    params?: Record<string, any>,
  ) =>
    http.get<any, Report[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/reports`,
      { params },
    ),
  create: (
    workspaceId: number | string,
    projectId: number | string,
    data: Record<string, any>,
  ) =>
    http.post<any, Report>(
      `/workspaces/${workspaceId}/projects/${projectId}/reports`,
      data,
    ),
  detail: (
    workspaceId: number | string,
    projectId: number | string,
    reportId: number | string,
  ) =>
    http.get<any, Report>(
      `/workspaces/${workspaceId}/projects/${projectId}/reports/${reportId}`,
    ),
  markdown: (
    workspaceId: number | string,
    projectId: number | string,
    reportId: number | string,
  ) =>
    http.get<any, string>(
      `/workspaces/${workspaceId}/projects/${projectId}/reports/${reportId}/markdown`,
    ),
  remove: (
    workspaceId: number | string,
    projectId: number | string,
    reportId: number | string,
  ) =>
    http.delete<any, { success: boolean }>(
      `/workspaces/${workspaceId}/projects/${projectId}/reports/${reportId}`,
    ),
  downloadUrl: (
    workspaceId: number | string,
    projectId: number | string,
    reportId: number | string,
  ) =>
    `/api/workspaces/${workspaceId}/projects/${projectId}/reports/${reportId}/download`,
};
