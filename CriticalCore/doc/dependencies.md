# 依赖信息 - CriticalCore

## 当前配置

### 1.20.1 Forge

| 项目          | 版本              |
| ----------- | --------------- |
| Minecraft   | 1.20.1          |
| JDK         | 17              |
| 加载器         | Forge 47.4.13   |
| 映射          | Official (Mojmap) |

## 版本历史

| 日期         | MC 版本  | 加载器           | JDK | 备注    |
| ---------- | ------ | ------------- | --- | ----- |
| 2026-05-03 | 1.20.1 | Forge 47.4.13 | 17  | 初始配置  |

## 依赖关系

| 依赖方向        | 说明                        |
| ----------- | ------------------------- |
| Kill_line 依赖本 mod | Kill_line V2.0 可选前置，安装后斩杀线附魔只能通过合成链获取 |
| 本 mod 不依赖其他 mod  | 独立运行                       |

## Gradle 配置

- 代理: 127.0.0.1:7890
- Mixin: 启用 (SpongePowered Mixin 0.8.5)
- 生产构建: `./gradlew jar reobfJar --offline`
- 输出位置: `build/reobfJar/output.jar` → `{mod根}/build/current/`
