# 功能需求 - KillLineAnimation

## 概述

独立死亡动画库 Mod。为 Minecraft Fabric Mod 提供自定义死亡动画的注册、触发和渲染基础设施。其他 Mod 可通过 API 注册自定义动画并在实体死亡时触发。

## 定位

- **独立 Library Mod**，不依赖 Kill_line
- 其他 Mod 可声明依赖后使用其 API
- 提供内置基础动画（如 Shatter）作为示例和默认

## API 设计

### 服务端 API

- `DeathAnimationRegistry.register(Identifier, int, DeathAnimationFactory)` — 注册动画类型
- `AnimationDispatcher.triggerDeathAnimation(ServerWorld, LivingEntity, DeathAnimationType, Vec3d)` — 触发死亡动画
- `DeathAnimationRegistry.getAll()` — 获取所有已注册类型（用于随机选择）

### 客户端 API

- `AbstractDeathAnimation` — 抽象基类，消费者 Mod 继承实现自定义动画
- `EntitySnapshot` — 实体死亡时的渲染快照数据
- `AnimatedFragment` — 单个碎片（带物理状态：位置/速度/旋转/alpha）
- `FragmentRenderer` — 工具类：渲染碎片
- `DeathAnimationManager` — 客户端动画生命周期管理

### 网络协议

- `DeathAnimationTriggerPacket` (S2C): entityId, position, bodyYaw, animationTypeId, directionX/Z

## 内置动画

> 首次设计会话: `70bbfb8a-fc07-4707-ba6c-5b6e60860bf1.jsonl` (2026-04-21)

| ID | ID (代码)          | 名称   | 时长(tick) | 核心视觉                                                    |
| -- | ----------------- | ---- | -------- | ------------------------------------------------------- |
| 0  | `SHATTER`         | 碎裂   | 35       | 身体爆炸成头/躯干/四肢碎片飞散                                        |
| 1  | `HORIZONTAL_SLASH`| 横斩   | 40       | 腰部横切线 → 上半身滑落 → 两半碎散                                    |
| 2  | `VERTICAL_SPLIT`  | 纵裂   | 45       | 从头到脚纵向切割 → 左右两半分开                                       |
| 3  | `DECAPITATION`    | 斩首   | 50       | 头部沿抛物线飞出 → 无头身体摇晃倒下 → 头部弹跳                              |
| 4  | `IMPALE`          | 穿刺   | 60       | 幽灵剑穿透身体 → 半跪低头钉地 → 身体碎裂                                 |
| 5  | `ASCENSION`       | 升华   | 50       | 身体缓慢上升 → 从脚到头溶解成金白色光粒                                   |
| 6  | `ICE_SHATTER`     | 冰碎   | 45       | 冻结蓝白色 → 裂缝扩散 → 碎成冰晶碎片                                   |
| 7  | `ASH`             | 灰化   | 40       | 从伤口位置开始 → 身体从内向外溶解成灰色余烬                                 |

### 动画类型说明

- **模型碎片类** (HORIZONTAL_SLASH, VERTICAL_SPLIT, DECAPITATION, SHATTER): 将实体模型拆分为头/躯干/四肢等部件，各自带物理模拟飞散
- **特效为主类** (IMPALE): 幽灵剑等额外视觉元素 + 模型碎片
- **粒子为主类** (ASCENSION, ASH): 实体整体溶解为粒子效果
- **混合类** (ICE_SHATTER): 模型碎片 + 粒子效果

### 非双足实体

非 HumanoidModel 的实体（如动物、怪物）不进行部件分离，使用整体模型变换 + 粒子效果。

## 核心机制

### 实体快照策略

实体在 kill() 后约 20 tick 从客户端移除，动画可能更久。收到动画包时立即快照：

- 位置、旋转、实体类型、纹理、模型引用、缩放
- 动画渲染完全不依赖实体对象

### 模型部件渲染

通过 EntityRenderDispatcher 获取实体类型的渲染器（单例），使用 ModelPart.render() 直接渲染特定部件。

### Mixin 抑制默认死亡动画

`EntityRenderDispatcherMixin` 在 render() HEAD 检查实体是否有活跃动画，有则跳过默认死亡渲染。

## 消费者 Mod 使用方式

### build.gradle

```groovy
includeBuild '../KillLineAnimation'
dependencies {
    modImplementation "com.kill_line.animation:KillLineAnimation"
}
```

### 注册动画

```java
// 服务端初始化时注册（ID和时长）
// 客户端初始化时提供 Factory
DeathAnimationRegistry.register(
    Identifier.of("my_mod", "my_animation"),
    40,
    (snapshot, type) -> new MyAnimation((EntitySnapshot) snapshot, type)
);
```

### 触发动画

```java
// 服务端触发
List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
DeathAnimationType type = types.get(world.getRandom().nextInt(types.size()));
AnimationDispatcher.triggerDeathAnimation(world, entity, type, attackDirection);
```

## 设计决策

| 决策             | 选择             | 原因                    |
| -------------- | -------------- | --------------------- |
| 独立 Library Mod | 是              | 动画系统可复用，其他 Mod 也能注册动画 |
| 网络同步           | 自定义 S2C 包      | 包含动画类型ID和攻击方向         |
| 模型渲染           | ModelPart 直接渲染 | 避免完整重建渲染管线            |
| 碎片物理           | 简单欧拉积分         | 性能友好，效果足够             |
| 并发动画上限         | 20             | 防止大量同时击杀导致卡顿          |

## 功能列表

### v1.0.0

- [x] DeathAnimationRegistry 注册 API
- [x] AnimationDispatcher 服务端触发 API
- [x] AbstractDeathAnimation 抽象基类
- [x] EntitySnapshot 实体快照
- [x] AnimatedFragment 碎片物理
- [x] FragmentRenderer 碎片渲染器
- [x] DeathAnimationManager 客户端管理器
- [x] DeathAnimationRenderer 世界渲染
- [x] EntityRenderDispatcherMixin 抑制默认死亡动画
- [x] 内置 Shatter 动画
- [x] 网络协议 (DeathAnimationTriggerPacket S2C)
