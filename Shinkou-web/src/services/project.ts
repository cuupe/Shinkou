import { request } from "../api/http";
import type {
  ProjectSummary,
  FileNode,
  ReportSummary,
  TaskCard,
} from "../types/project";

export function getProjects(workspaceId: string) {
  return request<ProjectSummary[]>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects`,
  );
}

export function getProjectDetail(workspaceId: string, projectId: string) {
  return request<ProjectSummary>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects/${encodeURIComponent(projectId)}`,
  );
}

export function getProjectFiles(workspaceId: string, projectId: string) {
  return request<FileNode[]>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects/${encodeURIComponent(projectId)}/files`,
  );
}

export function searchProjectFiles(
  workspaceId: string,
  projectId: string,
  query: string,
) {
  return request<FileNode[]>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects/${encodeURIComponent(projectId)}/files/search?q=${encodeURIComponent(query)}`,
  );
}

export function readProjectFileLines(
  workspaceId: string,
  projectId: string,
  path: string,
) {
  return request<{ lines: string[] }>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects/${encodeURIComponent(projectId)}/files/read-lines?path=${encodeURIComponent(path)}`,
  );
}

export function getProjectReports(workspaceId: string, projectId: string) {
  return request<ReportSummary[]>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects/${encodeURIComponent(projectId)}/reports`,
  );
}

export function getProjectTasks(workspaceId: string, projectId: string) {
  return request<TaskCard[]>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}/projects/${encodeURIComponent(projectId)}/tasks`,
  );
}
