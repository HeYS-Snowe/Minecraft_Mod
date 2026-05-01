# CLAUDE.md - Minecraft Mod 开发项目

## 项目概述

Minecraft Mod 开发项目，支持 Fabric / Forge / NeoForge 三个加载器，单人开发。

**规则体系**: 使用 `D:\Code\.Rules` 规则体系，预设为 `stacks/minecraft-mod/presets/minecraft-mod.yaml`

## 项目结构

每个 MC 版本通过 **IDE 的 Minecraft Development 插件**创建独立的 Gradle 项目，放置在 `{mod名称}/{MC版本}/` 目录下。项目内部结构由 IDE 插件根据选择的加载器自动生成。

当同一 MC 版本需要支持多个加载器时，采用 **Architectury 多模块架构**（common + fabric/forge/neoforge），此时需通过 Architectury 模板创建。

```
Minecraft_Mod/
├── doc/                                    # 全局开发文档
│   ├── development-guide.md                # Mod 开发综合指南
│   └── project-structure.md                # 项目结构说明
│
└── {mod名称}/                              # 每个具体 mod
    ├── doc/                                # mod 开发文档（跨版本共享）
    │   ├── dependencies.md                 # 依赖版本
    │   └── requirements.md                 # 功能需求
    ├── build/
    │   ├── current/                        # 当前编译版本
    │   └── archive/                        # 历史版本
    │
    └── {MC版本}/                           # IDE 插件创建的独立 Gradle 项目
        │
        ├── [单加载器模式 — 默认]            # IDE 插件直接生成的标准结构
        │   ├── src/main/
        │   │   ├── java/                   # 业务逻辑 + 入口类 + 注册
        │   │   └── resources/              # 加载器配置 + 资源文件
        │   ├── build.gradle
        │   ├── settings.gradle
        │   ├── gradle.properties
        │   └── gradle/
        │
        └── [多加载器模式 — Architectury]    # 仅需跨加载器时使用
            ├── common/                     # 共享模块（核心逻辑、资源）
            ├── fabric/                     # Fabric 平台模块
            ├── forge/                      # Forge 平台模块
            ├── neoforge/                   # NeoForge 平台模块
            ├── build.gradle
            ├── settings.gradle
            ├── gradle.properties
            └── gradle/
```

## 开发规范

### 规则优先级

```
项目规则 > minecraft-mod 预设 > 核心规则 > 默认行为
```

### 必须遵守

- mod_id 仅使用小写字母、数字、下划线，长度 2-64
- 所有可翻译文本放在 lang 文件中，NEVER 硬编码
- 新 MC 版本项目**必须**通过 IDE 的 Minecraft Development 插件创建，NEVER 由 AI 手动创建项目基础结构
- 构建产物按规范命名后存入 build/current/
- 构建产物命名: `{mod名}-{版本号}[+{前置mod名}-{前置版本}]-{加载器}-{MC版本}-{YYMMDD}.jar`
- 版本号格式: `{MAJOR}.{MINOR}.{PATCH}.{BUILD}[.fix{N}]`，每次构建更新
- 前置 mod 本身使用普通命名（无 `+依赖` 部分）
- 新建 mod 前必须先在 doc/ 中创建依赖信息和需求文档
- NEVER 全局安装 Gradle，始终使用项目自带的 gradlew

### 新版本项目创建流程

**核心原则**: 新 MC 版本项目由用户通过 IDE 的 Minecraft Development 插件创建，AI 仅提供配置信息。

**流程**:

1. 用户提出创建新版本需求（mod 名称、MC 版本、加载器）
2. AI 根据需求提供 IDE 插件所需的所有配置信息（见下方模板）
3. 用户通过 IDE 插件创建项目，放置到 `{mod名称}/{MC版本}/` 目录
4. 用户完成创建并通知 AI
5. AI 开始协助代码开发（添加功能、迁移代码等）

**AI 提供的配置信息模板**:

| 配置项                    | 说明                   | 示例                                                  |
| ---------------------- | -------------------- | --------------------------------------------------- |
| Mod ID                 | 仅小写字母、数字、下划线，2-64 字符 | `kill_line`                                         |
| Mod Name               | 显示名称                 | `Kill Line`                                         |
| Package / Group ID     | Java 包名              | `com.kill_line`                                     |
| MC Version             | 目标 Minecraft 版本      | `1.20.1`                                            |
| Loader                 | 加载器类型                | `Fabric` / `Forge` / `NeoForge`                     |
| Loader Version         | 加载器版本                | `Fabric Loader 0.15.x` / `Forge 47.x`               |
| Java Version           | JDK 版本               | `17` (1.20.1) / `21` (1.21.4+)                      |
| Mappings               | 映射类型                 | Fabric: `Yarn`, Forge/NeoForge: `Official (Mojmap)` |
| Fabric API / Forge MDK | API/MDK 版本           | `Fabric API 0.92.x` / `Forge 47.4.x`                |

### 禁止事项

- NEVER 由 AI 手动创建新 MC 版本的项目基础结构（build.gradle、settings.gradle、gradle.properties 等），必须通过 IDE 插件创建
- NEVER 在 common 模块中直接引用任何加载器特有的 API（使用平台抽象层）
- NEVER 将平台相关代码放入 common 模块
- NEVER 直接修改 Minecraft 源码（使用 Mixin）
- NEVER 硬编码 Minecraft 版本号
- NEVER 在主线程执行耗时操作

### 多加载器架构模式（仅 Architectury 项目适用）

当同一 MC 版本需要支持多个加载器时，使用 Architectury 多模块架构。**单加载器项目不需要此模式。**

**代码分层原则**：

- **common 模块**：所有业务逻辑、物品/方块行为、工具类、Mixin、共享资源（材质、模型、lang、配方）
- **平台模块（fabric/forge/neoforge）**：仅包含入口类、注册逻辑、平台特有实现

**平台抽象**：

- 在 common 中定义接口或使用 `@ExpectPlatform` 声明平台差异点
- 各平台模块提供具体实现（如注册 API 差异）
- common 中的代码通过抽象层调用，不感知具体平台

**代码放置判断**：

```
这段代码是否使用了加载器特有的 API？
├── 否 → 放入 common/
└── 是 → 能否通过接口抽象？
    ├── 能 → 接口放 common/，实现在各平台模块
    └── 不能 → 放入各平台模块（极少情况）
```

### 参考文档

- 核心原则: `D:\Code\.Rules\common\core\principles.md`
- 安全规范: `D:\Code\.Rules\common\core\security.md`
- 代码风格: `D:\Code\.Rules\common\core\code-style.md`
- 开发流程: `D:\Code\.Rules\common\core\workflow.md`
- Mod 预设: `D:\Code\.Rules\stacks\minecraft-mod\presets\minecraft-mod.yaml`
- 开发指南: `doc/development-guide.md`

## 提交规范

```
<type>(<mod名称>): <subject>

类型: feat | fix | refactor | docs | chore
scope: mod 名称

示例:
feat(MoreTools): 添加红宝石物品
fix(MoreTools): 修复合成配方 JSON 格式错误
refactor(MoreTools): 重构物品注册逻辑
```
