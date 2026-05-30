import { request } from "../api/http";
import type { WorkspaceSummary, User } from "../types/auth";

export interface WorkspaceResponse {
  workspaces: WorkspaceSummary[];
  user: User;
}

export function getMyWorkspaces() {
  return request<WorkspaceSummary[]>("/api/workspaces/my");
}

export function getWorkspaceDetail(workspaceId: string) {
  return request<WorkspaceSummary>(
    `/api/workspaces/${encodeURIComponent(workspaceId)}`,
  );
}
