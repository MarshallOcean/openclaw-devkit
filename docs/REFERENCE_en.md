# OpenClaw DevKit Technical Whitepaper & Reference Manual

This manual is the definitive technical documentation for the OpenClaw DevKit. It serves as both a zero-friction Quick Start guide for **beginners** and a deep-dive architectural whitepaper for **senior developers and architects**.

---

## 📖 Core Blueprint

### 🟢 Beginner Tier
- [1. Fast Mode: 3-Minute Deployment](#1-fast-mode-fully-automated-deployment) - Recommended for first-time users.
- [2. Interactive Onboarding](#2-interactive-onboarding-config-guide) - Guided setup for LLM and API keys.
- [3. Common Operation Commands](#3-common-operation-commands) - Essential `up`, `down`, and `logs` commands.

### 🔵 Power User Tier
- [4. Version Selection Guide](#4-one-click-version-switching) - Standard vs. Java vs. Office.
- [5. Data Persistence & Mounting](#5-deep-dive-data-mounting--persistence) - Understanding state separation.
- [6. Roles & Symlink Management](#6-roles--dev-flow-optimization) - Balancing privacy and convenience.

### 🔴 Architect Tier
- [7. Layered Orchestration Architecture](#7-architecture-layered-orchestration) - The logic behind `docker-compose.build.yml`.
- [8. Initialization Trace](#8-initialization-deep-trace) - Permission fixes and seed populating.
- [9. Security Sandbox & Network Binding](#9-security-whitepaper-sandbox--network-binding) - Critical security guidelines for production.
- [Appendix: Orchestration Logic Flow (ORCHESTRATION.md)](ORCHESTRATION.md) - Deep dive into the installation lifecycle.

---

## 🟢 Beginner Tier: Seamless Start

### 1. Fast Mode (Fully Automated Deployment) ⭐
Fast Mode utilizes pre-built images from GitHub Packages. No source code environment or local compilation is required.

```bash
# Clone and enter the directory
git clone https://github.com/hrygo/openclaw-devkit.git && cd openclaw-devkit

# One-click intelligent install (Auto env check, .env generation, image pull)
make install
```

### 2. Interactive Onboarding (Config Guide)
After startup, you need to configure the "soul" of OpenClaw (LLM keys, Feishu/Slack tokens, etc.).
```bash
make onboard
```
> **Beginner Tip**: Simply follow the terminal prompts. Configurations are automatically saved in `~/.openclaw`.

### 3. Common Operation Commands
| Command | Scenario | Description |
| :--- | :--- | :--- |
| `make up` | Start daily work | Start services in background |
| `make down` | End usage | Stop all services |
| `make logs` | Troubleshooting | View real-time Gateway logs |
| `make status` | Check health | View container status and access URLs |

---

## 🔵 Power User Tier: Productivity Scaling

### 4. One-click Version Switching
DevKit supports three specialized image environments, switchable via `make install [version]`:

| Version | Environment | Best For |
| :--- | :--- | :--- |
| **Standard** | Node, Go, Python | Default full-stack development |
| **Office** | OCR, Pandoc, LaTeX | Document processing & office automation |
| **Java** | JDK 25, Gradle, Maven | Enterprise Java development & debugging |

### 5. Deep Dive: Data Mounting & Persistence
DevKit follows the **"Config-State-Workspace"** triple separation principle:
1. **Seed Config** (`~/.openclaw`): Stores `openclaw.json`.
2. **State Volume** (`openclaw-state`): Stores sessions, credentials, and high-frequency data (persists even if images are deleted).
3. **Workspace** (`~/.openclaw/workspace`): Your project workbench, bi-directionally synced with the host.

### 6. Roles & Dev Flow Optimization
It is recommended to use **Symbolic Links (Symlinks)** to manage Agent role configurations. This keeps your Git history clean while maintaining local privacy.
```bash
# Example: Creating a symlink
ln -s /path/to/your/private/roles roles
```

---

## 🔴 Architect Tier: Deep Architectural Insights

### 7. Architecture: Layered Orchestration
DevKit employs an industry-leading **Layered Orchestration** model:
- **`docker-compose.yml`**: Static Layer. Defines core networking, ports, and base images.
- **`docker-compose.build.yml`**: Build Layer. Activated only when included in the `COMPOSE_FILE` variable, used for injecting complex build parameters (Apt mirrors, proxies, build arguments).
- **`Makefile` Driven**: Automatically senses `OPENCLAW_SKIP_BUILD` from `.env`.

### 8. Initialization: Deep Trace
When running `make install`, the system triggers the following critical logic chain:
1. **Idempotent Setup**: Verifies the `~/.openclaw` tree and creates all missing directories.
2. **Permission Guard**: Uses Docker layered permission fixing to ensure host-mounted directories are 100% readable/writable by the container's `node` user.
3. **Identity Lock**: Standardizes `gateway.bind = "lan"` in `docker-entrypoint.sh` to ensure zero-friction bridge networking.

**Visual Architecture Reference**: For every decision-making step during `make install`, please refer to [ORCHESTRATION.md](ORCHESTRATION.md).

### 9. Security Whitepaper: Sandbox & Network Binding
> [!IMPORTANT]
> **Containers MUST bind to `0.0.0.0 (lan)`.**
> Because Docker port forwarding originates from the bridge gateway. If the container binds to `127.0.0.1`, it will be unable to receive traffic forwarded from the host due to loopback isolation. DevKit's automated scripts ensure this is handled securely.

---

## ❓ Troubleshooting (QA / FAQ)

<details>
<summary><b>Q: No internet connectivity inside container (e.g., cannot access Claude API)?</b></summary>
A: Likely a proxy configuration issue. Ensure your host proxy has "Allow LAN" enabled. Then set <code>HTTP_PROXY=http://host.docker.internal:7897</code> in <code>.env</code>. Verify with <code>make test-proxy</code>.
</details>

<details>
<summary><b>Q: Code changes are not taking effect?</b></summary>
A: If you are in Dev Mode, run <code>make rebuild</code>. This performs a secondary build to apply your latest code increments.
</details>

---

## ⚙️ Full Technical Parameter Matrix

This table summarizes all environment variables supported by the DevKit for deep optimization.

| Category | Variable | Default | Description & Recommended Settings |
| :--- | :--- | :--- | :--- |
| **Orchestration** | `COMPOSE_FILE` | `docker-compose.yml` | Defines layers. Add `:docker-compose.build.yml` for local building. |
| | `OPENCLAW_SKIP_BUILD`| `true` | Switches: `true` (Fast Mode/Pull), `false` (Dev Mode/Build). |
| | `OPENCLAW_IMAGE` | `...:latest` | Full Tag of the target Docker image. |
| **Path Audit** | `OPENCLAW_CONFIG_DIR`| `~/.openclaw` | Path for `openclaw.json` and identity data. |
| | `OPENCLAW_WORKSPACE_DIR`| `.../workspace` | Primary workbench. Syncs with host. |
| **Network & Security**| `OPENCLAW_GATEWAY_PORT`| `18789` | External port for the Web UI. |
| | `OPENCLAW_GATEWAY_BIND`| `lan` | Must be `lan` for bridge network compatibility. |
| | `OPENCLAW_GATEWAY_TOKEN`| (Random) | Access token, auto-injected during installation. |
| | `HTTP[S]_PROXY` | - | Outbound proxy. Use `http://host.docker.internal:PORT`. |
| **Acceleration** | `DOCKER_MIRROR` | `docker.io` | Docker Hub acceleration (build-time). |
| | `APT_MIRROR` | `ustc` | Debian package mirror for faster builds. |
| | `NPM_MIRROR` | - | pnpm mirror for dependency resolution. |
| | `PYTHON_MIRROR` | - | pip mirror for Python package installation. |
| **Extensions** | `OPENCLAW_HOME_VOLUME`| - | (Optional) Named volume for persisting the entire `/home/node`. |
| | `OPENCLAW_EXTRA_MOUNTS`| - | (Advanced) Format: `src:dst[:ro]`. Mount extra resources. |

---

<p align="center">
  <b>OpenClaw Team | 2026 Technical Whitepaper</b>
</p>
