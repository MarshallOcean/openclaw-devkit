# OpenClaw Docker 构建架构与流程

本项目采用了 **分层运行时 (Hierarchical Runtime)** 架构，通过将静态 SDK 环境与动态应用安装分离，实现了极致的构建速度和缓存利用率。

## 1. 核心架构图 (分层逻辑)

```text
┌──────────────────────────────────────────────────────────────────────────┐
│ 层级 III: 产品发布层 (Product Layer) - 构建频率: 高                         │
│ 镜像: openclaw-devkit:latest, :go, :java, :office                   │
│ 内容: 执行 openclaw.ai/install.sh (官方 Release)                          │
└───────────────────┬───────────────────────────────────┬──────────────────┘
                    │ (FROM)                            │ (FROM)
┌───────────────────┴───────────────────────────────────┴──────────────────┐
│ 层级 II: 技术栈运行时 (Stack Runtimes) - 构建频率: 低                      │
│ 镜像: openclaw-runtime:go, :java, :office                                │
│ 内容: Go SDK 1.26.1, JDK 21, LibreOffice, Python IDP libs                │
└───────────────────────────────────┬──────────────────────────────────────┘
                                    │ (FROM)
┌───────────────────────────────────┴──────────────────────────────────────┐
│ 层级 I: 基础设施层 (Base Foundation) - 构建频率: 极低                      │
│ 镜像: openclaw-runtime:base                                              │
│ 内容: Debian Bookworm Slim, Node.js 22, Bun, uv, Playwright Deps         │
└──────────────────────────────────────────────────────────────────────────┘
```

## 2. 本地构建流程 (Local Build Flow)

开发者通过 `Makefile` 进行本地驱动：

```text
 用户命令:  make build go
           │
           ▼
 [Makefile] 自动逻辑选择:
 1. 检查是否存在 openclaw-runtime:go
 2. 执行: docker build -f Dockerfile 
          --build-arg BASE_IMAGE=openclaw-runtime:go 
          -t openclaw-go .
           │
           ▼
 [Dockerfile] 内部动作:
 1. 继承 runtime:go (包含所有 SDK，跳过下载)
 2. 运行 curl | bash (安装应用)
 3. 产出镜像: [openclaw-go:latest]
```

## 3. GitHub CI 构建流程 (CI/CD Pipeline)

由 `.github/workflows/docker-publish.yml` 驱动，利用 Job 依赖并行构建：

```text
[Job: prepare] ────────────────┐
      │                        │
      ▼                        ▼
[Job: build-base]              │ (感知版本)
      │                        │
      ▼                        │
[Job: build-stacks] ───────────┤ (并行构建 go/java/office)
      │                        │
      ▼                        │
[Job: build-products] <────────┘ (并行拉取对应的 stack 并安装应用)
      │
      ▼
[产物推送至 GHCR]:
  - ghcr.io/hrygo/openclaw-runtime:base             # 基础基础设施镜像
  - ghcr.io/hrygo/openclaw-runtime:go               # Go 技术栈运行时
  - ghcr.io/hrygo/openclaw-runtime:java             # Java 技术栈运行时
  - ghcr.io/hrygo/openclaw-runtime:office           # Office 自动化运行时
  - ghcr.io/hrygo/openclaw-devkit:latest            # 标准版开发工具 (Product)
  - ghcr.io/hrygo/openclaw-devkit:go                # Go 语言版开发工具
  - ghcr.io/hrygo/openclaw-devkit:java              # Java 语言版开发工具
  - ghcr.io/hrygo/openclaw-devkit:office            # Office 自动化版开发工具
  - ghcr.io/hrygo/openclaw-devkit:v* (带版本号的生产 Tag)
```

## 4. 高级配置 (Advanced Configuration)

### 4.1 构建参数 (Build Arguments)
在 `Makefile` 构建时，可以通过以下参数优化构建过程（或通过 `.env` 设置）：

| 参数 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `HTTP_PROXY` | - | 构建时的网络代理 |
| `APT_MIRROR` | `deb.debian.org` | Debian 软件源镜像 |
| `OPENCLAW_VERSION` | `latest` | 指定安装的 OpenClaw 版本 |
| `INSTALL_BROWSER` | `1` | 是否在产品镜像中安装 Playwright 浏览器 |

### 4.2 运维环境变量 (Runtime Envs)
`docker-setup.sh` 和 `docker-compose.yml` 使用以下变量：

| 变量 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `OPENCLAW_CONFIG_DIR` | `~/.openclaw` | 宿主机配置文件存放路径 |
| `OPENCLAW_WORKSPACE_DIR` | `~/.openclaw/workspace` | 宿主机工作区路径 |
| `OPENCLAW_GATEWAY_PORT` | `18789` | Gateway 访问端口 |
| `OPENCLAW_EXTRA_MOUNTS` | - | 额外挂载点 (src:dst[:ro]) |
| `OPENCLAW_IMAGE` | `openclaw-devkit` | 使用的镜像名称 |

## 5. 常见问题排查 (Troubleshooting)

### 5.1 权限问题 (Permission Denied)
如果在挂载目录时遇到权限错误，请尝试：
```bash
# 自动修复宿主机目录权限 (由于容器内 node 用户 UID 为 1000)
sudo chown -R 1000:1000 ~/.openclaw
```
`docker-setup.sh` 会尝试自动修复权限，但在某些系统（如特定 Linux 发行版）下可能需要手动干预。

### 5.2 缓存清理 (Cleanup)
构建失败或需要彻底重置时：
```bash
make clean          # 清理容器和悬空镜像
make clean-volumes  # 【慎用】清理所有持久化数据卷
```

## 6. 架构优势总结

1.  **DRY (Don't Repeat Yourself)**: 所有的 Docker 构建逻辑收拢在 `Makefile`，`docker-setup.sh` 只负责环境初始化。
2.  **SOLID (Single Responsibility)**: 
    - `Dockerfile.base`: 只管 OS 和通用工具。
    - `Dockerfile.stacks`: 只管 SDK 环境。
    - `Dockerfile`: 只管应用安装。
3.  **极速缓存**: 更新 OpenClaw 版本时，Layer I 和 Layer II 的数 GB 数据完全来源于本地缓存，无需重新下载或安装。
