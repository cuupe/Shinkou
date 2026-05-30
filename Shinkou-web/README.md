# Shinkou 登录与激活页面

使用 Vue + TypeScript + Vite + Tailwind CSS + TanStack Query 搭建。Monaco Editor 已作为依赖保留，便于后续项目文件/代码编辑页面接入。

## 启动

```bash
npm install
npm run dev
```

默认接口代理到 `http://localhost:8080`。

## 页面

- `/login`：企业邮箱 + 密码登录
- `/activate?token=inv_demo_admin_token`：邀请激活账号

## 接口

- `POST /api/auth/login`
- `GET /api/invitations/{token}`
- `POST /api/auth/activate`

图标与插画位置均以空白占位容器保留。


## Tailwind 版本说明

本项目使用 Tailwind CSS v3 配置方式。若本地之前安装过 Tailwind v4 导致 PostCSS 报错，请删除 `node_modules` 和 `package-lock.json` 后重新执行：

```bash
npm install
npm run dev
```
