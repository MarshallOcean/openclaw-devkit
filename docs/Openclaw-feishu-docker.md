# OpenClaw 配置飞书机器人 - Docker 容器版

**7 步快速配置，Docker 环境专属**

---

## 前置准备

在开始配置之前，请确保：

- ✅ 已安装 Docker 和 Docker Compose
- ✅ 已通过 `make install` 安装 openclaw-devkit
- ✅ OpenClaw Gateway 服务正在运行（`make up`）
- ✅ 已完成模型配置（可通过 `make onboard` 交互式配置）

---

## 快速检查

在项目根目录运行以下命令，确认服务状态：

```bash
make status
```

预期输出：
```
【容器】
  openclaw-gateway: Up X hours
【镜像】
  openclaw-devkit:dev (xxx MB)
【访问】 http://127.0.0.1:18789/
```

---

## 第一步：创建飞书应用（3 分钟）

### 1.1 登录飞书开放平台

1. 打开浏览器，访问：https://open.feishu.cn
2. 使用你的飞书账号登录

### 1.2 创建应用

1. 点击右上角 **"创建应用"** 按钮
2. 选择 **"自建应用"**
3. 填写应用信息：
   - 应用名称：`OpenClaw助手`
   - 应用描述：`AI 个人助理`
4. 点击 **"创建"**

### 1.3 记录应用凭证

进入应用详情页，点击左侧 **"凭证与基础信息"**，记录以下信息：

| 项目 | 说明 | 示例 |
|------|------|------|
| **App ID** | 应用标识符 | `cli_xxxxxxxxxxxxxxxx` |
| **App Secret** | 应用密钥 | 点击"查看"后复制 |

⚠️ **重要提示：** App Secret 仅显示一次，请务必妥善保存！

---

## 第二步：修改 OpenClaw 配置（2 分钟）

### 2.1 理解容器配置映射

openclaw-devkit 使用 Docker Volume 映射，容器内的配置目录挂载到宿主机：

| 容器内路径 | 宿主机路径 |
|-----------|-----------|
| `~/.openclaw/` | `~/.openclaw/` |

这意味着你可以直接在宿主机编辑配置文件，无需进入容器！

### 2.2 备份现有配置

```bash
# 在宿主机终端执行
cp ~/.openclaw/openclaw.json ~/.openclaw/openclaw.json.backup
```

### 2.3 编辑配置文件

**Mac / Linux 用户：**
```bash
open ~/.openclaw/openclaw.json    # Mac
nano ~/.openclaw/openclaw.json   # Linux
```

**Windows 用户：**
```
使用记事本或 VS Code 打开：
%USERPROFILE%\.openclaw\openclaw.json
```

### 2.4 添加飞书配置

在配置文件中找到或添加 `channels` 部分：

```json
{
  // ... 其他配置 ...
  "channels": {
    "feishu": {
      "enabled": true,
      "defaultAccount": "main",
      "accounts": {
        "main": {
          "appId": "cli_xxxxxxxxxxxxxx",
          "appSecret": "xxxxxxxxxxxxxxxxxxxxxxxx",
          "botName": "OpenClaw助手"
        }
      }
    }
  }
  // ... 其他配置 ...
}
```

**⚠️ 注意事项：**
- 将 `appId` 和 `appSecret` 替换为第一步获取的实际值
- 确保 JSON 格式正确（使用双引号，注意逗号）
- 如果 `channels` 已存在，只需添加或更新 `feishu` 部分

### 2.5 验证配置格式

```bash
# 在项目目录执行
make cli CMD="config list"
```

如果配置正确，应该能看到飞书相关配置信息。

---

## 第三步：添加机器人能力（2 分钟）

### 3.1 在飞书开放平台配置

1. 回到飞书开放平台应用页面
2. 找到 **"添加应用能力"**
3. 选择 **"机器人"** → **"添加"**
4. 填写机器人信息：
   - 机器人名称：`OpenClaw助手`
   - 机器人描述：`由 OpenClaw 驱动的 AI 助手`
5. 点击 **"保存"**

### 3.2 发布版本

1. 找到 **"版本管理与发布"**
2. 点击 **"创建版本"**
3. 填写版本信息：
   - 版本号：`1.0.0`
   - 更新日志：`初始版本 - 连接 OpenClaw`
4. 点击 **"保存"** → **"发布"** → **"确认发布"**

---

## 第四步：配置应用权限（2 分钟）

### 4.1 添加必需权限

在飞书开放平台：

1. 找到 **"权限管理"** 或 **"权限配置"**
2. 点击 **"添加权限"**
3. 搜索并勾选以下权限：

| 权限名称 | 说明 |
|---------|------|
| `im:message` | 发送/接收消息 |
| `im:message.group_at_msg` | 群聊 @消息 |
| `im:chat` | 聊天管理 |
| `drive:drive` | 云盘访问 |
| `drive:file:readonly` | 文件只读 |
| `drive:file` | 文件读写 |
| `wiki:wiki` | 知识库 |
| `contact:user.base:readonly` | 用户基本信息 |

4. 点击 **"确认开通权限"**

---

## 第五步：配置事件与回调（2 分钟）

### 5.1 开启长连接

⚠️ **关键步骤：** 这是飞书机器人能够接收消息的核心配置！

1. 在飞书开放平台找到 **"事件与回调"**
2. 在 **"长连接配置"** 区域：
   - 点击 **"开启长连接"**
   - 确保状态显示为 **"已开启"**
3. 点击 **"保存"**

### 5.2 添加事件订阅

在同一页面的 **"事件订阅"** 区域：

1. 点击 **"添加事件"**
2. 添加以下事件：

| 事件名称 | 说明 |
|---------|------|
| `im.message.receive_v1` | 接收消息（群聊） |
| `im.message.receive_v1` | 接收消息（私聊） |
| `im.chat.member.add_v1` | 聊天成员添加 |
| `im.chat.member.delete_v1` | 聊天成员移除 |

3. 点击 **"保存"**

### 5.3 发布新版本

⚠️ **重要：** 配置长连接和事件后，必须发布新版本才能生效！

1. 找到 **"版本管理与发布"**
2. 点击 **"创建版本"**
3. 填写版本信息：
   - 版本号：`1.1.0`
   - 更新日志：`启用长连接和事件订阅`
4. 点击 **"保存"** → **"发布"** → **"确认发布"**

---

## 第六步：添加应用到飞书（1 分钟）

### 6.1 添加到工作台

1. 点击 **"添加到工作台"**
2. 选择应用可见范围：
   - 选择你的部门
   - 或选择 **"全公司"**
3. 点击 **"添加"**

### 6.2 在飞书中打开应用

1. 打开飞书客户端
2. 进入 **"工作台"**
3. 找到 **"OpenClaw助手"**
4. 点击进入聊天界面

---

## 第七步：测试连接（1 分钟）

### 7.1 重启 OpenClaw 服务

在项目根目录执行：

```bash
make restart
```

等待几秒钟，服务将自动重启并加载新配置。

### 7.2 验证服务状态

```bash
make status
```

确保 `openclaw-gateway` 容器状态为 `Up`。

### 7.3 在飞书中测试

1. 在飞书应用中发送消息：`你好`
2. 等待 OpenClaw 的回复

如果收到回复，恭喜你！配置成功！🎉

---

## ✅ 完成

现在你可以在飞书中使用 OpenClaw 了！

试试这些命令：
- `help` - 查看帮助信息
- `status` - 查看服务状态
- `time` - 查看当前时间

---

## 📋 常用 Docker 命令参考

| 操作 | 命令 |
|------|------|
| 启动服务 | `make up` |
| 停止服务 | `make down` |
| 重启服务 | `make restart` |
| 查看状态 | `make status` |
| 查看日志 | `make logs` |
| 进入容器 | `make shell` |
| 执行 OpenClaw 命令 | `make cli CMD="..."` |

---

## 🆘 故障排除

### 问题 1：飞书发送消息无响应

**排查步骤：**

```bash
# 1. 检查服务状态
make status

# 2. 查看实时日志
make logs

# 3. 重启服务
make restart
```

### 问题 2：配置文件格式错误

**解决方法：**

```bash
# 恢复备份
cp ~/.openclaw/openclaw.json.backup ~/.openclaw/openclaw.json

# 重启服务
make restart
```

然后使用 JSON 验证工具（如 https://jsonlint.com）检查格式。

### 问题 3：查看详细错误信息

```bash
# 进入容器
make shell

# 查看 OpenClaw 日志
tail -50 /tmp/openclaw-gateway.log

# 退出容器
exit
```

### 问题 4：容器无法启动

```bash
# 查看容器详细状态
docker ps -a

# 查看容器日志
docker compose logs openclaw-gateway

# 完全重启
make down && make up
```

---

## 📝 配置参数说明

| 参数 | 说明 | 示例值 |
|------|------|--------|
| `enabled` | 是否启用飞书功能 | `true` / `false` |
| `defaultAccount` | 默认账号标识 | `"main"` |
| `appId` | 飞书应用 ID | `cli_xxxxxxxxxxxxxx` |
| `appSecret` | 飞书应用密钥 | `xxxxxxxxxxxxxxxx` |
| `botName` | 机器人显示名称 | `"OpenClaw助手"` |

---

## 🔄 配置更新流程

当需要更新飞书配置时，按照以下步骤操作：

1. **编辑配置文件**：修改 `~/.openclaw/openclaw.json`
2. **验证配置**：`make cli CMD="config list"`
3. **重启服务**：`make restart`
4. **测试连接**：在飞书发送测试消息

---

**文档版本：** 1.0.0
**最后更新：** 2026-03-13
**适用环境：** openclaw-devkit Docker 容器部署
