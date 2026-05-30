import type { WorkspaceSummary } from "./auth";

export type WorkspaceStatus = WorkspaceSummary["status"];

export interface WorkspaceDetail extends WorkspaceSummary {
  description?: string | null;
  updatedAt?: string;
}
