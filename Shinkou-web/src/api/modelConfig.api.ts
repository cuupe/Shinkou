import { http } from "./http";
import type { ModelConfig, ToolConfig } from "@/types/domain";
export const modelConfigApi = {
  list: (workspaceId: number | string) =>
    http.get<any, ModelConfig[]>(`/workspaces/${workspaceId}/model-configs`),
  create: (workspaceId: number | string, data: ModelConfig) =>
    http.post<any, ModelConfig>(
      `/workspaces/${workspaceId}/model-configs`,
      data,
    ),
  update: (
    workspaceId: number | string,
    configId: number | string,
    data: Partial<ModelConfig>,
  ) =>
    http.patch<any, ModelConfig>(
      `/workspaces/${workspaceId}/model-configs/${configId}`,
      data,
    ),
  remove: (workspaceId: number | string, configId: number | string) =>
    http.delete<any, { success: boolean }>(
      `/workspaces/${workspaceId}/model-configs/${configId}`,
    ),
  test: (workspaceId: number | string, configId: number | string) =>
    http.post<any, { success: boolean; latencyMs?: number; message?: string }>(
      `/workspaces/${workspaceId}/model-configs/${configId}/test`,
    ),
  tools: (workspaceId: number | string) =>
    http.get<any, ToolConfig[]>(`/workspaces/${workspaceId}/tool-configs`),
  createTool: (workspaceId: number | string, data: ToolConfig) =>
    http.post<any, ToolConfig>(`/workspaces/${workspaceId}/tool-configs`, data),
  updateTool: (
    workspaceId: number | string,
    toolConfigId: number | string,
    data: Partial<ToolConfig>,
  ) =>
    http.patch<any, ToolConfig>(
      `/workspaces/${workspaceId}/tool-configs/${toolConfigId}`,
      data,
    ),
  removeTool: (workspaceId: number | string, toolConfigId: number | string) =>
    http.delete<any, { success: boolean }>(
      `/workspaces/${workspaceId}/tool-configs/${toolConfigId}`,
    ),
};
