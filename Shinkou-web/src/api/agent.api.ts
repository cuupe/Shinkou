import { http } from "./http";
import type {
  AffectedFile,
  AgentAnalyzeResponse,
  AgentSession,
  PageResult,
  Risk,
  TaskDraft,
  ToolCall,
} from "@/types/domain";
export const agentApi = {
  analyze: (
    workspaceId: number | string,
    projectId: number | string,
    requirement: string,
  ) =>
    http.post<any, AgentAnalyzeResponse>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent/analyze`,
      { requirement },
      { timeout: 120000 },
    ),
  sessions: (
    workspaceId: number | string,
    projectId: number | string,
    params?: Record<string, any>,
  ) =>
    http.get<any, PageResult<AgentSession> | AgentSession[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions`,
      { params },
    ),
  session: (
    workspaceId: number | string,
    projectId: number | string,
    sessionId: string,
  ) =>
    http.get<any, AgentSession>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions/${sessionId}`,
    ),
  toolCalls: (
    workspaceId: number | string,
    projectId: number | string,
    sessionId: string,
  ) =>
    http.get<any, ToolCall[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions/${sessionId}/tool-calls`,
    ),
  affectedFiles: (
    workspaceId: number | string,
    projectId: number | string,
    sessionId: string,
  ) =>
    http.get<any, AffectedFile[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions/${sessionId}/affected-files`,
    ),
  risks: (
    workspaceId: number | string,
    projectId: number | string,
    sessionId: string,
  ) =>
    http.get<any, Risk[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions/${sessionId}/risks`,
    ),
  taskDrafts: (
    workspaceId: number | string,
    projectId: number | string,
    sessionId: string,
  ) =>
    http.get<any, TaskDraft[]>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions/${sessionId}/task-drafts`,
    ),
  generateReport: (
    workspaceId: number | string,
    projectId: number | string,
    sessionId: string,
  ) =>
    http.post<any, any>(
      `/workspaces/${workspaceId}/projects/${projectId}/agent-sessions/${sessionId}/reports`,
    ),
};
