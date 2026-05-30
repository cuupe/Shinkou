export type Status = "ACTIVE" | "ARCHIVED" | "DELETED" | "DISABLED" | string;
export type Role = "OWNER" | "ADMIN" | "MEMBER" | "VIEWER" | string;
export type Priority = "P0" | "P1" | "P2" | "P3" | string;
export type AgentStatus =
  | "PENDING"
  | "RUNNING"
  | "COMPLETED"
  | "FAILED"
  | "CANCELLED"
  | string;

export interface CurrentUser {
  id: number;
  email: string;
  name: string;
  avatarUrl?: string | null;
  department?: string | null;
  position?: string | null;
  status: string;
}
export interface WorkspaceBrief {
  id: number;
  name: string;
  code: string;
  description?: string | null;
  role: Role;
  status?: Status;
}
export interface WorkspaceDetail extends WorkspaceBrief {
  currentUserRole?: Role;
  createdAt?: string;
  updatedAt?: string;
}
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: CurrentUser;
  workspaces: WorkspaceBrief[];
}
export interface Project {
  id: number;
  workspaceId: number;
  name: string;
  code: string;
  description?: string;
  rootPath?: string;
  fileCount: number;
  status: Status;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
}
export interface FileTreeNode {
  name: string;
  path: string;
  type: "directory" | "file";
  children?: FileTreeNode[];
}
export interface ProjectFile {
  id: number;
  workspaceId: number;
  projectId: number;
  filePath: string;
  fileName: string;
  fileExt?: string;
  language?: string;
  size?: number;
  indexed: boolean;
}
export interface FileSearchHit {
  path: string;
  line: number;
  snippet: string;
}
export interface FileContent {
  path: string;
  language?: string;
  content: string;
  start?: number;
  end?: number;
  lines?: Array<{ line: number; text: string }>;
}
export interface ToolCall {
  id?: number;
  sessionId?: string;
  toolName: string;
  arguments?: Record<string, unknown>;
  result?: Record<string, unknown>;
  status: string;
  latencyMs?: number;
  errorMessage?: string;
  createdAt?: string;
}
export interface AffectedFile {
  id?: number;
  path?: string;
  filePath: string;
  fileName?: string;
  language?: string;
  lineStart?: number;
  lineEnd?: number;
  reason: string;
  confidence?: number;
}
export interface Risk {
  id?: number;
  riskLevel?: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL" | string;
  description: string;
  suggestion?: string;
}
export interface TaskDraft {
  id: number;
  sessionId?: string;
  workspaceId?: number;
  projectId?: number;
  title: string;
  priority: Priority;
  description?: string;
  relatedFiles?: string[] | unknown;
  status: "DRAFT" | "ACCEPTED" | "REJECTED" | "CONVERTED" | string;
  createdAt?: string;
  updatedAt?: string;
}
export interface WorkTask {
  id: number;
  title: string;
  priority: Priority;
  description?: string;
  relatedFiles?: string[] | unknown;
  status: "TODO" | "READY" | "IN_PROGRESS" | "DONE" | "CONFIRMING" | string;
  sourceSessionId?: string;
  assigneeName?: string;
  createdAt?: string;
  updatedAt?: string;
}
export interface AgentSession {
  sessionId: string;
  workspaceId: number;
  projectId: number;
  requirement: string;
  status: AgentStatus;
  summary?: string;
  createdAt?: string;
  updatedAt?: string;
  errorMessage?: string;
}
export interface AgentAnalyzeResponse extends AgentSession {
  affectedFiles?: AffectedFile[];
  risks?: Risk[];
  tasks?: TaskDraft[];
  steps?: ToolCall[];
}
export interface Report {
  id: number;
  name?: string;
  title?: string;
  reportName?: string;
  reportType?: string;
  sourceRequirement?: string;
  format?: "PDF" | "Markdown" | string;
  status?: string;
  createdAt?: string;
  generatedAt?: string;
  version?: string;
}
export interface ModelConfig {
  id?: number;
  type?: "CHAT" | "EMBEDDING" | "RERANKER" | string;
  provider: string;
  model: string;
  temperature?: number;
  maxTokens?: number;
  timeoutSeconds?: number;
  dimension?: number;
  topK?: number;
  rerankTopN?: number;
  enabled?: boolean;
  isDefault?: boolean;
}
export interface ToolConfig {
  id?: number;
  name: string;
  description?: string;
  permissionLevel?: "READ" | "WRITE" | string;
  enabled: boolean;
  requireConfirm?: boolean;
}
export interface PageResult<T> {
  items: T[];
  total: number;
}
