# OpenClaw DevKit 技术白皮书与参考手册

本手册是 OpenClaw DevKit 的权威技术文档。它既是面向**小白用户**的零门槛快速入门指南，也是面向**资深开发者与架构师**的底层逻辑白皮书。

---

## 📖 核心蓝图

### 🟢 基础篇
- [1. 极速模式：3 分钟部署](#1-极速模式-全自动快车道) - 零基础首选。
- [2. 交互式 onboard](#2-交互式-onboard-配置引导) - 手把手教你配置 LLM。
- [3. 常用运维指令](#3-常用运维指令) - 掌握 `up`, `down`, `logs` 三板斧。

### 🔵 进阶篇
- [4. 版本选择指南](#4-版本一键切换) - Standard vs. Java vs. Office。
- [5. 数据持久化与挂载](#5-深度解析数据挂载与持久化) - 理解状态分离逻辑。
- [6. Roles 与软链接管理](#6-roles-与开发流优化) - 隐私与便捷的平衡。

### 🔴 架构篇
- [7. 分层编排架构](#7-底层逻辑分层编排架构) - 揭秘 `docker-compose.build.yml` 逻辑。
- [8. 环境初始化深度溯源](#8-环境初始化深度溯源) - 权限修复与种子填充机制。
- [9. 安全沙盒与网络绑定](#9-安全白皮书沙盒与网络绑定) - 生产环境安全红线。
- [附录：编排逻辑流转图 (ORCHESTRATION.md)](ORCHESTRATION.md) - 深度内窥安装全流程。

---

## 🟢 基础篇：无障碍起步

### 1. 极速模式 (全自动快车道) ⭐
极速模式利用 GitHub Packages 的预构建镜像，无需源码环境，无需本地编译。

```bash
# 克隆并进入目录
git clone https://github.com/hrygo/openclaw-devkit.git && cd openclaw-devkit

# 一键智能安装 (自动完成环境检查、.env 生成、镜像拉取)
make install
```

### 2. 交互式 onboard 配置引导
启动后，您需要为 OpenClaw 配置“灵魂”（LLM 密钥、飞书/Slack 令牌等）。
```bash
make onboard
```
> **小白提示**：按照终端提示进行交互式输入即可，配置会自动保存在 `~/.openclaw` 中。

### 3. 常用运维指令
| 指令 | 场景 | 说明 |
| :--- | :--- | :--- |
| `make up` | 每天开始工作 | 启动服务进入后台 |
| `make down` | 结束使用 | 停止所有服务 |
| `make logs` | 出错时排查 | 查看网关实时运行日志 |
| `make status` | 检查状态 | 查看容器是否在运行及访问地址 |

---

## 🔵 进阶篇：生产力伸缩

### 4. 版本一键切换
DevKit 支持三种侧重点不同的镜像环境，通过 `make install [version]` 直接切换：

| 版本 | 特色环境 | 适用场景 |
| :--- | :--- | :--- |
| **Standard** | Node, Go, Python | 默认全栈开发环境 |
| **Office** | OCR, Pandoc, LaTeX | 深度文档处理与办公自动化 |
| **Java** | JDK 25, Gradle, Maven | 企业级 Java 开发与调试 |

### 5. 深度解析：数据挂载与持久化
DevKit 遵循 **“配置-状态-工作区”** 三路分离原则：
1. **种子配置** (`~/.openclaw`): 存储 `openclaw.json`。
2. **状态卷** (`openclaw-state`): 存储会话、凭证等高频读写数据（即便删除镜像，数据依然存在）。
3. **工作区** (`~/.openclaw/workspace`): 您的代码操作台，与宿主机双向同步。

### 6. Roles 与开发流优化
建议使用**软链接**模式管理 Agent 角色配置，兼顾 Git 提交的清晰度与本地隐私。
```bash
# 建立软链接示例
ln -s /path/to/your/private/roles roles
```

---

## 🔴 架构篇：底层逻辑深度内窥

### 7. 底层逻辑：分层编排架构
DevKit 采用业界领先的 **Layered Orchestration (分层编排)** 模型：
- **`docker-compose.yml`**: 静态层。定义网络架构、外部端口和基础镜像。
- **`docker-compose.build.yml`**: 构建层。仅在 `COMPOSE_FILE` 变量中包含时被激活，用于注入复杂的构建参数（Apt 镜像、代理、加速镜像等）。
- **`Makefile` 驱动**: 自动感应 `.env` 中的 `OPENCLAW_SKIP_BUILD`。

### 8. 环境初始化深度溯源
在执行 `make install` 时，系统触发了以下关键逻辑链路：
1. **Idempotent Setup**: 检查 `~/.openclaw` 树，补全所有必需目录。
2. **Permission Guard**: 利用 Docker 分层权限修复技术，确保宿主机挂载目录对容器内的 `node` 用户 100% 可读写。
3. **Identity Lock**: 在 `docker-entrypoint.sh` 中锁定 `gateway.bind = "lan"`，确保容器网络转发无阻。

**可视化架构参考**：关于 `make install` 的每一步决策过程，请参阅 [ORCHESTRATION.md](ORCHESTRATION.md)。

### 9. 安全白皮书：沙盒与网络绑定
> [!IMPORTANT]
> **容器必须绑定 `0.0.0.0 (lan)`。**
> 因为 Docker 的端口转发是从桥接网关发出的请求。如果容器内绑定 `127.0.0.1`，由于回环地址隔离，它将无法接收来自宿主机的转发流量。DevKit 已经通过自动化脚本确保了这一点的安全性。

---

## ❓ 故障排查 (QA / FAQ)

<details>
<summary><b>Q: 容器内网络连不通 (如：无法访问 Claude API)？</b></summary>
A: 大概率是代理配置问题。首先确保宿主机代理开启了“允许局域网” (Allow LAN)。然后在 <code>.env</code> 中设置 <code>HTTP_PROXY=http://host.docker.internal:7897</code>。使用 <code>make test-proxy</code> 验证。
</details>

<details>
<summary><b>Q: 修改了代码但没生效？</b></summary>
A: 如果您处于开发模式，请运行 <code>make rebuild</code>。它会执行二次构建并应用最新代码增量。
</details>

---

## ⚙️ 全量技术参数矩阵

本表汇总了 DevKit 中支持的所有环境变量，供资深用户进行深度调优。

| 变量分类 | 变量名 | 默认值 | 详细说明与推荐配置 |
| :--- | :--- | :--- | :--- |
| **编排核心** | `COMPOSE_FILE` | `docker-compose.yml` | 定义编排分层。启用本地构建需加上 `:docker-compose.build.yml` |
| | `OPENCLAW_SKIP_BUILD`| `true` | 开关：`true` (极速模式拉镜像), `false` (开发模式本地构建) |
| | `OPENCLAW_IMAGE` | `...:latest` | 指定运行时的 Docker 镜像 Full Tag |
| **路径审计** | `OPENCLAW_CONFIG_DIR`| `~/.openclaw` | 宿主机配置根目录，包含 `openclaw.json` 与 `identity` |
| | `OPENCLAW_WORKSPACE_DIR`| `.../workspace` | 智能体操作的主战场，建议定期备份 |
| **网络隔离** | `OPENCLAW_GATEWAY_PORT`| `18789` | 外部访问网关监听端口 |
| | `OPENCLAW_GATEWAY_BIND`| `lan` | 必须保持 `lan` 以支持 Docker Bridge 转发 |
| | `OPENCLAW_GATEWAY_TOKEN`| (随机) | Gateway 鉴权令牌，安装时自动注入 |
| | `HTTP[S]_PROXY` | - | 容器外网出口。推荐使用 `http://host.docker.internal:端口` |
| **加速镜像** | `DOCKER_MIRROR` | `docker.io` | Docker Hub 加速，构建时生效 |
| | `APT_MIRROR` | `ustc` | Debian 包加速源，显著提升本地构建速度 |
| | `NPM_MIRROR` | - | 支持 pnpm 构建时的加速，推荐淘宝源 |
| | `PYTHON_MIRROR` | - | 支持 pip 安装依赖时的加速，推荐清华源 |
| **平台扩展** | `OPENCLAW_HOME_VOLUME`| - | (可选) 若设为命名卷名，则整个 `/home/node` 持久化 |
| | `OPENCLAW_EXTRA_MOUNTS`| - | (高级) 格式: `src:dst[:ro]`。支持动态挂载额外资源 |

---

<p align="center">
  <b>OpenClaw Team | 2026 技术规格书</b>
</p>
