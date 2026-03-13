# OpenClaw Feishu Bot Configuration - Docker Edition

**7-Step Quick Setup, Docker Environment Exclusive**

---

## Prerequisites

Before starting configuration, ensure:

- ✅ Docker and Docker Compose are installed
- ✅ openclaw-devkit is installed via `make install`
- ✅ OpenClaw Gateway service is running (`make up`)
- ✅ Model configuration is complete (use `make onboard` for interactive setup)

---

## Quick Check

Run the following command in the project root directory to verify service status:

```bash
make status
```

Expected output:
```
【Container】
  openclaw-gateway: Up X hours
【Image】
  openclaw-devkit:dev (xxx MB)
【Access】 http://127.0.0.1:18789/
```

---

## Step 1: Create Feishu App (3 Minutes)

### 1.1 Login to Feishu Open Platform

1. Open browser and visit: https://open.feishu.cn
2. Login with your Feishu account

### 1.2 Create App

1. Click **"Create App"** button in top right corner
2. Select **"Self-built App"**
3. Fill in app information:
   - App Name: `OpenClaw Assistant`
   - App Description: `AI Personal Assistant`
4. Click **"Create"**

### 1.3 Record App Credentials

Go to app details page, click **"Credentials & Basic Info"** on the left sidebar, and record:

| Item | Description | Example |
|------|-------------|---------|
| **App ID** | Application identifier | `cli_xxxxxxxxxxxxxxxx` |
| **App Secret** | Application secret | Click "View" to copy |

⚠️ **Important:** App Secret is displayed only once. Save it securely!

---

## Step 2: Modify OpenClaw Configuration (2 Minutes)

### 2.1 Understand Container Volume Mapping

openclaw-devkit uses Docker Volume mapping. The configuration directory inside the container is mounted to the host:

| Container Path | Host Path |
|---------------|-----------|
| `~/.openclaw/` | `~/.openclaw/` |

This means you can edit the configuration file directly on the host without entering the container!

### 2.2 Backup Existing Configuration

```bash
# Execute on host terminal
cp ~/.openclaw/openclaw.json ~/.openclaw/openclaw.json.backup
```

### 2.3 Edit Configuration File

**Mac / Linux Users:**
```bash
open ~/.openclaw/openclaw.json    # Mac
nano ~/.openclaw/openclaw.json   # Linux
```

**Windows Users:**
```
Open with Notepad or VS Code:
%USERPROFILE%\.openclaw\openclaw.json
```

### 2.4 Add Feishu Configuration

Find or add the `channels` section in the configuration file:

```json
{
  // ... other configurations ...
  "channels": {
    "feishu": {
      "enabled": true,
      "defaultAccount": "main",
      "accounts": {
        "main": {
          "appId": "cli_xxxxxxxxxxxxxx",
          "appSecret": "xxxxxxxxxxxxxxxxxxxxxxxx",
          "botName": "OpenClaw Assistant"
        }
      }
    }
  }
  // ... other configurations ...
}
```

**⚠️ Important Notes:**
- Replace `appId` and `appSecret` with actual values from Step 1
- Ensure JSON format is correct (use double quotes, watch for commas)
- If `channels` already exists, just add or update the `feishu` section

### 2.5 Verify Configuration

```bash
# Execute in project directory
make cli CMD="config list"
```

If configuration is correct, you should see Feishu-related configuration information.

---

## Step 3: Add Bot Capability (2 Minutes)

### 3.1 Configure on Feishu Open Platform

1. Return to Feishu Open Platform app page
2. Find **"Add App Capability"**
3. Select **"Bot"** → **"Add"**
4. Fill in bot information:
   - Bot Name: `OpenClaw Assistant`
   - Bot Description: `AI Assistant powered by OpenClaw`
5. Click **"Save"**

### 3.2 Publish Version

1. Find **"Version Management & Publishing"**
2. Click **"Create Version"**
3. Fill in version information:
   - Version: `1.0.0`
   - Changelog: `Initial Version - Connect to OpenClaw`
4. Click **"Save"** → **"Publish"** → **"Confirm Publish"**

---

## Step 4: Configure App Permissions (2 Minutes)

### 4.1 Add Required Permissions

On Feishu Open Platform:

1. Find **"Permission Management"** or **"Permission Configuration"**
2. Click **"Add Permission"**
3. Search and check the following permissions:

| Permission Name | Description |
|-----------------|-------------|
| `im:message` | Send/Receive messages |
| `im:message.group_at_msg` | Group chat @mentions |
| `im:chat` | Chat management |
| `drive:drive` | Cloud drive access |
| `drive:file:readonly` | File read-only |
| `drive:file` | File read/write |
| `wiki:wiki` | Knowledge base |
| `contact:user.base:readonly` | User basic information |

4. Click **"Confirm Grant Permissions"**

---

## Step 5: Configure Events & Callback (2 Minutes)

### 5.1 Enable Long Connection

⚠️ **Critical Step:** This is the core configuration for Feishu bot to receive messages!

1. Find **"Events & Callback"** on Feishu Open Platform
2. In the **"Long Connection Configuration"** section:
   - Click **"Enable Long Connection"**
   - Ensure status shows **"Enabled"**
3. Click **"Save"**

### 5.2 Add Event Subscription

In the same page's **"Event Subscription"** section:

1. Click **"Add Event"**
2. Add the following events:

| Event Name | Description |
|------------|-------------|
| `im.message.receive_v1` | Receive message (group chat) |
| `im.message.receive_v1` | Receive message (private chat) |
| `im.chat.member.add_v1` | Chat member added |
| `im.chat.member.delete_v1` | Chat member removed |

3. Click **"Save"**

### 5.3 Publish New Version

⚠️ **Important:** After configuring long connection and events, you must publish a new version for it to take effect!

1. Find **"Version Management & Publishing"**
2. Click **"Create Version"**
3. Fill in version information:
   - Version: `1.1.0`
   - Changelog: `Enable long connection and event subscription`
4. Click **"Save"** → **"Publish"** → **"Confirm Publish"**

---

## Step 6: Add App to Feishu (1 Minute)

### 6.1 Add to Workspace

1. Click **"Add to Workspace"**
2. Select app visibility:
   - Select your department
   - Or select **"All Company"**
3. Click **"Add"**

### 6.2 Open App in Feishu

1. Open Feishu client
2. Go to **"Workspace"**
3. Find **"OpenClaw Assistant"**
4. Click to enter chat interface

---

## Step 7: Test Connection (1 Minute)

### 7.1 Restart OpenClaw Service

Execute in project root directory:

```bash
make restart
```

Wait a few seconds, the service will automatically restart and load the new configuration.

### 7.2 Verify Service Status

```bash
make status
```

Ensure the `openclaw-gateway` container status is `Up`.

### 7.3 Test in Feishu

1. Send a message in Feishu app: `Hello`
2. Wait for OpenClaw's reply

If you receive a reply, congratulations! Configuration successful! 🎉

---

## ✅ Complete

Now you can use OpenClaw in Feishu!

Try these commands:
- `help` - View help information
- `status` - View service status
- `time` - View current time

---

## 📋 Docker Command Reference

| Operation | Command |
|-----------|---------|
| Start service | `make up` |
| Stop service | `make down` |
| Restart service | `make restart` |
| View status | `make status` |
| View logs | `make logs` |
| Enter container | `make shell` |
| Execute OpenClaw command | `make cli CMD="..."` |

---

## 🆘 Troubleshooting

### Issue 1: No response when sending messages in Feishu

**Troubleshooting Steps:**

```bash
# 1. Check service status
make status

# 2. View real-time logs
make logs

# 3. Restart service
make restart
```

### Issue 2: Configuration file format error

**Solution:**

```bash
# Restore backup
cp ~/.openclaw/openclaw.json.backup ~/.openclaw/openclaw.json

# Restart service
make restart
```

Then use a JSON validation tool (like https://jsonlint.com) to check the format.

### Issue 3: View detailed error information

```bash
# Enter container
make shell

# View OpenClaw logs
tail -50 /tmp/openclaw-gateway.log

# Exit container
exit
```

### Issue 4: Container cannot start

```bash
# View container detailed status
docker ps -a

# View container logs
docker compose logs openclaw-gateway

# Complete restart
make down && make up
```

---

## 📝 Configuration Parameters

| Parameter | Description | Example Value |
|-----------|-------------|---------------|
| `enabled` | Enable Feishu functionality | `true` / `false` |
| `defaultAccount` | Default account identifier | `"main"` |
| `appId` | Feishu application ID | `cli_xxxxxxxxxxxxxx` |
| `appSecret` | Feishu application secret | `xxxxxxxxxxxxxxxx` |
| `botName` | Bot display name | `"OpenClaw Assistant"` |

---

## 🔄 Configuration Update Workflow

When you need to update Feishu configuration, follow these steps:

1. **Edit configuration file**: Modify `~/.openclaw/openclaw.json`
2. **Verify configuration**: `make cli CMD="config list"`
3. **Restart service**: `make restart`
4. **Test connection**: Send a test message in Feishu

---

## 📚 Additional Resources

- **openclaw-devkit Documentation**: [Project README](../README.md)
- **OpenClaw Official Docs**: https://github.com/openclaw/openclaw
- **Feishu Open Platform**: https://open.feishu.cn

---

**Document Version:** 1.0.0
**Last Updated:** 2026-03-13
**Environment:** openclaw-devkit Docker container deployment
