export interface ProjectSummary {
  id: number | string;
  name: string;
  description: string;
  language: string;
  fileCount: number;
  lastUpdated: string;
  status?: "ACTIVE" | "ARCHIVED" | "PENDING";
}

export interface FileNode {
  id: string;
  name: string;
  path: string;
  type: "file" | "folder";
  children?: FileNode[];
  changedLines?: string;
}

export interface ReportSummary {
  id: string;
  title: string;
  category: string;
  source: string;
  format: "PDF" | "Markdown";
  createdAt: string;
  status: "已完成" | "进行中" | "失败";
}

export interface TaskCard {
  id: string;
  title: string;
  status: "待确认" | "待开发" | "开发中" | "已完成";
  summary: string;
  owner: string;
  priority: "P1" | "P2" | "P3";
  updatedAt: string;
  tags: string[];
}
