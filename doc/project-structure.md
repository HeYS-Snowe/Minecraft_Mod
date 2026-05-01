# 项目结构说明

> 说明 Minecraft_Mod 项目的目录组织方式和项目创建流程。

---

## 设计理念

- **每个 MC 版本独立** — 不同 MC 版本的 API 差异较大，分别维护
- **IDE 插件创建** — 项目通过 IDE 的 Minecraft Development 插件创建，确保正确的运行配置和项目结构
- **AI 提供配置** — AI 仅提供创建时所需的配置信息，不手动创建项目基础结构
- **按需多加载器** — 默认单加载器项目；同一 MC 版本需多加载器支持时，使用 Architectury 架构

---

## 项目创建流程

### 流程概述

1. 用户提出需求 → AI 提供配置信息 → 用户通过 IDE 插件创建项目 → AI 协助开发

### IDE 插件配置信息模板

创建新 MC 版本项目时，AI 应提供以下信息：

| 配置项                | 说明                                      | 获取来源                  |
| ------------------ | --------------------------------------- | --------------------- |
| Mod ID             | 小写字母、数字、下划线，2-64 字符                     | mod 需求文档              |
| Mod Name           | 显示名称                                    | mod 需求文档              |
| Package / Group ID | Java 包名                                 | 如 `com.kill_line`     |
| MC Version         | 目标版本                                    | 用户需求                  |
| Loader             | Fabric / Forge / NeoForge               | 用户需求                  |
| Java Version       | 17 (MC 1.18-1.20.4) / 21 (MC 1.20.5+)   | MC 版本决定               |
| Mappings           | Fabric: Yarn / Forge/NeoForge: Official | 加载器决定                 |
| Loader Version     | 具体版本号                                   | dependencies.md 或官网查询 |
| API/MDK Version    | Fabric API / Forge MDK 版本               | dependencies.md 或官网查询 |

### 放置位置

创建后的项目放置在 `{mod名称}/{MC版本}/` 目录下。

---

## 目录层级

```
Minecraft_Mod/
│
├── doc/                                    # 全局开发文档
│   ├── development-guide.md                # Mod 开发综合指南
│   └── project-structure.md                # 本文件
│
└── {mod名称}/                              # 每个 mod 一个顶级文件夹
    ├── doc/                                # mod 级开发文档（跨版本共享）
    │   ├── dependencies.md                 # 依赖版本记录
    │   └── requirements.md                 # 功能需求文档
    │
    ├── build/                              # 编译产物
    │   ├── current/                        # 当前最新版本
    │   └── archive/                        # 历史版本存档
    │
    └── {MC版本}/                           # IDE 插件创建的独立 Gradle 项目
        │
        ├── [单加载器模式 — 默认]            # IDE 插件直接生成的标准结构
        │   ├── src/main/
        │   │   ├── java/                   # 业务逻辑 + 入口类 + 注册
        │   │   │   └── com/{namespace}/{mod_id}/
        │   │   │       ├── item/
        │   │   │       ├── block/
        │   │   │       ├── mixin/
        │   │   │       └── ...
        │   │   └── resources/              # 加载器配置 + 资源文件
        │   │       ├── assets/{mod_id}/    # 材质、模型、lang
        │   │       ├── data/{mod_id}/      # 配方、战利品表、标签
        │   │       └── {加载器配置文件}     # fabric.mod.json / mods.toml
        │   ├── build.gradle
        │   ├── settings.gradle
        │   ├── gradle.properties
        │   └── gradle/
        │
        └── [多加载器模式 — Architectury]    # 仅需跨加载器时使用
            ├── common/                     # 共享模块
            │   └── src/main/
            │       ├── java/               # 跨平台业务逻辑
            │       └── resources/          # 共享资源
            ├── fabric/                     # Fabric 平台模块
            ├── forge/                      # Forge 平台模块
            ├── neoforge/                   # NeoForge 平台模块
            ├── build.gradle
            ├── settings.gradle
            ├── gradle.properties
            └── gradle/
```

## 各层职责

### {mod名称}

以 mod 名称命名，如 `Kill_line`、`KillLineAnimation`。包含该 mod 所有版本的项目。

### doc/ (mod 级)

存放该 mod 跨版本共享的开发文档：

| 文件                | 内容                         |
| ----------------- | -------------------------- |
| `dependencies.md` | 各 MC 版本的 JDK、加载器、映射版本等依赖信息 |
| `requirements.md` | 该 mod 的功能需求、设计目标           |

### build/ (mod 级)

存放编译产物，不提交到 Git：

| 子目录        | 内容               |
| ---------- | ---------------- |
| `current/` | 当前最新版本，每次构建后更新   |
| `archive/` | 历史版本存档，按日期+版本号命名 |

### {MC版本}/

一个独立的 Gradle 项目，由 IDE 的 Minecraft Development 插件创建。内部结构由插件根据加载器类型自动生成。

#### 单加载器项目（默认）

IDE 插件直接生成的标准项目结构，包含完整的 src/、build.gradle、settings.gradle 等。

#### 多加载器项目（Architectury — 仅在需要时使用）

当同一 MC 版本需支持多个加载器时使用，包含 common + fabric/forge/neoforge 子模块。

##### common/ — 共享模块

**放入 common 的内容**：

- 物品、方块、附魔等游戏对象的逻辑代码
- Mixin 类（跨平台通用的 Mixin）
- 网络通信协议代码
- 工具类、辅助方法
- 所有资源文件（材质、模型、lang、配方、战利品表、标签）
- 平台抽象接口（`platform/` 包）

**不要放入 common 的内容**：

- 任何 `net.fabricmc.*`、`net.minecraftforge.*`、`net.neoforged.*` 的 import
- 使用加载器特有 API 的代码
- 平台特有的注册逻辑

##### fabric/ — Fabric 平台模块

- 主入口类（实现 `ModInitializer`）
- 使用 Fabric Registry API 的注册代码
- `fabric.mod.json`
- Fabric 特有的 Mixin 配置

##### forge/ — Forge 平台模块

- 主入口类（`@Mod` 注解）
- 使用 Forge 事件总线的注册代码
- `META-INF/mods.toml`
- Forge 特有的 Mixin 配置

##### neoforge/ — NeoForge 平台模块

- 主入口类（`@Mod` 注解）
- 使用 NeoForge 事件总线的注册代码
- `META-INF/neoforge.mods.toml`
- NeoForge 特有的 Mixin 配置

## 平台抽象模式（仅 Architectury 项目）

### 代码放置判断

```
新增一段代码时：
│
├── 是否使用了加载器特有的 API？
│   ├── 否 → 放入 common/
│   └── 是 → 能否通过接口抽象？
│       ├── 能 → 接口放 common/，实现在各平台模块
│       └── 不能 → 放入各平台模块（极少情况，如 Mixin 差异）
│
└── 是资源文件吗？
    ├── 材质、模型、lang、配方 → 放入 common/src/main/resources/
    └── 平台配置（fabric.mod.json 等）→ 放入对应平台模块
```

## gradle.properties 模板

```properties
# Mod 信息
mod_id=your_mod_id
mod_name=Your Mod Name
mod_version=1.0.0

# Minecraft
minecraft_version=1.21.4

# Fabric
fabric_loader_version=0.19.2
fabric_api_version=0.100.0+1.21.4

# Forge
forge_version=1.21.4-54.0.0

# NeoForge
neoforge_version=21.4.0

# 通用
java_version=21
yarn_mappings=1.21.4+build.8
architectury_version=13.0.0
```

## doc/ 模板

### dependencies.md 模板

```markdown
# 依赖信息 - {mod名称}

## 当前配置

### 1.21.4

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.4 |
| JDK | 21 |
| Fabric Loader | 0.19.2 |
| Fabric API | 0.100.x |
| Forge | 1.21.4-54.0.0 |
| NeoForge | 21.4.0 |
| Yarn 映射 | 1.21.4+build.x |
| Architectury | 13.0.x |

## 版本历史

| 日期 | MC 版本 | 变更 | 备注 |
|------|---------|------|------|
| YYYY-MM-DD | 1.21.4 | 初始配置 | 支持 Fabric/Forge/NeoForge |
```

### requirements.md 模板

```markdown
# 功能需求 - {mod名称}

## 概述
{简要描述这个 mod 做什么}

## 功能列表

### v1.0.0
- [ ] 功能1: {描述}
- [ ] 功能2: {描述}

### v1.1.0 (计划)
- [ ] 功能3: {描述}

## 设计决策
| 决策 | 选择 | 原因 |
|------|------|------|
| 多加载器方案 | Architectury | 从架构层面保证代码复用 |
```

## .gitignore 建议

```
# Gradle
.gradle/
**/build/
!build/current/.gitkeep
!build/archive/.gitkeep

# IDE
.idea/
*.iml
.vscode/
.settings/
.classpath
.project

# Minecraft
run/
logs/

# OS
.DS_Store
Thumbs.db
```
