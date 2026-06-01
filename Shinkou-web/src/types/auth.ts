export type UserStatus = "PENDING" | "ACTIVE" | "DISABLED" | "LOCKED";
export type WorkspaceRole = "OWNER" | "ADMIN" | "MEMBER" | "VIEWER" | "AUDITOR";
export type WorkspaceStatus = "ACTIVE" | "DISABLED";

export interface User {
  id: number;
  email: string;
  name: string;
  avatarUrl: string | null;
  department?: string | null;
  position?: string | null;
  status: UserStatus;
}

export interface WorkspaceSummary {
  id: number;
  name: string;
  code: string;
  description?: string | null;
  role: WorkspaceRole;
  status?: WorkspaceStatus;
}

export interface AuthResponse {
  accessToken?: string;
  tokenType?: "Bearer";
  expiresIn?: number;
  user: User;
  workspaces: WorkspaceSummary[];
  activatedWorkspace?: WorkspaceSummary;
}

export interface InvitationDetail {
  token: string;
  email: string;
  name: string;
  department: string;
  position: string;
  role: WorkspaceRole;
  status: "PENDING" | "ACCEPTED" | "EXPIRED" | "REVOKED";
  expiresAt: string;
  workspace: {
    id: number;
    name: string;
    code: string;
  };
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface ActivatePayload {
  invitationToken: string;
  password: string;
  confirmPassword: string;
}
