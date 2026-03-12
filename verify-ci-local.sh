#!/bin/bash
set -e

# 本地验证 CI 流程 - 三层构建
# 模拟: prepare → build-base → build-stacks → build-products

REGISTRY="ghcr.io"
IMAGE_BASE="openclaw-runtime"

echo "=== Step 1: 构建 base 镜像 ==="
docker buildx build \
  --platform linux/amd64 \
  -f Dockerfile.base \
  -t ${IMAGE_BASE}:base \
  --load \
  .

echo ""
echo "=== Step 2: 构建 stacks 镜像 ==="
for stack in go java office; do
  echo "--- Building stack: $stack ---"
  docker buildx build \
    --platform linux/amd64 \
    -f Dockerfile.stacks \
    --target stack-${stack} \
    --build-arg BASE_IMAGE=${IMAGE_BASE}:base \
    -t ${IMAGE_BASE}:${stack} \
    --load \
    .
done

echo ""
echo "=== Step 3: 构建 product 镜像 ==="
# 基于 base 构建 latest
echo "--- Building product: latest (from base) ---"
docker buildx build \
  --platform linux/amd64 \
  -f Dockerfile \
  --target default \
  --build-arg BASE_IMAGE=${IMAGE_BASE}:base \
  -t openclaw-devkit:latest \
  --load \
  .

# 基于 stacks 构建 go/java/office
for stack in go java office; do
  echo "--- Building product: $stack (from $stack stack) ---"
  docker buildx build \
    --platform linux/amd64 \
    -f Dockerfile \
    --target default \
    --build-arg BASE_IMAGE=${IMAGE_BASE}:${stack} \
    -t openclaw-devkit:${stack} \
    --load \
    .
done

echo ""
echo "=== 构建完成！验证镜像 ==="
docker images | grep -E "(openclaw-runtime|openclaw-devkit)"
