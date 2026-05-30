CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),

    name VARCHAR(100) NOT NULL,
    avatar_url TEXT,

    department VARCHAR(100),
    position VARCHAR(100),

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    last_login_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workspace_members (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_workspace_user UNIQUE (workspace_id, user_id)
);

CREATE TABLE invitations (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,

    email VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    department VARCHAR(100),
    position VARCHAR(100),

    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',

    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    invited_by BIGINT,

    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,

    root_path TEXT,
    file_count INTEGER NOT NULL DEFAULT 0,

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_workspace_code UNIQUE (workspace_id, code)
);

ALTER TABLE workspaces
ADD CONSTRAINT fk_workspaces_created_by
FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE workspace_members
ADD CONSTRAINT fk_workspace_members_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE workspace_members
ADD CONSTRAINT fk_workspace_members_user
FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE invitations
ADD CONSTRAINT fk_invitations_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE invitations
ADD CONSTRAINT fk_invitations_invited_by
FOREIGN KEY (invited_by) REFERENCES users(id);

ALTER TABLE projects
ADD CONSTRAINT fk_projects_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE projects
ADD CONSTRAINT fk_projects_created_by
FOREIGN KEY (created_by) REFERENCES users(id);

CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_users_status
ON users(status);

CREATE INDEX idx_workspaces_code
ON workspaces(code);

CREATE INDEX idx_workspaces_status
ON workspaces(status);

CREATE INDEX idx_workspace_members_workspace_id
ON workspace_members(workspace_id);

CREATE INDEX idx_workspace_members_user_id
ON workspace_members(user_id);

CREATE INDEX idx_workspace_members_role
ON workspace_members(role);

CREATE INDEX idx_invitations_token
ON invitations(token);

CREATE INDEX idx_invitations_email
ON invitations(email);

CREATE INDEX idx_invitations_workspace_id
ON invitations(workspace_id);

CREATE INDEX idx_invitations_status
ON invitations(status);

CREATE UNIQUE INDEX uk_pending_invitation_workspace_email
ON invitations(workspace_id, email)
WHERE status = 'PENDING';

CREATE INDEX idx_projects_workspace_id
ON projects(workspace_id);

CREATE INDEX idx_projects_created_by
ON projects(created_by);

CREATE INDEX idx_projects_status
ON projects(status);

CREATE INDEX idx_projects_workspace_status
ON projects(workspace_id, status);

INSERT INTO workspaces (
    name,
    code,
    description,
    status
)
VALUES (
    'Shinkou Engineering',
    'Shinkou-engineering',
    'Shinkou 示例研发团队',
    'ACTIVE'
);

INSERT INTO invitations (
    workspace_id,
    email,
    name,
    department,
    position,
    role,
    token,
    status,
    expires_at
)
VALUES (
    1,
    'admin@company.com',
    '系统管理员',
    '研发平台部',
    '平台管理员',
    'ADMIN',
    'inv_demo_admin_token',
    'PENDING',
    NOW() + INTERVAL '30 days'
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT,
    user_id BIGINT,
    
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id BIGINT,
    
    result VARCHAR(50) NOT NULL,
    
    ip_address VARCHAR(100),
    user_agent TEXT,
    
    request_id VARCHAR(100),
    
    message TEXT,
    detail JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

);

CREATE TABLE project_files (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    file_path TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_ext VARCHAR(50),
    language VARCHAR(50),

    size BIGINT,
    checksum VARCHAR(128),

    indexed BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_file_path UNIQUE (project_id, file_path)
);

CREATE TABLE agent_sessions (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL UNIQUE,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    requirement TEXT NOT NULL,

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    summary TEXT,
    error_message TEXT,

    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE agent_tool_calls (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    tool_name VARCHAR(100) NOT NULL,

    arguments JSONB,
    result JSONB,

    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',

    latency_ms INTEGER,

    error_message TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analysis_affected_files (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    file_path TEXT NOT NULL,
    file_name VARCHAR(255),
    language VARCHAR(50),

    line_start INTEGER,
    line_end INTEGER,

    reason TEXT NOT NULL,

    confidence NUMERIC(5, 4),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analysis_risks (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    risk_level VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',

    description TEXT NOT NULL,

    suggestion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analysis_task_drafts (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,

    priority VARCHAR(50) NOT NULL DEFAULT 'P2',

    description TEXT,

    related_files JSONB,

    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE project_files
ADD CONSTRAINT fk_project_files_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE project_files
ADD CONSTRAINT fk_project_files_project
FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE agent_sessions
ADD CONSTRAINT fk_agent_sessions_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE agent_sessions
ADD CONSTRAINT fk_agent_sessions_project
FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE agent_sessions
ADD CONSTRAINT fk_agent_sessions_user
FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_project_files_workspace_id
ON project_files(workspace_id);

CREATE INDEX idx_project_files_project_id
ON project_files(project_id);

CREATE INDEX idx_project_files_language
ON project_files(language);

CREATE INDEX idx_project_files_indexed
ON project_files(indexed);

CREATE INDEX idx_agent_sessions_workspace_id
ON agent_sessions(workspace_id);

CREATE INDEX idx_agent_sessions_project_id
ON agent_sessions(project_id);

CREATE INDEX idx_agent_sessions_user_id
ON agent_sessions(user_id);

CREATE INDEX idx_agent_sessions_status
ON agent_sessions(status);

CREATE INDEX idx_agent_sessions_created_at
ON agent_sessions(created_at);

CREATE INDEX idx_agent_tool_calls_session_id
ON agent_tool_calls(session_id);

CREATE INDEX idx_agent_tool_calls_project_id
ON agent_tool_calls(project_id);

CREATE INDEX idx_agent_tool_calls_tool_name
ON agent_tool_calls(tool_name);

CREATE INDEX idx_analysis_affected_files_session_id
ON analysis_affected_files(session_id);

CREATE INDEX idx_analysis_affected_files_project_id
ON analysis_affected_files(project_id);

CREATE INDEX idx_analysis_risks_session_id
ON analysis_risks(session_id);

CREATE INDEX idx_analysis_risks_project_id
ON analysis_risks(project_id);

CREATE INDEX idx_analysis_task_drafts_session_id
ON analysis_task_drafts(session_id);

CREATE INDEX idx_analysis_task_drafts_project_id
ON analysis_task_drafts(project_id);

CREATE INDEX idx_analysis_task_drafts_status
ON analysis_task_drafts(status);
