import axios, { AxiosError } from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export const http = axios.create({
  baseURL: API_BASE_URL || "/api",
  timeout: 30000,
  withCredentials: true,
});

http.interceptors.request.use((config) => {
  return config;
});

http.interceptors.response.use(
  (r) => {
    const payload = r.data;
    if (
      payload &&
      typeof payload === "object" &&
      ("code" in payload || "data" in payload)
    ) {
      if (payload.code && payload.code !== "SUCCESS") {
        return Promise.reject({
          status: r.status,
          code: payload.code,
          message: payload.message || "请求失败",
          payload,
        });
      }
      return payload.data;
    }
    return payload;
  },
  (error: AxiosError<any>) => {
    const status = error.response?.status;
    const payload = error.response?.data || {};
    const isNetworkError =
      !error.response ||
      error.code === "ERR_NETWORK" ||
      error.code === "ECONNABORTED";
    const message = isNetworkError
      ? "无法连接后端服务，请确认后端已启动，或在 VITE_API_PROXY_TARGET 中配置正确的 API 地址。"
      : payload.message || error.message || "请求失败";
    if (status === 401 || payload.code === "AUTH_TOKEN_EXPIRED") {
      sessionStorage.removeItem("Shinkou_session_active");
      localStorage.removeItem("Shinkou_access_token");
      localStorage.removeItem("Shinkou_user");
      localStorage.removeItem("Shinkou_workspaces");
      localStorage.removeItem("accessToken");
      localStorage.removeItem("authUser");
      localStorage.removeItem("workspaces");
      if (!location.pathname.startsWith("/login")) location.href = "/login";
    }
    return Promise.reject({ status, code: payload.code, message, payload });
  },
);

export class ApiError extends Error {
  code?: string;
  status: number;

  constructor(message: string, status: number, code?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}

export async function request<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers as HeadersInit);

  if (!headers.has("Content-Type") && init.body) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    credentials: "include",
    headers,
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new ApiError(
      payload?.message ?? "请求失败，请稍后重试",
      response.status,
      payload?.code,
    );
  }

  if (
    payload &&
    typeof payload === "object" &&
    ("code" in payload || "data" in payload)
  ) {
    const envelope = payload as { code?: string; message?: string; data?: any };

    if (envelope.code && envelope.code !== "SUCCESS") {
      throw new ApiError(
        envelope.message ?? "请求失败，请稍后重试",
        response.status,
        envelope.code,
      );
    }

    return envelope.data as T;
  }

  return payload as T;
}

export function normalizeList<T>(
  data: T[] | { items: T[]; total?: number } | any,
): T[] {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.items)) return data.items;
  return [];
}
