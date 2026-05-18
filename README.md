# ShinkouDev

ShinkouDev 是一个面向研发团队的需求变更影响分析多智能体平台。

用户可以上传或接入项目代码，系统建立项目文件索引后，由多个智能体协作完成需求理解、代码探索、影响分析、任务拆解、测试建议和报告生成。

ShinkouDev 不定位为 IDE 编码助手，不直接替代 Cursor、Claude Code 或 GitHub Copilot。它主要面向需求评审、研发协作、任务拆解和变更风险评估场景。



## 项目定位

IDE 编码助手主要解决：

\- 帮开发者写代码
\- 修改文件
\- 解释函数
\- 生成测试
\- 在本地开发环境中辅助编码

ShinkouDev 主要解决：

\- 一个需求会影响哪些模块
\- 哪些文件、接口、页面、数据表可能需要修改
\- 这个需求有哪些技术风险
\- 需要补充哪些测试场景
\- 如何拆分开发任务
\- 如何生成可审计的分析报告
\- 如何沉淀团队级研发知识



## MVP 目标

Version 0.1(beta):

```text
创建项目
→ 上传项目 ZIP
→ 扫描项目文件
→ 建立文件索引
→ 用户输入需求
→ 智能体搜索代码并读取文件片段
→ 生成影响分析
→ 生成任务草稿
→ 展示 Agent 工具调用轨迹
```



## MVP 功能

- 项目管理
- 上传项目 ZIP
- 项目文件扫描
- 项目文件树展示
- 代码文本搜索
- 指定文件片段读取
- 需求影响分析
- 任务草稿生成
- Agent 工具调用轨迹展示



## 后续增强

- Git 仓库接入
- 分支 / PR Diff 分析
- RAG 语义检索
- Reranker 精排
- 多智能体 LangGraph 编排
- 报告中心
- PDF 导出
- 测试建议生成
- 模型配置
- 工具权限控制
- Human-in-the-loop 确认机制



## 技术栈

### Frontend

- Vue
- TypeScript
- Vite
- Tailwind CSS
- Monaco Editor
- TanStack Query

### Backend

- Spring Boot 3
- PostgreSQL
- Redis
- Docker Compose

### AI Service

- FastAPI
- Pydantic
- LangGraph

### RAG

- 关键词搜索 / ripgrep
- Embedding
- pgvector
- Reranker



