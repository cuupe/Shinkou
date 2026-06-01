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

export function persistAuth(_response: AuthResponse) {
  sessionStorage.setItem("Shinkou_session_active", "true");
  clearLegacyAuthStorage();
}

export function getAuthToken() {
  return null;
}

export function getSavedUser() {
  return null;
}

export function getSavedWorkspaces() {
  return [];
}

export function clearAuth() {
  sessionStorage.removeItem("Shinkou_session_active");
  clearLegacyAuthStorage();
}

function clearLegacyAuthStorage() {
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
