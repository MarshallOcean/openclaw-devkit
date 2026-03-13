# Docker 构建架构与流程

采用 **分层运行时 (Hierarchical Runtime)** 架构，将静态 SDK 环境与动态应用分离，实现极致构建速度。

---

## 1. 分层架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ Layer III: Product Layer - 构建频率: 高                                        │
│ ghcr.io/hrygo/openclaw-devkit:latest | :go | :java | :office              │
│ 包含: OpenClaw 官方 Release (openclaw.ai)                                   │
└───────────────────┬─────────────────────────────────┬─────────────────────┘
                    │ FROM                             │ FROM
┌───────────────────┴─────────────────────────────────┴─────────────────────┐
│ Layer II: Stack Runtimes - 构建频率: 低                                      │
│ ghcr.io/hrygo/openclaw-runtime:go | :java | :office                       │
│ 包含: Go 1.26, JDK 21, LibreOffice, Python IDP                             │
└───────────────────────────────────┬───────────────────────────────────────┘
                                    │ FROM
┌───────────────────────────────────┴───────────────────────────────────────┐
│ Layer I: Base Foundation - 构建频率: 极低                                   │
│ ghcr.io/hrygo/openclaw-runtime:base                                        │
│ 包含: Debian Bookworm, Node.js 22, Bun, uv, Playwright                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 本地构建

```bash
# 构建标准版
make build

# 构建指定版本
make build-go
make build-java
make build-office
```

**执行流程**:
```
make build-go
       │
       ▼
检查是否存在 openclaw-runtime:go
       │
       ▼
docker build -f Dockerfile
  --build-arg BASE_IMAGE=openclaw-runtime:go
  -t ghcr.io/hrygo/openclaw-devkit:go .
       │
       ▼
FROM openclaw-runtime:go (已包含 Go SDK)
RUN npm install -g openclaw
```

---

## 3. CI/CD 构建

由 `.github/workflows/docker-publish.yml` 驱动：

```
[prepare] ─────────────────────────────┐
      │                                │
      ▼                                ▼
[build-base]                          │ 感知版本
      │                                │
      ▼                                │
[build-stacks] ───────────────┐        │
      │                       │        │
      ▼                       ▼        ▼
[build-products] <───────────┴────────┘
      │
      ▼
推送至 GHCR:
  ghcr.io/hrygo/openclaw-runtime:base
  ghcr.io/hrygo/openclaw-runtime:{go,java,office}
  ghcr.io/hrygo/openclaw-devkit:{latest,go,java,office}
  ghcr.io/hrygo/openclaw-devkit:v1.6.2
```

---

## 4. 构建参数

| 参数               | 默认值           | 说明            |
| :----------------- | :--------------- | :-------------- |
| `HTTP_PROXY`       | -                | 网络代理        |
| `APT_MIRROR`       | `deb.debian.org` | Debian 镜像     |
| `OPENCLAW_VERSION` | `latest`         | OpenClaw 版本   |
| `INSTALL_BROWSER`  | `1`              | 安装 Playwright |

---

## 5. 运维变量

| 变量                     | 默认值                          | 说明         |
| :----------------------- | :------------------------------ | :----------- |
| `OPENCLAW_CONFIG_DIR`    | `~/.openclaw`                   | 配置目录     |
| `OPENCLAW_WORKSPACE_DIR` | `~/.openclaw/workspace`         | 工作区       |
| `OPENCLAW_GATEWAY_PORT`  | `18789`                         | Gateway 端口 |
| `OPENCLAW_IMAGE`         | `ghcr.io/hrygo/openclaw-devkit` | 镜像名       |

---

## 6. 镜像更新策略 (Image Update Strategy)

为了平衡**安装速度**与**版本时效性**，DevKit 采用以下优先级：

### 6.1 优先级逻辑
1. **本地优先 (`make install`)**：
   - 脚本首先检查本地是否存在对应标签的镜像。
   - 若存在，直接启动，**不主动联机检查**版本差异。
2. **强制拉取 (`make rebuild`)**：
   - 调用 `docker pull`。Docker 引擎会检查本地与远程 Registry 的 Image Digest。
   - 若远程有更新，自动下载并替换，随后重启容器。

### 6.2 常用方案
| 场景                | 命令                  | 行为                               |
| :------------------ | :-------------------- | :--------------------------------- |
| **首次安装**        | `make install`        | 拉取镜像并初始化环境               |
| **日常启动**        | `make up`             | 快速启动，无网络开销               |
| **跟进新特性/修复** | `make rebuild`        | **检测更新**、拉取并重启           |
| **手动维护**        | `docker pull <image>` | 仅手动更新镜像，不影响运行中的容器 |

---

## 7. 排查

### 权限问题
```bash
# 修复宿主机目录权限
sudo chown -R 1000:1000 ~/.openclaw
```

### 清理
```bash
make clean            # 容器和悬空镜像
make clean-volumes   # 所有数据卷（慎用）
```

---

## 8. UX 优化 (DevKit Cockpit)

为了提升 DevKit 的开箱即用体验，v1.6.2+ 引入了 Cockpit 运维引擎：

### 8.1 一键直达 (Dashboard)
- **命令**：`make dashboard`
- **逻辑**：自动获取容器内 Gateway Token 并生成带身份的 URL。
- **优势**：绕过初次访问的 `pairing required` 拦截，一键直达仪表盘。

### 8.2 自动化配对 (Approve)
- **命令**：`make approve`
- **逻辑**：自动识别 Web UI 发出的最新 `pending` 请求 ID 并批准。
- **场景**：如果您已打开网页正处于“待配对”状态，运行此命令可立即放行。

---

## 9. Windows / WSL 性能适配

由于 Windows 文件系统挂载性能较慢，我们针对性调整了 Docker 健康检查：
- **宽限期 (`start_period`)**：延长至 60s，给宿主机 IO 留足初始化缓冲。
- **重试 (`retries`)**：增加至 10次。
- **自愈**：`openclaw-init` 已合入主容器入口，启动时自动执行 `doctor --fix`。

---

## 10. 架构优势

- **DRY**: 构建逻辑收拢在 Makefile
- **缓存**: 更新版本时 Layer I/II 来自本地缓存
- **独立**: 各层可独立测试和发布
