# NekoEssentialX - 猫娘风格的Minecraft综合插件

## 插件简介

NekoEssentialX是一个适用于Minecraft Spigot服务器的综合性插件，基于EssentialX开发，融入了猫娘的害羞、可爱、猫娘风格。插件采用简洁可爱的语言风格，为服务器带来温馨的猫娘氛围。

## 核心功能

### 1. 箱子GUI界面系统
- **全新的箱子界面交互方式**
- 所有功能支持双调用方式：指令调用 + GUI调用
- 直观的图标分类，清晰的标签导航
- 支持鼠标点击交互，提供清晰的视觉反馈
- 完整的导航栏（返回、上一页、下一页、信息）
- 分页支持，处理大量数据

### 2. 猫娘风格聊天系统
- 所有聊天消息自动转换为猫娘风格
- 带有害羞、可爱的语言特征
- 自动添加「喵~」后缀
- 支持私聊消息的风格转换
- 成就消息的美化处理

### 2. 头衔系统
- 可配置的头衔管理
- 在聊天和Tab列表中显示头衔
- 支持系统头衔和自定义头衔
- 头衔前缀显示
- 完整的命令支持

### 3. 传送系统
- TPA请求系统（发送/接受/拒绝）
- 家（Home）系统
- 传送点（Warp）管理
- 跨世界传送支持

### 4. 经济系统
- Vault经济插件集成
- 货币转账、余额查询
- 经济排行榜
- 与主流经济插件兼容

### 5. 每日签到
- 每日登录奖励
- 累计登录天数统计
- 可配置的奖励金额
- 防止重复签到机制

### 6. 新手礼包
- 新玩家首次登录奖励
- 随机物品奖励
- 防止重复领取机制

### 7. AFK系统
- 自动检测AFK状态
- AFK玩家标记
- 可配置的AFK检测时间

### 8. 基础管理工具
- 玩家列表
- 服务器信息
- 帮助系统

### 9. 防爆系统
- 全面的爆炸防护
- 支持拦截末影龙、凋零等实体的破坏行为
- 可配置的爆炸类型控制
- 支持TNT、末影水晶、床等爆炸的防护
- 实体破坏方块控制
- 详细的日志记录

## 安装方法

1. 确保您的服务器运行的是Java 17+和支持的Minecraft版本
2. 下载NekoEssentialX插件JAR文件
3. 将JAR文件放入服务器的`plugins`文件夹中
4. 启动服务器，插件将自动生成配置文件
5. 根据需要修改配置文件
6. 重启服务器或使用`/nekoessentialsx reload`命令重载插件

## 核心命令

### 箱子GUI命令（双调用方式）
- `/mainmenu` - 打开主菜单GUI，访问所有功能
- `/home` - 打开家系统GUI（不指定家名称时）
- `/warp` - 打开传送点GUI（不指定传送点名称时）
- `/kit` - 打开工具包GUI（不指定工具包名称时）
- `/title` - 打开头衔系统GUI（不指定操作时）
- `/money` - 打开经济系统GUI
- `/tpa` - 打开传送请求GUI
**双调用方式说明：**
- **指令调用**：直接输入带参数的指令，如 `/home 家名`、`/warp 传送点名`
- **GUI调用**：输入不带参数的指令，如 `/home`、`/warp`，将打开对应的箱子GUI界面
- 两种调用方式功能完全一致，数据实时同步

### 基础命令
- `/nekoessentialsx` - 插件主命令
- `/help` - 显示帮助信息
- `/info` - 显示服务器信息
- `/list` - 显示在线玩家列表

### 传送命令
- `/tpa <player>` - 发送传送请求
- `/tpaccept [player|*]` - 接受传送请求
- `/tpdeny [player|*]` - 拒绝传送请求
- `/tpacancel [player]` - 取消传送请求
- `/sethome [name]` - 设置家
- `/home [name]` - 回家
- `/delhome [name]` - 删除家
- `/setwarp <name>` - 设置传送点
- `/warp <name>` - 前往传送点
- `/delwarp <name>` - 删除传送点

### 头衔命令
- `/title set <title>` - 设置自己的头衔
- `/title list` - 列出可用头衔
- `/title info <title>` - 查看头衔信息
- `/title give <player> <title>` - 授予头衔（管理员）
- `/title take <player> <title>` - 移除头衔（管理员）
- `/title clear [player]` - 清除头衔

### 经济命令
- `/money` - 查看余额
- `/money <player>` - 查看其他玩家余额
- `/pay <player> <amount>` - 转账给玩家

### 签到命令
- `/checkin` - 每日签到

### 私聊命令
- `/msg <player> <message>` - 发送私聊消息
- `/tell <player> <message>` - 发送私聊消息
- `/whisper <player> <message>` - 发送私聊消息
- `/w <player> <message>` - 发送私聊消息
- `/r <message>` - 回复最近的私聊消息
- `/reply <message>` - 回复最近的私聊消息

## 配置文件

### 主配置文件
- `plugins/NekoEssentialX/config.yml` - 核心配置

### 猫娘风格配置
- `plugins/NekoEssentialX/catstyle.yml` - 风格设置
  - `enabled` - 是否启用猫娘风格
  - `suffix` - 消息后缀
  - 语言转换规则

### 头衔配置
- `plugins/NekoEssentialX/titles.yml` - 头衔定义
  - 系统头衔配置
  - 权限设置

### 新手礼包配置
- `plugins/NekoEssentialX/newbiegiftpack.yml` - 礼包物品配置

### 防爆系统配置
- `plugins/NekoEssentialX/config.yml` - 防爆系统核心配置
  - `anti-explosion.enabled` - 防爆系统总开关
  - `anti-explosion.entity-explosion` - 生物爆炸配置
  - `anti-explosion.entity-block-break` - 实体破坏方块配置
  - `anti-explosion.tnt-explosion` - TNT爆炸配置
  - `anti-explosion.end-crystal-explosion` - 末影水晶爆炸配置
  - `anti-explosion.bed-explosion` - 床爆炸配置
  - `anti-explosion.other-explosion` - 其他爆炸配置
  - `anti-explosion.logging` - 日志配置

## 权限节点

### 基本权限
- `nekoessentialsx.use` - 允许使用插件基础功能
- `nekoessentialsx.admin` - 管理员权限

### 传送权限
- `nekoessentialsx.tpa` - 允许使用TPA命令
- `nekoessentialsx.home` - 允许使用家系统
- `nekoessentialsx.warp` - 允许使用传送点系统

### 头衔权限
- `nekoessentialsx.title` - 允许使用头衔命令
- `nekoessentialsx.title.admin` - 允许管理所有头衔
- `nekoessentialsx.titles.<title>` - 允许使用特定头衔

### 防爆系统权限
- `nekoessentialsx.anti-explosion` - 允许使用防爆系统相关功能
- `nekoessentialsx.anti-explosion.admin` - 允许管理防爆系统配置

## 插件特点

### 风格特色
- 基于结城さくな的说话特征设计
- 害羞、可爱、猫娘风格
- 所有消息统一风格
- 成就消息美化

### 技术特点
- Java 17+开发
- Maven构建系统
- 模块化设计
- 可扩展的插件架构
- SQLite数据库存储

## 开发说明

本插件使用Maven构建，支持Java 17+。

### 构建命令
```bash
mvn clean package
```

### 开发依赖
- Spigot API
- Vault API
- SQLite JDBC

## 支持的Minecraft版本

- 1.19.x
- 1.20.x

## 许可证

本插件采用GPLv3许可证，基于EssentialX开发。

## 致谢

感谢EssentialX团队的优秀框架！

---

**喵~ 希望您喜欢这个可爱的插件！** 🐱