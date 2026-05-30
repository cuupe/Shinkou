package com.cuupe.shinkou.common.enums;

/**
 * 枚举说明
 * | tool_name             | 说明             |
 * | --------------------- | ---------------- |
 * | `search_text`         | 搜索文件内容     |
 * | `read_file`           | 读取完整文件     |
 * | `read_lines`          | 读取文件指定行   |
 * | `list_files`          | 获取项目文件列表 |
 * | `analyze_requirement` | 分析需求         |
 * | `generate_tasks`      | 生成任务草稿     |
 */
public enum AgentTool {
    SEARCH_TEXT,
    READ_FILE,
    READ_LINES,
    LIST_FILES,
    ANALYZE_REQUIREMENT,
    GENERATE_TASKS
}
