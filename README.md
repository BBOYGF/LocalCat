# 🐱 **Local Cat** - 跨设备极简传输工具  
**让数据在设备间自由流动，告别繁琐的手动备份**

---

## 🌟 功能亮点

### 📱 多平台支持
- **Android 移动端** + **Windows/Mac 桌面端** 全平台覆盖
- **iOS 客户端**（未来版本支持）持续更新中 ⏳
- 基于局域网直连传输，**无需互联网**，不消耗流量  
- 全平台客户端一键启动，自动发现同网络设备  

### ⚡ 极速传输体验
- 基于局域网直连传输，速度可达传统方案5-10倍    
- 传输过程内存优化，低资源占用不影响设备使用
- **断点续传**（开发中）即将上线，敬请期待 ⚙️

### 🔄 智能文件监控
- 自定义监控目录与过滤规则（支持扩展名/文件夹/正则表达式）  
- 实时扫描指定目录，快速生成待传输文件列表  
- 支持手动批量选择文件或设置自动监控策略 

### 🔒 安全传输保障
- 数据仅在局域网内流转，**不经过第三方服务器**  
- 传输通道AES-256加密，防止敏感信息泄露  
- 可视化传输日志，实时追踪文件流向  

---

## 📥 下载安装

前往 [Release 页面](https://github.com/BBOYGF/Local_Cat/releases) 获取最新版本：

| 平台 | 下载指南 | 系统要求 |
|------|----------|----------|
| **Android** | 下载 `.apk` 文件直接安装 | Android 8.0+ |
| **Windows** | 下载 `.exe` 安装包 | Windows 10+ |
| **macOS**   | 下载 `.dmg` 镜像文件 | macOS 11+ |

[![Windows Download](https://img.shields.io/badge/Windows-v1.0.0-blue?logo=windows)](https://github.com/BBOYGF/Local_Cat/releases)
[![Android Download](https://img.shields.io/badge/Android-v1.0.0-green?logo=android)](https://github.com/BBOYGF/Local_Cat/releases)
[![macOS Download](https://img.shields.io/badge/macOS-v1.0.0-silver?logo=apple)](https://github.com/BBOYGF/Local_Cat/releases)

---

## 🚀 快速开始

1. **设备准备**  
   - 在所有设备安装对应版本客户端
   - 确保设备处于同一局域网环境

2. **角色切换**  
   - 打开应用后选择工作模式：
   -- 📤 发送者模式：选择要传输的文件
   -- 📥 接收者模式：等待接收文件

3. **建立连接**  
   - 接收者点击「开始监听」
   - 发送者切换至发送者界面，点击扫描接受者
   - 搜索到接受者后点击连接
     
4. **文件传输**  
   - 发送者设置文件过滤规则（如*.jpg, /DCIM等）
   - 点击「扫描文件」生成待传输列表
   - 点击「立即传输」开始发送
---

## 🏆 为什么选择快猫？

| 功能                | 传统方案                | Local Cat 方案           |
|---------------------|-------------------------|--------------------------|
| **传输方式**        | USB线/云盘中转          | 纯局域网P2P直连 |
| **平台覆盖**      | 多平台体验不一致    | 全平台KMP统一代码基         |
| **文件管理**      | 手动选择文件            | 智能规则自动筛选          |
| **隐私保护**        | 存在云存储泄露风险        | 数据永不离开本地网络            |

---

## 🎯 典型使用场景

- **摄影工作者**：自动同步手机拍摄的RAW照片到工作站  
- **移动办公**：即时传输文档/表格到会议演示电脑  
- **开发者**：快速同步测试日志/APK包到开发机  
- **家庭用户**：自动备份孩子手机中的照片/视频  

---

## 🛠 技术栈

- **跨平台核心**：Kotlin Multiplatform Mobile (KMP) 

---

## 🤝 参与贡献

我们欢迎任何形式的贡献：
- 提交 Issue 报告问题或建议
- Fork 项目并提交 Pull Request
- 帮助完善 [项目文档](https://github.com/BBOYGF/Local_Cat/wiki)
- 分享使用经验到 Discussions

---

📮 **问题反馈**：通过 [Issues](https://github.com/BBOYGF/Local_Cat/issues) 提交问题  
📜 **开源协议**：Apache License 2.0  
