# 依赖信息 - Kill_line

## 版本历史

| 日期         | MC 版本  | 加载器           | JDK | 备注                |
| ---------- | ------ | ------------- | --- | ----------------- |
| 2026-04-20 | 1.21.4 | Fabric 0.16.9 | 21  | 初始配置              |
| 2026-04-24 | 1.21.4 | Architectury (common+fabric+forge+neoforge) | 21 | 多加载器架构 |
| 2026-04-25 | 1.20.1 | Forge 47.4.13 | 17  | 从 Fabric 1.21.4 移植 |

## 当前配置

### 1.20.1 Forge

| 项目          | 版本              |
| ----------- | --------------- |
| Minecraft   | 1.20.1          |
| JDK         | 17              |
| 加载器         | Forge 47.4.13   |
| 映射          | Official (Mojmap) |
| Mod 版本      | 2.0.0.1         |

### 1.21.4 Fabric (Architectury)

| 项目          | 版本                   |
| ----------- | -------------------- |
| Minecraft   | 1.21.4               |
| JDK         | 21                   |
| 加载器         | Fabric Loader 0.16.9 |
| Fabric API  | 0.114.0+1.21.4       |
| Yarn 映射     | 1.21.4+build.8       |
| Mod 版本      | 1.2.1 (稳定)           |

## 依赖关系

| Mod               | 类型      | 必需   | 说明                                |
| ----------------- | ------- | ---- | --------------------------------- |
| KillLineAnimation | Library | 是    | 死亡动画基础设施、碎片渲染、Mixin 抑制默认动画        |
| CriticalCore      | 前置 mod  | 可选   | 安装后斩杀线附魔只能通过合成链获取；未安装保持 V1.x 行为 |

## Gradle 配置 (1.20.1)

- 依赖: `implementation "com.kill_line.animation:KillLineAnimation"`
- 代理: 127.0.0.1:7890
- Mixin: 启用 (SpongePowered Mixin 0.8.5)
- 生产构建: `./gradlew jar reobfJar --offline`
- 输出位置: `build/reobfJar/output.jar` → `{mod根}/build/current/`

## 构建命名规范

`{mod名}-{版本号}[+{前置mod名}-{前置版本}]-{加载器}-{MC版本}-{YYMMDD}.jar`

示例:
- `kill_line-2.0.0.1+kill_line_animation-1.1.0-forge-1.20.1-260503.jar`
- `kill_line-1.2.1-fabric-1.21.4-260422.jar`
