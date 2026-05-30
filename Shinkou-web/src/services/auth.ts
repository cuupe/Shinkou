import { request } from "../api/http";
import type {
  ActivatePayload,
  AuthResponse,
  InvitationDetail,
  LoginPayload,
} from "../types/auth";

export function login(payload: LoginPayload) {
  return request<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getInvitation(token: string) {
  return request<InvitationDetail>(
    `/api/invitations/${encodeURIComponent(token)}`,
  );
}

export function activateAccount(payload: ActivatePayload) {
  return request<AuthResponse>("/api/auth/activate", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function persistAuth(response: AuthResponse) {
  localStorage.setItem("Shinkou_access_token", response.accessToken);
  localStorage.setItem("Shinkou_user", JSON.stringify(response.user));
  localStorage.setItem(
    "Shinkou_workspaces",
    JSON.stringify(response.workspaces),
  );
  localStorage.setItem("accessToken", response.accessToken);
  localStorage.setItem("authUser", JSON.stringify(response.user));
  localStorage.setItem("workspaces", JSON.stringify(response.workspaces));
}

export function getAuthToken() {
  return localStorage.getItem("Shinkou_access_token");
}

export function getSavedUser() {
  const stored = localStorage.getItem("Shinkou_user");
  if (!stored) return null;
  try {
    return JSON.parse(stored) as AuthResponse["user"];
  } catch {
    return null;
  }
}

export function getSavedWorkspaces() {
  const stored = localStorage.getItem("Shinkou_workspaces");
  if (!stored) return [];
  try {
    return JSON.parse(stored) as AuthResponse["workspaces"];
  } catch {
    return [];
  }
}

export function clearAuth() {
  localStorage.removeItem("Shinkou_access_token");
  localStorage.removeItem("Shinkou_user");
  localStorage.removeItem("Shinkou_workspaces");
  localStorage.removeItem("accessToken");
  localStorage.removeItem("authUser");
  localStorage.removeItem("workspaces");
}

export function resolveNextPath(workspaces: AuthResponse["workspaces"]) {
  if (workspaces.length === 1) {
    return `/workspaces/${workspaces[0].id}/projects`;
  }
  return "/workspace-select";
}
