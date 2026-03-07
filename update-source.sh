#!/usr/bin/env bash
# OpenClaw 源码更新脚本
# 从 GitHub Releases 下载最新版本源码

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${SCRIPT_DIR}/.openclaw_src"
REPO="openclaw/openclaw"

# 临时文件跟踪 (用于 trap 清理)
TMP_FILE=""
TMP_DIR=""

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

info() { echo -e "${GREEN}==>${NC} $*" >&2; }
warn() { echo -e "${YELLOW}WARNING:${NC} $*" >&2; }
error() { echo -e "${RED}ERROR:${NC} $*" >&2; exit 1; }

# 清理临时文件
cleanup() {
  [[ -n "$TMP_FILE" && -f "$TMP_FILE" ]] && rm -f "$TMP_FILE"
  [[ -n "$TMP_DIR" && -d "$TMP_DIR" ]] && rm -rf "$TMP_DIR"
}
trap cleanup EXIT INT TERM

# 获取最新 release 版本
get_latest_version() {
  local version
  version=$(curl -fsSL "https://api.github.com/repos/${REPO}/releases/latest" | jq -r '.tag_name')
  if [[ -z "$version" ]]; then
    error "无法获取最新版本信息"
  fi
  echo "$version"
}

# 下载源码
download_source() {
  local version="$1"
  local url="https://github.com/${REPO}/archive/refs/tags/${version}.tar.gz"
  local tmp_file="/tmp/openclaw-${version}.tar.gz"

  info "下载 OpenClaw ${version} 源码..."
  if ! curl -fsSL "$url" -o "$tmp_file"; then
    error "下载失败: $url"
  fi

  echo "$tmp_file"
}

# 停止运行中的容器
stop_containers() {
  if docker ps --filter "name=openclaw" --format "{{.Names}}" 2>/dev/null | grep -q .; then
    info "停止运行中的容器..."
    docker compose -f "${SCRIPT_DIR}/docker-compose.dev.yml" down 2>/dev/null || true
  fi
}

# 清理旧源码
clean_old_source() {
  if [[ -d "${SRC_DIR}" ]]; then
    info "清理旧版本源码..."
    rm -rf "${SRC_DIR}"
  fi
}

# 解压源码
extract_source() {
  local tar_file="$1"
  local version="$2"
  local tmp_dir="/tmp/openclaw-extract-$$"

  info "解压源码..."
  rm -rf "$tmp_dir"
  mkdir -p "$tmp_dir"
  tar -xzf "$tar_file" -C "$tmp_dir"

  # 找到解压后的目录名 (通常是 openclaw-版本号)
  local extracted_dir
  extracted_dir=$(ls -1 "$tmp_dir" | head -1)

  info "安装新版本源码..."
  mv "${tmp_dir}/${extracted_dir}" "${SRC_DIR}"

  # 清理临时文件
  rm -f "$tar_file"
  rm -rf "$tmp_dir"
}

# 主流程
main() {
  local current_version=""
  local latest_version=""

  # 检查当前版本
  if [[ -f "${SRC_DIR}/package.json" ]]; then
    current_version=$(jq -r '.version' "${SRC_DIR}/package.json" 2>/dev/null || echo "unknown")
    current_version="v${current_version}"
    info "当前版本: ${current_version}"
  else
    warn "源码目录不存在"
  fi

  # 获取最新版本
  info "检查最新版本..."
  latest_version=$(get_latest_version)
  info "最新版本: ${latest_version}"

  # 检查是否需要更新
  if [[ "$current_version" == "$latest_version" ]]; then
    info "已是最新版本，无需更新"
    exit 0
  fi

  # 下载新版本
  local tar_file
  tar_file=$(download_source "$latest_version")

  # 停止容器
  stop_containers

  # 清理旧版本
  clean_old_source

  # 解压新版本
  extract_source "$tar_file" "$latest_version"

  info "✓ 源码已更新到 ${latest_version}"
  info "运行 'make rebuild' 重建镜像"
}

main "$@"
