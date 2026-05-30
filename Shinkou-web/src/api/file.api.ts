import { http } from "./http";
import type {
  FileContent,
  FileSearchHit,
  FileTreeNode,
  ProjectFile,
} from "@/types/domain";
export const fileApi = {
  uploadZip: (
    workspaceId: number | string,
    projectId: number | string,
    file: File,
  ) => {
    const form = new FormData();
    form.append("file", file);
    return http.post<
      any,
      {
        projectId: number;
        workspaceId: number;
        fileCount: number;
        status: string;
      }
    >(`/workspaces/${workspaceId}/projects/${projectId}/upload-zip`, form, {
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 120000,
    });
  },
  list: (workspaceId: number | string, projectId: number | string) =>
    http.get<any, ProjectFile[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/files`,
    ),
  tree: (workspaceId: number | string, projectId: number | string) =>
    http.get<any, FileTreeNode[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/files/tree`,
    ),
  search: (
    workspaceId: number | string,
    projectId: number | string,
    keyword: string,
  ) =>
    http.get<any, FileSearchHit[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/files/search`,
      { params: { keyword } },
    ),
  content: (
    workspaceId: number | string,
    projectId: number | string,
    path: string,
  ) =>
    http.get<any, FileContent>(
      `/workspaces/${workspaceId}/projects/${projectId}/files/content`,
      { params: { path } },
    ),
  readLines: (
    workspaceId: number | string,
    projectId: number | string,
    path: string,
    start: number,
    end: number,
  ) =>
    http.get<any, FileContent>(
      `/workspaces/${workspaceId}/projects/${projectId}/files/read-lines`,
      { params: { path, start, end } },
    ),
};
