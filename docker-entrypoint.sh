#!/usr/bin/env bash
set -e

# OpenClaw Docker Entrypoint
# Handles auto-initialization for fresh environments

CONFIG_DIR="/home/node/.openclaw"
CONFIG_FILE="$CONFIG_DIR/openclaw.json"
SEED_DIR="/home/node/.openclaw-seed"

# 1. Fix Permissions (if running as root)
if [ "$(id -u)" = "0" ]; then
    chown -R node:node /home/node/.openclaw
fi

# 2. Check for missing configuration
if [ ! -f "$CONFIG_FILE" ]; then
    echo "==> Initializing fresh OpenClaw environment..."
    
    # Try to copy from seed if available
    if [ -d "$SEED_DIR" ] && [ "$(ls -A "$SEED_DIR" 2>/dev/null)" ]; then
        echo "--> Copying initial configuration from seed..."
        cp -rn "$SEED_DIR"/* "$CONFIG_DIR/" 2>/dev/null || true
    fi
    
    # If still missing, run official setup
    if [ ! -f "$CONFIG_FILE" ]; then
        echo "--> Running official OpenClaw setup (non-interactive)..."
        openclaw setup --non-interactive
    fi
fi

# 3. Ensure Gateway safety & access for Docker
if [ -f "$CONFIG_FILE" ]; then
    # Force localized settings for Docker environment
    openclaw config set gateway.mode local --strict-json >/dev/null 2>&1 || true
    openclaw config set gateway.bind lan --strict-json >/dev/null 2>&1 || true
    openclaw config set gateway.controlUi.allowedOrigins '["http://127.0.0.1:18789"]' --strict-json >/dev/null 2>&1 || true
fi

# 4. Execute CMD
echo "==> Starting OpenClaw..."
exec "$@"
