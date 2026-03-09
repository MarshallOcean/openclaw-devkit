# OpenClaw 开发工具箱套件

为 [OpenClaw](https://github.com/openclaw/openclaw) 多通道 AI 生产力工具提供完整的容器化开发环境。集成开发、调试、测试于一体的工具链，助力快速迭代和部署。

## 目录结构

```
.
├── .env                    # 环境变量配置
├── .openclaw_src/          # OpenClaw 源码（包含 .git）
├── Dockerfile.dev          # 开发环境镜像定义
├── docker-compose.dev.yml  # Docker Compose 配置
├── docker-dev-setup.sh     # 初始化脚本
├── Makefile                # 运维命令工具
└── README.md               # 本文档
```

## 快速开始

```bash
# 查看帮助
make help

# 首次安装 / 初始化
make install

# 启动服务
make up

# 查看状态
make status

# 查看日志
make logs

# 进入容器
make shell
```

## 更新源码

```bash
# 从 GitHub Release 更新到最新版本
make update

# 然后重建镜像
make rebuild
```

更新脚本会自动：
1. 检查当前版本
2. 获取 GitHub 最新 Release 版本
3. 下载并解压源码
4. 替换 `.openclaw_src/` 目录

## 工具版本

| 组件 | 版本 |
|------|------|
| Go | 1.25.8 |
| gh CLI | 2.87.3 |
| Node.js | 22 |
| pnpm | latest |
| Bun | latest |

## 常用命令

| 命令 | 说明 |
|------|------|
| `make up` | 启动服务 |
| `make down` | 停止服务 |
| `make restart` | 重启服务 |
| `make logs` | 查看日志 |
| `make shell` | 进入容器 |
| `make rebuild` | 重建镜像 |
| `make backup-config` | 备份配置 |
| `make test-proxy` | 测试代理连接 |

## 访问地址

- **Web UI**: http://127.0.0.1:18789/
- **Gateway**: ws://127.0.0.1:18789/
