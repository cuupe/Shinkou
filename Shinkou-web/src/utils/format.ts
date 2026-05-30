export function fmtDate(value?: string) {
  if (!value) return "-";
  try {
    return new Date(value).toLocaleString();
  } catch {
    return value;
  }
}

export function fmtSize(bytes?: number) {
  if (!bytes && bytes !== 0) return "-";
  const units = ["B", "KB", "MB", "GB"];
  let size = bytes;
  let index = 0;
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024;
    index += 1;
  }
  return `${size.toFixed(index ? 1 : 0)} ${units[index]}`;
}

export function asArrayFiles(value: unknown): string[] {
  if (!value) return [];
  if (Array.isArray(value)) return value as string[];
  try {
    const parsed = JSON.parse(String(value));
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function statusCn(status?: string) {
  const map: Record<string, string> = {
    ACTIVE: "正常",
    ARCHIVED: "归档",
    DELETED: "删除",
    DISABLED: "禁用",
    PENDING: "等待",
    RUNNING: "进行中",
    COMPLETED: "已完成",
    SUCCESS: "成功",
    FAILED: "失败",
    CANCELLED: "已取消",
    DRAFT: "草稿",
    ACCEPTED: "已接受",
    REJECTED: "已拒绝",
    CONVERTED: "已转任务",
    TODO: "待开发",
    READY: "待确认",
    IN_PROGRESS: "开发中",
    DONE: "已完成",
    CONFIRMING: "待确认",
    P0: "P0",
    P1: "P1",
    P2: "P2",
    P3: "P3",
    LOW: "低",
    MEDIUM: "中",
    HIGH: "高",
    CRITICAL: "严重",
  };
  return status ? map[status] || status : "-";
}
