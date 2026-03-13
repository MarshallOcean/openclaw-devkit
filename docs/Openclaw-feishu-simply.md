# OpenClaw 配置飞书机器人-快速版

**简单 7 步，12 分钟完成**

---

## 前置准备

在开始配置飞书机器人之前，请确保：

- ✅ OpenClaw 已安装
- ✅ OpenClaw 已配置模型
- ✅ OpenClaw Gateway 正在运行


---

## 第一步：创建飞书应用（3 分钟）

### 1.1 登录飞书开放平台

1. 打开浏览器，访问：https://open.feishu.cn
2. 使用你的飞书账号登录

### 1.2 创建应用

1. 点击右上角 **"创建应用"** 按钮
2. 选择 **"自建应用"**
3. 填写应用信息：
   - 应用名称：`我的OpenClaw助手`
   - 应用描述：`OpenClaw个人助理`
4. 点击 **"创建"**

### 1.3 记录应用信息

点击新创建的应用进入应用详情页面，点击左侧凭证与基础信息找到并记下这 2 个信息：

**App ID**（例如：`cli_xxxxxxxxxxxxxxxx`）
**App Secret**（点击"查看"，例如：`xxxxxxxxxxxxxxxxxxxxx`）

⚠️ **重要：** App ID 和 App Secret  务必保存好！

---

## 第二步：配置 OpenClaw（2 分钟）

### 2.1 找到配置文件

**本机部署用户：**

**Mac 用户：**
打开终端，运行：
```bash
open ~/.openclaw/
```

**Windows 用户：**
1. 按 `Win + R` 打开运行窗口
2. 输入：`%USERPROFILE%\.openclaw`
3. 按回车

**Docker 容器部署用户：**

在 openclaw-devkit 项目目录下，运行以下命令进入容器：
```bash
make shell
```

然后在容器内打开配置目录：
```bash
cd ~/.openclaw/
ls -la
```

### 2.2 备份配置文件

**本机部署用户：**
1. 找到 `openclaw.json` 文件
2. 复制一份，重命名为 `openclaw.json.backup`

**Docker 容器部署用户：**
```bash
# 进入容器后执行
cp ~/.openclaw/openclaw.json ~/.openclaw/openclaw.json.backup
```

### 2.3 编辑配置文件

使用文本编辑器打开 `openclaw.json`：

**本机部署用户：**
- Mac：用 VS Code、TextEdit 或其他文本编辑器
- Windows：用 VS Code、Notepad++ 或记事本

**Docker 容器部署用户：**
```bash
# 退出容器（如果已进入）
exit

# 在宿主机编辑配置文件（推荐）
# 容器内的 ~/.openclaw/ 目录挂载到宿主机的 ~/.openclaw/
# 所以可以直接在宿主机编辑

# Mac 用户
open ~/.openclaw/openclaw.json

# Linux 用户
nano ~/.openclaw/openclaw.json

# Windows 用户
# 使用记事本或 VS Code 打开 %USERPROFILE%\.openclaw\openclaw.json
```

### 2.4 添加飞书配置

在配置文件中找到 `channels` 部分。如果没有，就在文件末尾的 `}` 之前添加以下内容：

```json
  "channels": {
    "feishu": {
      "enabled": true,
      "defaultAccount": "main",
      "accounts": {
        "main": {
          "appId": "cli_xxxxxxxxxxxxxx2",
          "appSecret": "vpxxxxxxxxxxxxxxebG1z",
          "botName": "我的OpenClaw助手"
        }
      }
    }
  }
```

**重要：** 将上面的 `appId` 和 `appSecret` 替换为你在第一步记录的值！

### 2.5 保存配置文件

保存文件，确保使用 UTF-8 编码。

---

## 第三步：添加机器人（2 分钟）

### 3.1 添加机器人能力

1. 在飞书开放平台的应用页面，找到 **"添加应用能力"**
2. 选择 **"机器人"**，点击 **"添加"**
3. 填写机器人信息：
   - 机器人名称：`OpenClaw助手`（或自定义名称）
   - 机器人描述：`AI 个人助理`
4. 点击 **"保存"**

### 3.2 发布版本

1. 在飞书开放平台的应用页面，找到 **"版本管理与发布"**
2. 点击 **"创建版本"**
3. 填写版本信息：
   - 版本号：`1.0.0`
   - 更新日志：`初始版本 - 连接OpenClaw`
4. 点击 **"保存"**
5. 点击 **"发布"**
6. 点击 **"确认发布"**

---

## 第四步：配置应用权限（2 分钟）

### 4.1 添加必需权限

1. 在飞书开放平台的应用页面，找到 **"权限管理"** 或 **"权限配置"**
2. 点击 **"添加权限"**
3. 搜索并勾选以下权限：

**必需权限列表：**
- `im:message`
- `im:message.group_at_msg`
- `im:chat`
- `drive:drive`
- `drive:file:readonly`
- `drive:file`
- `wiki:wiki`
- `contact:user.base:readonly`

4. 点击 **"确认开通权限"** 

---

## 第五步：配置事件与回调（2 分钟）

### 5.1 配置长连接

⚠️ **重要：** 这是飞书机器人能够接收消息的关键配置！

1. 在飞书开放平台的应用页面，找到 **"事件与回调"**
2. 在 **"长连接配置"** 区域：
   - 点击 **"开启长连接"**
   - 确保状态显示为 **"已开启"**
3. 点击 **"保存"**

### 5.2 添加事件订阅

1. 在同一页面的 **"事件订阅"** 区域，点击 **"添加事件"**
2. 搜索并添加以下事件：

**必需事件列表：**
- `im.message.receive_v1` - 接收消息（群聊）
- `im.message.receive_v1` - 接收消息（私聊）
- `im.chat.member.add_v1` - 聊天成员添加
- `im.chat.member.delete_v1` - 聊天成员移除

3. 点击 **"保存"**

### 5.3 验证配置

确保以下状态正常：
- ✅ 长连接状态：**已开启**
- ✅ 事件订阅：**已配置**

### 5.4 发布新版本

⚠️ **重要：** 配置长连接和事件后，必须发布新版本才能生效！

1. 在飞书开放平台的应用页面，找到 **"版本管理与发布"**
2. 点击 **"创建版本"**
3. 填写版本信息：
   - 版本号：`1.1.0`（或更高版本号）
   - 更新日志：`启用长连接和事件订阅`
4. 点击 **"保存"**
5. 点击 **"发布"**
6. 点击 **"确认发布"**

---

## 第六步：添加应用到飞书（1 分钟）

### 6.1 添加到工作台

1. 在飞书开放平台的应用页面，点击 **"添加到工作台"**
2. 选择应用可见范围：
   - 选择你的部门
   - 或选择 **"全公司"**
3. 点击 **"添加"**

### 6.2 在飞书中打开应用

1. 打开飞书客户端
2. 进入 **"工作台"**
3. 找到你创建的 **"我的OpenClaw助手"** 应用
4. 点击打开应用，进入聊天界面

---

## 第七步：测试连接（1 分钟）

### 7.1 重启 OpenClaw

**本机部署用户：**

打开终端，运行：
```bash
openclaw gateway restart
```

等待几秒钟，OpenClaw 会重新启动。

**Docker 容器部署用户：**

在 openclaw-devkit 项目目录下，运行以下命令重启服务：
```bash
make restart
```

或使用容器内命令：
```bash
make cli CMD="gateway restart"
```

等待几秒钟，OpenClaw 会重新启动。

### 7.2 在飞书中测试

1. 打开飞书应用
2. 找到你创建的 OpenClaw 应用
3. 发送消息：`你好`
4. 等待回复

如果收到回复，恭喜你！配置成功！🎉

---

## ✅ 完成

现在你可以在飞书中使用 OpenClaw 了！

试试这些命令：
- `help` - 查看帮助
- `status` - 查看状态
- `time` - 查看时间

---

## 🆘 常见问题

### 问题：飞书中发送消息无响应

**解决方法：**

**本机部署用户：**

1. 检查 OpenClaw 是否运行：
   ```bash
   openclaw status
   ```

2. 查看错误日志：
   ```bash
   openclaw logs --tail 50
   ```

3. 重启 OpenClaw：
   ```bash
   openclaw gateway restart
   ```

**Docker 容器部署用户：**

1. 检查服务状态：
   ```bash
   make status
   ```

2. 查看 Gateway 日志：
   ```bash
   make logs
   ```

3. 重启服务：
   ```bash
   make restart
   ```

### 问题：配置文件格式错误

**解决方法：**

1. 恢复备份配置：

   **本机部署用户：**
   - 使用 `openclaw.json.backup`
   - 重新复制到 `openclaw.json`

   **Docker 容器部署用户：**
   ```bash
   # 在宿主机执行
   cp ~/.openclaw/openclaw.json.backup ~/.openclaw/openclaw.json
   make restart
   ```

2. 检查 JSON 格式：
   - 访问：https://jsonlint.com
   - 粘贴你的配置内容
   - 检查是否有错误

3. 确保使用双引号：
   - ✅ 正确：`"appId": "cli_123456"`
   - ❌ 错误：`'appId': 'cli_123456'`

### 问题：App Secret 错误

**解决方法：**

1. 检查 App Secret 是否正确复制：
   - 确保没有多余的空格
   - 确保完整复制（没有截断）

2. 如果忘记了 App Secret：
   - 在飞书开放平台重新生成
   - 更新配置文件
   - 重启 OpenClaw

   **Docker 容器部署用户：**
   ```bash
   # 更新配置后重启
   make restart
   ```

### 问题：Docker 容器内无法修改配置文件

**解决方法：**

容器内的 `~/.openclaw/` 目录已挂载到宿主机的 `~/.openclaw/`，建议直接在宿主机编辑：

1. **Mac 用户：**
   ```bash
   open ~/.openclaw/openclaw.json
   ```

2. **Linux 用户：**
   ```bash
   nano ~/.openclaw/openclaw.json
   ```

3. **Windows 用户：**
   - 使用记事本或 VS Code 打开 `%USERPROFILE%\.openclaw\openclaw.json`

编辑完成后，在项目目录下执行 `make restart` 使配置生效。

---

## 📝 配置参数说明

- `enabled`: 设置为 `true` 启用飞书功能
- `defaultAccount`: 默认账号名称，通常是 `"main"`
- `accounts`: 账号配置列表
  - `main`: 账号标识（可以自定义）
    - `appId`: 从飞书开放平台获取的 App ID
    - `appSecret`: 从飞书开放平台获取的 App Secret
    - `botName`: 机器人在飞书中显示的名称

---

**文档版本：** 1.4.0
**最后更新：** 2026-03-12
**更新内容：** 优化企业飞书用户的应用添加流程
