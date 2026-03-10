#!/bin/bash
# ============================================================
# Playwright CLI + Skills 测试脚本
# ============================================================

set -e

IMAGE_NAME="openclaw-playwright-test"
CONTAINER_NAME="openclaw-playwright-test"

echo "============================================"
echo "  Playwright CLI + Skills 测试"
echo "============================================"

# 清理旧容器
echo "[1/4] 清理旧容器..."
docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

# 构建测试镜像
echo "[2/4] 构建测试镜像 (无缓存)..."
docker build --no-cache -f Dockerfile.test-playwright -t ${IMAGE_NAME} .

# 运行验证
echo "[3/4] 运行验证..."
docker run --rm --name ${CONTAINER_NAME} ${IMAGE_NAME} bash -c '
    echo ""
    echo "=== 1. 验证 playwright-cli 命令 ==="
    which playwright-cli
    playwright-cli --version

    echo ""
    echo "=== 2. 验证 --help 输出 ==="
    playwright-cli --help | head -20

    echo ""
    echo "=== 3. 验证 Skill 文件存在 ==="
    test -f /root/.claude/skills/playwright-cli/SKILL.md && echo "SKILL.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/request-mocking.md && echo "request-mocking.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/running-code.md && echo "running-code.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/session-management.md && echo "session-management.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/storage-state.md && echo "storage-state.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/test-generation.md && echo "test-generation.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/tracing.md && echo "tracing.md: OK"
    test -f /root/.claude/skills/playwright-cli/references/video-recording.md && echo "video-recording.md: OK"

    echo ""
    echo "=== 4. 验证 Skill 文件内容 ==="
    head -10 /root/.claude/skills/playwright-cli/SKILL.md

    echo ""
    echo "============================================"
    echo "  所有验证通过!"
    echo "============================================"
'

echo "[4/4] 测试完成!"
