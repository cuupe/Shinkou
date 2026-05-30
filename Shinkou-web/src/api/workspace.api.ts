import { http } from "./http";
import type { WorkspaceBrief, WorkspaceDetail } from "@/types/domain";
export const workspaceApi = {
  my: () => http.get<any, WorkspaceBrief[]>("/workspaces/my"),
  detail: (workspaceId: number | string) =>
    http.get<any, WorkspaceDetail>(`/workspaces/${workspaceId}`),
};
