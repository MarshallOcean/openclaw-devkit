# OpenClaw DevKit Docker 核心工作流

本文档面向开发者和运维，深入说明 OpenClaw DevKit 的 Docker 编排架构、版本策略及高级运维逻辑。

---

## 1. 架构拓扑 (Orchestration)

DevKit 采用**“网关中心化 (Gateway-Centered)”**架构，主要由以下服务组成：

- **`openclaw-gateway`**: 核心逻辑容器，承载协议网关、Bot 编排引擎。
- **`openclaw-cli`**: 任务容器，作为 `onboard`、`cli` 等交互式后台指令的运行环境。
- **持久化卷**: 
    - `~/.openclaw`: 宿主机挂载点，存储 JSON 配置、工作区产物及加密密钥。
    - `openclaw-state`: Docker 内部卷，存储高频变动的数据库会话。

---

## 2. Windows / WSL 专项适配 (Performance Tuning)

针对 Windows 宿主机文件系统挂载产生的 I/O 延迟，DevKit 内置了**性能宽限策略**：
- **健康检查延迟**：`start_period` 设置为 60s，确保在缓慢的机械硬盘或 WSL2 挂载点上也有充足的自愈时间。
- **长效重试**：健康检查具备 10 次重试机制，防止波动导致的服务意外重启。
- **非 root 隔离**：全系镜像强制以 `node` 用户运行，同时启动脚本会自动纠正宿主机卷的 `chown` 权限，确保 Windows/Mac 跨平台文件读写一致性。

---

## 3. 镜像版本与更新策略

### 3.1 环境预设 (Flavors)
| 镜像标签 | 核心工具集成 |
| :--- | :--- |
| `latest` | Node.js, Claude Code, Playwright, Python |
| `go` | 基础版 + Golang 1.26 完整工具链 |
| `java` | 基础版 + OpenJDK 21, Gradle, Maven |
| `office` | 基础版 + LibreOffice, pandoc, 文档加工工具 |

### 3.2 更新机制 (The Rebuild Logic)
为了平衡安装速度与版本实时性，系统遵循以下逻辑：
- **`make install` (本地优先)**：如果本地存在镜像，则直接启动。这是为了保证在网络不稳时也能极速进入开发。
- **`make rebuild` (云端拉取)**：当需要同步 GitHub Registry 的最新特性或修复时，执行此命令。它会检查 Digest 并强制拉取最新的 Layer。

---

## 4. Cockpit 运维引擎 (Automation)

为了提升开发者体验，DevKit 内置了 Cockpit 自动化工具组：

### 4.1 认证绕避 (Automation Bypass)
- **`make dashboard`**: 通过容器内 `openclaw dashboard --no-open` 获取动态授权。
- **原理**：直接读取 Gateway 的认证令牌并拼接为 URL，解决 `pairing required` 拦截导致的首次进入难问题。

### 4.2 自动配对 (Approval Automation)
- **`make approve`**: 使用 `jq` 自动解析待处理配对列表。
- **场景**：无需查阅 ID 即可批准最新的 Web UI 接入请求。

---

## 5. 配置自愈与外科手术 (Configuration Surgery)

镜像入口脚本 `docker-entrypoint.sh` 具备**外科手术级**的配置修复能力：
- **深度净化**：启动时自动剔除旧版本中不再受支持、可能导致 Schema 校验失败的过期配置节点。
- **网络强固**：强制设置 `gateway.bind = "all"`，确保 Docker 网络环境下宿主机能稳定访问 18789 端口。
- **Origin 合规**：自动注入 `allowedOrigins` 白名单，确保携带 Token 的请求通过 CSRF 校验。

---

## 6. 排障常用命令

```bash
# 1. 检查 Gateway 实时流水日志
make logs

# 2. 检查配置文件的 JSON 有效性
make verify

# 3. 强制重排所有网络与容器
make restart

# 4. 彻底清理数据卷（环境重置）
make clean-volumes
```
