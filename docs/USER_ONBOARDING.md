# OpenClaw DevKit 用户安装手册

本文档描述了从克隆仓库到完成配置、开启 Web UI 的完整执行流程。DevKit 旨在提供“零摩擦”的容器化开发体验。

---

## 🚀 极速起步 (1-2-3 流程)

只需三条指令，即可从零开始进入 Web 仪表盘：

```bash
# 1. 安装环境并启动主服务
make install

# 2. 交互式配置 (初次使用必做：设置 LLM、飞书等)
make onboard

# 3. 直通仪表盘 (免配对进入)
make dashboard
```

---

## 📦 安装执行细节

执行 `make install` 时，系统会自动完成以下任务：

1.  **环境适配**：检测 Docker 及 Compose 环境。
2.  **配置就绪**：基于 `.env.example` 自动生成本地 `.env`。
3.  **权限修复**：自动校准宿主机配置目录 (`~/.openclaw`) 的访问权限。
4.  **服务拉取**：从 GitHub 注册表同步最新的预构建镜像。
5.  **自愈启动**：镜像启动时自动执行 `openclaw doctor --fix` 修复任何潜在的配置不兼容。

---

## 🛠️ 运维与进阶

### 1. 生命周期管理
| 命令 | 场景 |
| :--- | :--- |
| `make up` | 日常快速启动 |
| `make down` | 停止并移除容器 |
| `make restart` | 重启所有服务 |
| `make status` | 检查各组件健康状态 |

### 2. Cockpit 运维工具 (强推)
为了绕过繁琐的配对过程，DevKit 提供了以下高阶指令：
- **`make dashboard`**: 自动获取容器 Token 并生成**免配对专用链接**，直接在默认浏览器打开。
- **`make approve`**: 如果您已开网页但处于等待配对状态，运行此命令将**自动批准**最新的请求。
- **`make devices`**: 列出所有已授权和待处理的设备请求。

### 3. 构建与更新
- **`make rebuild [flavor]`**: 检查远程更新、重新拉取镜像并重启。适用于跟进最新特性。
- **`make build [flavor]`**: 在本地基于分层架构重新构建镜像（开发者模式）。

---

## 🏗️ 架构概览

### 运行组件
- **`openclaw-gateway`**: 核心主服务，负责所有 Bot 的编排与通讯。
- **`openclaw-cli`**: 任务容器，负责执行 `onboard` 等交互式命令。

### 数据持久化
- **配置文件**: `~/.openclaw/openclaw.json` (宿主机挂载)
- **工作区**: `~/.openclaw/workspace` (代码产出目录)
- **系统状态**: Docker 卷 `openclaw-state` (持久化会话与元数据)

---

## ❓ 常见问题

### Q: 启动后无法访问 Web UI？
**解决**: 首先运行 `make logs` 查看 Gateway 输出。若是权限问题，系统会在启动时尝试自愈；若是网络绑定问题，DevKit 已强制将 `gateway.bind` 设置为 `all`。

### Q: `make install` 是否会覆盖我的配置？
**不会**。它采用幂等逻辑，仅在配置不存在时生成默认值，并修复已损坏或不兼容的 JSON 节点。

### Q: 如何切换版本 (Go/Java/Office)？
直接指定版本标签运行安装：
```bash
make install go    # 切换至 Go 增强版
make install office # 切换至 Office 旗舰版
```
安装后，系统会记住您的选型，后续只需 `make up` 即可。
