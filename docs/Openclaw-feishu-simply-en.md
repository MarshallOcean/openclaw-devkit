# OpenClaw Feishu Bot Configuration - Quick Start

**7 Simple Steps, 12 Minutes to Complete**

---

## Prerequisites

Before configuring the Feishu bot, ensure:

- ✅ OpenClaw is installed
- ✅ OpenClaw is configured with a model
- ✅ OpenClaw Gateway is running

---

## Step 1: Create Feishu App (3 Minutes)

### 1.1 Login to Feishu Open Platform

1. Open your browser and visit: https://open.feishu.cn
2. Login with your Feishu account

### 1.2 Create App

1. Click the **"Create App"** button in the top right corner
2. Select **"Self-built App"**
3. Fill in app information:
   - App Name: `My OpenClaw Assistant`
   - App Description: `OpenClaw Personal Assistant`
4. Click **"Create"**

### 1.3 Record App Information

Click on your newly created app to enter the app details page. Navigate to **"Credentials & Basic Info"** on the left sidebar and record the following 2 items:

**App ID** (e.g., `cli_xxxxxxxxxxxxxxxx`)
**App Secret** (Click "View", e.g., `xxxxxxxxxxxxxxxxxxxxx`)

⚠️ **Important:** Save your App ID and App Secret securely!

---

## Step 2: Configure OpenClaw (2 Minutes)

### 2.1 Locate Configuration File

**Native Installation Users:**

**Mac Users:**
Open Terminal and run:
```bash
open ~/.openclaw/
```

**Windows Users:**
1. Press `Win + R` to open the Run dialog
2. Type: `%USERPROFILE%\.openclaw`
3. Press Enter

**Docker Container Deployment Users:**

In the openclaw-devkit project directory, run the following command to enter the container:
```bash
make shell
```

Then open the configuration directory inside the container:
```bash
cd ~/.openclaw/
ls -la
```

### 2.2 Backup Configuration File

**Native Installation Users:**
1. Find the `openclaw.json` file
2. Copy it and rename to `openclaw.json.backup`

**Docker Container Deployment Users:**
```bash
# Execute inside container
cp ~/.openclaw/openclaw.json ~/.openclaw/openclaw.json.backup
```

### 2.3 Edit Configuration File

Open `openclaw.json` with a text editor:

**Native Installation Users:**
- Mac: Use VS Code, TextEdit, or any text editor
- Windows: Use VS Code, Notepad++, or Notepad

**Docker Container Deployment Users:**
```bash
# Exit container (if already inside)
exit

# Edit configuration file on host machine (recommended)
# Container's ~/.openclaw/ directory is mounted to host's ~/.openclaw/
# So you can edit directly on the host

# Mac users
open ~/.openclaw/openclaw.json

# Linux users
nano ~/.openclaw/openclaw.json

# Windows users
# Open %USERPROFILE%\.openclaw\openclaw.json with Notepad or VS Code
```

### 2.4 Add Feishu Configuration

Find the `channels` section in the configuration file. If it doesn't exist, add the following content before the closing `}` at the end of the file:

```json
  "channels": {
    "feishu": {
      "enabled": true,
      "defaultAccount": "main",
      "accounts": {
        "main": {
          "appId": "cli_xxxxxxxxxxxxxx2",
          "appSecret": "vpxxxxxxxxxxxxxxebG1z",
          "botName": "My OpenClaw Assistant"
        }
      }
    }
  }
```

**Important:** Replace the `appId` and `appSecret` with the values you recorded in Step 1!

### 2.5 Save Configuration File

Save the file, ensuring UTF-8 encoding.

---

## Step 3: Add Bot (2 Minutes)

### 3.1 Add Bot Capability

1. On the Feishu Open Platform app page, find **"Add App Capability"**
2. Select **"Bot"** and click **"Add"**
3. Fill in bot information:
   - Bot Name: `OpenClaw Assistant` (or custom name)
   - Bot Description: `AI Personal Assistant`
4. Click **"Save"**

### 3.2 Publish Version

1. On the Feishu Open Platform app page, find **"Version Management & Publishing"**
2. Click **"Create Version"**
3. Fill in version information:
   - Version: `1.0.0`
   - Changelog: `Initial Version - Connect to OpenClaw`
4. Click **"Save"**
5. Click **"Publish"**
6. Click **"Confirm Publish"**

---

## Step 4: Configure App Permissions (2 Minutes)

### 4.1 Add Required Permissions

1. On the Feishu Open Platform app page, find **"Permission Management"** or **"Permission Configuration"**
2. Click **"Add Permission"**
3. Search and check the following permissions:

**Required Permissions List:**
- `im:message`
- `im:message.group_at_msg`
- `im:chat`
- `drive:drive`
- `drive:file:readonly`
- `drive:file`
- `wiki:wiki`
- `contact:user.base:readonly`

4. Click **"Confirm Grant Permissions"**

---

## Step 5: Configure Events & Callback (2 Minutes)

### 5.1 Configure Long Connection

⚠️ **Important:** This is the key configuration for the Feishu bot to receive messages!

1. On the Feishu Open Platform app page, find **"Events & Callback"**
2. In the **"Long Connection Configuration"** section:
   - Click **"Enable Long Connection"**
   - Ensure status shows **"Enabled"**
3. Click **"Save"**

### 5.2 Add Event Subscription

1. On the same page, in the **"Event Subscription"** section, click **"Add Event"**
2. Search and add the following events:

**Required Events List:**
- `im.message.receive_v1` - Receive message (group chat)
- `im.message.receive_v1` - Receive message (private chat)
- `im.chat.member.add_v1` - Chat member added
- `im.chat.member.delete_v1` - Chat member removed

3. Click **"Save"**

### 5.3 Verify Configuration

Ensure the following statuses are normal:
- ✅ Long Connection Status: **Enabled**
- ✅ Event Subscription: **Configured**

### 5.4 Publish New Version

⚠️ **Important:** After configuring long connection and events, you must publish a new version for it to take effect!

1. On the Feishu Open Platform app page, find **"Version Management & Publishing"**
2. Click **"Create Version"**
3. Fill in version information:
   - Version: `1.1.0` (or higher version number)
   - Changelog: `Enable long connection and event subscription`
4. Click **"Save"**
5. Click **"Publish"**
6. Click **"Confirm Publish"**

---

## Step 6: Add App to Feishu (1 Minute)

### 6.1 Add to Workspace

1. On the Feishu Open Platform app page, click **"Add to Workspace"**
2. Select app visibility:
   - Select your department
   - Or select **"All Company"**
3. Click **"Add"**

### 6.2 Open App in Feishu

1. Open the Feishu client
2. Go to **"Workspace"**
3. Find your created **"My OpenClaw Assistant"** app
4. Click to open the app and enter the chat interface

---

## Step 7: Test Connection (1 Minute)

### 7.1 Restart OpenClaw

**Native Installation Users:**

Open Terminal and run:
```bash
openclaw gateway restart
```

Wait a few seconds, OpenClaw will restart.

**Docker Container Deployment Users:**

In the openclaw-devkit project directory, run the following command to restart the service:
```bash
make restart
```

Or use the container command:
```bash
make cli CMD="gateway restart"
```

Wait a few seconds, OpenClaw will restart.

### 7.2 Test in Feishu

1. Open the Feishu app
2. Find your created OpenClaw app
3. Send a message: `Hello`
4. Wait for reply

If you receive a reply, congratulations! Configuration successful! 🎉

---

## ✅ Complete

Now you can use OpenClaw in Feishu!

Try these commands:
- `help` - View help
- `status` - View status
- `time` - View time

---

## 🆘 Troubleshooting

### Issue: No response when sending messages in Feishu

**Solution:**

**Native Installation Users:**

1. Check if OpenClaw is running:
   ```bash
   openclaw status
   ```

2. View error logs:
   ```bash
   openclaw logs --tail 50
   ```

3. Restart OpenClaw:
   ```bash
   openclaw gateway restart
   ```

**Docker Container Deployment Users:**

1. Check service status:
   ```bash
   make status
   ```

2. View Gateway logs:
   ```bash
   make logs
   ```

3. Restart service:
   ```bash
   make restart
   ```

### Issue: Configuration file format error

**Solution:**

1. Restore backup configuration:

   **Native Installation Users:**
   - Use `openclaw.json.backup`
   - Copy it back to `openclaw.json`

   **Docker Container Deployment Users:**
   ```bash
   # Execute on host machine
   cp ~/.openclaw/openclaw.json.backup ~/.openclaw/openclaw.json
   make restart
   ```

2. Check JSON format:
   - Visit: https://jsonlint.com
   - Paste your configuration content
   - Check for errors

3. Ensure using double quotes:
   - ✅ Correct: `"appId": "cli_123456"`
   - ❌ Wrong: `'appId': 'cli_123456'`

### Issue: App Secret error

**Solution:**

1. Check if App Secret is copied correctly:
   - Ensure no extra spaces
   - Ensure complete copy (not truncated)

2. If App Secret is forgotten:
   - Regenerate on Feishu Open Platform
   - Update configuration file
   - Restart OpenClaw

   **Docker Container Deployment Users:**
   ```bash
   # Restart after updating configuration
   make restart
   ```

### Issue: Cannot modify configuration file inside Docker container

**Solution:**

The `~/.openclaw/` directory inside the container is mounted to the host's `~/.openclaw/`. It's recommended to edit directly on the host:

1. **Mac Users:**
   ```bash
   open ~/.openclaw/openclaw.json
   ```

2. **Linux Users:**
   ```bash
   nano ~/.openclaw/openclaw.json
   ```

3. **Windows Users:**
   - Open `%USERPROFILE%\.openclaw\openclaw.json` with Notepad or VS Code

After editing, run `make restart` in the project directory to apply the configuration.

---

## 📝 Configuration Parameters

- `enabled`: Set to `true` to enable Feishu functionality
- `defaultAccount`: Default account name, typically `"main"`
- `accounts`: Account configuration list
  - `main`: Account identifier (customizable)
    - `appId`: App ID obtained from Feishu Open Platform
    - `appSecret`: App Secret obtained from Feishu Open Platform
    - `botName`: Bot name displayed in Feishu

---

**Document Version:** 1.4.0
**Last Updated:** 2026-03-13
**Update:** Added Docker container deployment commands and English translation
