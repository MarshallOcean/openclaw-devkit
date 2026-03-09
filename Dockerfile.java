# OpenClaw 开发环境定制镜像 (Java 增强版)
# 基于 Dockerfile.dev，增加 Java/JVM 技术栈开发工具
# 集成多语言开发栈: TypeScript (主) + Go + Python + Java
#
# 构建命令:
#   docker build -t openclaw:dev-java -f Dockerfile.java .
#
# 运行命令:
#   OPENCLAW_IMAGE=openclaw:dev-java ./docker-setup.sh

FROM node:22-bookworm@sha256:cd7bcd2e7a1e6f72052feb023c7f6b722205d3fcab7bbcbd2d1bfdab10b1e935

LABEL org.opencontainers.image.base.name="docker.io/library/node:22-bookworm" \
  org.opencontainers.image.base.digest="sha256:cd7bcd2e7a1e6f72052feb023c7f6b722205d3fcab7bbcbd2d1bfdab10b1e935" \
  org.opencontainers.image.source="https://github.com/openclaw/openclaw" \
  org.opencontainers.image.title="OpenClaw Dev (Java Enhanced)" \
  org.opencontainers.image.description="OpenClaw gateway with full development toolchain (TypeScript, Go, Python, Java)"

# ============================================================
# 第一阶段：安装系统依赖和开发工具链
# ============================================================

ENV DEBIAN_FRONTEND=noninteractive

# 开发工具 + Office 处理依赖 + 浏览器自动化依赖
ARG DEV_APT_PACKAGES="\
  # 基础工具
  curl wget jq git ripgrep \
  # 现代 CLI 工具
  fd-find bat \
  # HTTP 客户端
  httpie \
  # Python 开发
  python3 python3-pip python3-venv \
  # 构建工具
  build-essential pkg-config \
  # Office 文件处理 (pandoc 依赖)
  pandoc texlive-latex-base texlive-fonts-recommended \
  # 浏览器自动化依赖
  xvfb libnss3 libatk-bridge2.0-0 libdrm2 libxkbcommon0 libgbm1 \
  libasound2 libatspi2.0-0 libxshmfence1 libxcomposite1 libxdamage1 \
  libxfixes3 libxrandr2 libdbus-1-3 libgtk-3-0 \
  # 字体支持
  fonts-liberation fonts-noto-color-emoji \
  # 其他实用工具
  unzip file sqlite3 zip"

RUN apt-get update && \
  DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends $DEV_APT_PACKAGES && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# ============================================================
# Go 1.25 手动安装 (apt 版本过旧: 1.19.8)
# ============================================================
# https://go.dev/dl/
ARG GO_VERSION=1.25.8
RUN ARCH=$(dpkg --print-architecture) && \
  curl -fsSL "https://go.dev/dl/go${GO_VERSION}.linux-${ARCH}.tar.gz" | tar -C /usr/local -xz && \
  ln -sf /usr/local/go/bin/go /usr/local/bin/go && \
  ln -sf /usr/local/go/bin/gofmt /usr/local/bin/gofmt
ENV PATH="/usr/local/go/bin:${PATH}"
ENV GOPATH=/home/node/go
ENV PATH="${GOPATH}/bin:${PATH}"

# ============================================================
# GitHub CLI 最新版 (apt 版本过旧: 2.23.0)
# ============================================================
# https://github.com/cli/cli/releases
RUN ARCH=$(dpkg --print-architecture) && \
  GH_VERSION=$(curl -fsSL https://api.github.com/repos/cli/cli/releases/latest | jq -r '.tag_name' | sed 's/^v//') && \
  curl -fsSL "https://github.com/cli/cli/releases/download/v${GH_VERSION}/gh_${GH_VERSION}_linux_${ARCH}.deb" -o /tmp/gh.deb && \
  apt-get update && apt-get install -y /tmp/gh.deb && \
  rm /tmp/gh.deb && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# ============================================================
# 第二阶段：安装语言运行时和包管理器
# ============================================================

# Bun (TypeScript 运行时，比 Node 更快)
ENV BUN_INSTALL=/opt/bun
ENV PATH="${BUN_INSTALL}/bin:${PATH}"
RUN curl -fsSL https://bun.sh/install | bash && \
  ln -sf "${BUN_INSTALL}/bin/bun" /usr/local/bin/bun

# pnpm (Node 包管理器)
RUN corepack enable && corepack prepare pnpm@latest --activate

# Playwright CLI (浏览器自动化命令行工具)
RUN npm install -g @playwright/test@latest

# Claude Code CLI (Anthropic 官方 CLI 工具)
RUN npm install -g @anthropic-ai/claude-code@latest

# Python 包 (Office 处理 + 知识库工具)
ARG PYTHON_PACKAGES="\
  python-pptx \
  openpyxl \
  python-docx \
  beautifulsoup4 \
  lxml \
  pyyaml \
  pandoc"
RUN pip3 install --break-system-packages $PYTHON_PACKAGES

# Go 环境变量
ENV GOPATH=/home/node/go
ENV PATH="${GOPATH}/bin:/usr/local/go/bin:${PATH}"

# ============================================================
# Go 开发工具链 (golangci-lint, gopls, dlv, etc.)
# ============================================================
# golangci-lint: 综合性 linter (https://golangci-lint.run/)
ARG GOLANGCI_LINT_VERSION=1.64.8
RUN ARCH=$(dpkg --print-architecture) && \
  curl -fsSL "https://github.com/golangci/golangci-lint/releases/download/v${GOLANGCI_LINT_VERSION}/golangci-lint-${GOLANGCI_LINT_VERSION}-linux-${ARCH}.tar.gz" | \
  tar -xz -C /tmp && \
  mv /tmp/golangci-lint-${GOLANGCI_LINT_VERSION}-linux-${ARCH}/golangci-lint /usr/local/bin/ && \
  rm -rf /tmp/golangci-lint-*

# Go 工具安装 (需要 GOPATH 已设置)
ARG GO_TOOLS="\
  golang.org/x/tools/gopls@latest \
  github.com/go-delve/delve/cmd/dlv@latest \
  honnef.co/go/tools/cmd/staticcheck@latest \
  github.com/securego/gosec/v2/cmd/gosec@latest \
  golang.org/x/tools/cmd/goimports@latest \
  github.com/air-verse/air@latest \
  github.com/google/mock/mockgen@latest \
  github.com/google/wire/cmd/wire@latest \
  github.com/onsi/ginkgo/v2/ginkgo@latest"

RUN mkdir -p "${GOPATH}/bin" && \
  for tool in ${GO_TOOLS}; do \
  echo "Installing $tool..." && \
  go install "$tool"; \
  done && \
  # 清理 Go 模块缓存减小镜像体积
  go clean -modcache

# ============================================================
# Java 开发工具链 (JDK 25 LTS, Gradle, Maven, etc.)
# 2026 Java 开发最佳实践
# ============================================================

# SDKMAN 安装 (Java SDK 版本管理器)
# https://sdkman.io/
ENV SDKMAN_DIR=/home/node/.sdkman
RUN curl -fsSL "https://get.sdkman.io" | bash && \
  chown -R node:node /home/node/.sdkman

# JDK 25 LTS 安装 (2026 最新 LTS 版本)
# https://sdkman.io/jdks
ARG JDK_VERSION=25
ARG JDK_VENDOR=tempo
ENV JAVA_HOME=/home/node/.sdkman/candidates/java/current
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN bash -c "source ${SDKMAN_DIR}/bin/sdkman-init.sh && \
  sdk install java ${JDK_VERSION}-${JDK_VENDOR} && \
  sdk default java ${JDK_VERSION}-${JDK_VENDOR}"

# Gradle 安装 (推荐构建工具 - 增量构建更快)
ARG GRADLE_VERSION=8.14
RUN bash -c "source ${SDKMAN_DIR}/bin/sdkman-init.sh && \
  sdk install gradle ${GRADLE_VERSION} && \
  sdk default gradle ${GRADLE_VERSION}"

# Maven 安装 (传统项目兼容)
ARG MAVEN_VERSION=3.9.9
RUN bash -c "source ${SDKMAN_DIR}/bin/sdkman-init.sh && \
  sdk install maven ${MAVEN_VERSION} && \
  sdk default maven ${MAVEN_VERSION}"

# 创建 Java 工具目录
RUN mkdir -p /home/node/.local/bin /home/node/.local/lib && \
  chown -R node:node /home/node/.local

# Spring Boot CLI (https://spring.io/tools)
ARG SPRING_BOOT_VERSION=3.4.4
RUN curl -fsSL "https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/${SPRING_BOOT_VERSION}/spring-boot-cli-${SPRING_BOOT_VERSION}-bin.tar.gz" | \
  tar -xz -C /home/node/.local && \
  ln -sf /home/node/.local/spring-${SPRING_BOOT_VERSION}/bin/spring /home/node/.local/bin/spring && \
  chown -R node:node /home/node/.local/spring-${SPRING_BOOT_VERSION}
ENV PATH="/home/node/.local/spring-${SPRING_BOOT_VERSION}/bin:${PATH}"

# google-java-format (代码格式化 CLI)
# https://github.com/google/google-java-format
ARG GOOGLE_JAVA_FORMAT_VERSION=1.27.0
RUN curl -fsSL "https://github.com/google/google-java-format/releases/download/v${GOOGLE_JAVA_FORMAT_VERSION}/google-java-format-${GOOGLE_JAVA_FORMAT_VERSION}-all-deps.jar" \
  -o /home/node/.local/lib/google-java-format.jar && \
  echo '#!/bin/bash\njava -jar /home/node/.local/lib/google-java-format.jar "$@"' > /home/node/.local/bin/google-java-format && \
  chmod +x /home/node/.local/bin/google-java-format && \
  chown node:node /home/node/.local/lib/google-java-format.jar

# Checkstyle (代码风格检查 CLI)
# https://checkstyle.sourceforge.io/
ARG CHECKSTYLE_VERSION=10.23.1
RUN curl -fsSL "https://github.com/checkstyle/checkstyle/releases/download/checkstyle-${CHECKSTYLE_VERSION}/checkstyle-${CHECKSTYLE_VERSION}-all.jar" \
  -o /home/node/.local/lib/checkstyle.jar && \
  echo '#!/bin/bash\njava -jar /home/node/.local/lib/checkstyle.jar "$@"' > /home/node/.local/bin/checkstyle && \
  chmod +x /home/node/.local/bin/checkstyle && \
  chown node:node /home/node/.local/lib/checkstyle.jar

# PMD (代码质量分析 CLI)
# https://pmd.github.io/
ARG PMD_VERSION=7.12.0
RUN curl -fsSL "https://github.com/pmd/pmd/releases/download/pmd_releases%2F${PMD_VERSION}/pmd-dist-${PMD_VERSION}-bin.zip" \
  -o /tmp/pmd.zip && \
  unzip /tmp/pmd.zip -d /home/node/.local && \
  rm /tmp/pmd.zip && \
  ln -sf /home/node/.local/pmd-bin-${PMD_VERSION}/bin/pmd /home/node/.local/bin/pmd && \
  chown -R node:node /home/node/.local/pmd-bin-${PMD_VERSION}
ENV PATH="/home/node/.local/pmd-bin-${PMD_VERSION}/bin:${PATH}"

# SpotBugs (Bug 检测 CLI)
# https://spotbugs.github.io/
ARG SPOTBUGS_VERSION=4.9.3
RUN curl -fsSL "https://github.com/spotbugs/spotbugs/releases/download/${SPOTBUGS_VERSION}/spotbugs-${SPOTBUGS_VERSION}.tgz" | \
  tar -xz -C /home/node/.local && \
  ln -sf /home/node/.local/spotbugs-${SPOTBUGS_VERSION}/bin/spotbugs /home/node/.local/bin/spotbugs && \
  chown -R node:node /home/node/.local/spotbugs-${SPOTBUGS_VERSION}
ENV PATH="/home/node/.local/spotbugs-${SPOTBUGS_VERSION}/bin:${PATH}"

# 更新 PATH
ENV PATH="/home/node/.local/bin:/home/node/.sdkman/candidates/java/current/bin:/home/node/.sdkman/candidates/gradle/current/bin:/home/node/.sdkman/candidates/maven/current/bin:${PATH}"

# ============================================================
# 第三阶段：安装 OpenClaw 核心组件
# ============================================================

WORKDIR /app
RUN chown node:node /app

# 复制依赖声明文件（利用 Docker 层缓存）
COPY --chown=node:node package.json pnpm-lock.yaml pnpm-workspace.yaml .npmrc ./
COPY --chown=node:node ui/package.json ./ui/package.json
COPY --chown=node:node patches ./patches
COPY --chown=node:node scripts ./scripts

USER node
RUN NODE_OPTIONS=--max-old-space-size=2048 pnpm install --frozen-lockfile

# 安装 Chromium for Playwright (可选，约 300MB)
ARG INSTALL_BROWSER=1
USER root
RUN if [ "${INSTALL_BROWSER}" = "1" ]; then \
  mkdir -p /home/node/.cache/ms-playwright && \
  PLAYWRIGHT_BROWSERS_PATH=/home/node/.cache/ms-playwright \
  node /app/node_modules/playwright-core/cli.js install --with-deps chromium && \
  chown -R node:node /home/node/.cache/ms-playwright; \
  fi
USER node

# 复制源代码并构建
COPY --chown=node:node . .
RUN for dir in /app/extensions /app/.agent /app/.agents; do \
  if [ -d "$dir" ]; then \
  find "$dir" -type d -exec chmod 755 {} +; \
  find "$dir" -type f -exec chmod 644 {} +; \
  fi; \
  done
RUN pnpm build
ENV OPENCLAW_PREFER_PNPM=1
RUN pnpm ui:build

# 暴露 CLI
USER root
RUN ln -sf /app/openclaw.mjs /usr/local/bin/openclaw \
  && chmod 755 /app/openclaw.mjs

# ============================================================
# 第四阶段：生产环境配置
# ============================================================

ENV NODE_ENV=production
ENV PLAYWRIGHT_BROWSERS_PATH=/home/node/.cache/ms-playwright

USER node

# 健康检查
HEALTHCHECK --interval=3m --timeout=10s --start-period=15s --retries=3 \
  CMD node -e "fetch('http://127.0.0.1:18789/healthz').then((r)=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"

# 默认启动 Gateway
CMD ["node", "openclaw.mjs", "gateway", "--allow-unconfigured"]
