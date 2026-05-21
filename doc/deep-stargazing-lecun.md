# 临界核心 (Critical Core) Mod — V2.0 实现计划

## Context

Kill_line (斩杀线) mod 功能过于强大（一击必杀+击杀创造模式），需要增加获取难度。设计一个可选前置 mod **临界核心**，安装后斩杀线附魔只能通过复杂的特殊合成链获取；未安装时保持现有行为。

---

## 第一步：用 IDE 创建 Critical Core 项目

| 配置项                | 值                             |
| ------------------ | ----------------------------- |
| Mod ID             | `critical_core`               |
| Mod Name           | Critical Core                 |
| Package / Group ID | `com.kill_line.critical_core` |
| MC Version         | `1.20.1`                      |
| Loader             | `Forge`                       |
| Loader Version     | `47.4.13`                     |
| Java Version       | `17`                          |
| Mappings           | `Official (Mojmap)`           |

项目路径：`CriticalCore/1.20.1/`

创建完成后通知我。

---

## 第二步：贴图 AI 生成清单

### 临界核心 mod 贴图（`assets/critical_core/textures/`）

#### 物品贴图（16×16，`item/`）

| 文件名                     | 物品   | AI 提示词                                                                                                                                  |
| ----------------------- | ---- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `threshold_shard.png`   | 阈界碎片 | Minecraft 16x16 item icon, glowing purple crystal shard, dark background, pixel art style                                               |
| `critical_core.png`     | 临界核心 | Minecraft 16x16 item icon, pulsing core of purple and dark energy, crystalline sphere with inner glow, dark background, pixel art style |
| `critical_handbook.png` | 临界手札 | Minecraft 16x16 item icon, ancient book with purple runes on cover, glowing edges, dark mystical tome, pixel art style                  |
| `void_bottle.png`       | 虚空瓶  | Minecraft 16x16 item icon, glass bottle containing swirling dark purple void energy, cork stopper, pixel art style                      |
| `stable_core.png`       | 稳定核心 | Minecraft 16x16 item icon, small blue-white stabilizing core, calm blue glow, metallic frame, pixel art style                           |

#### 方块贴图（16×16，`block/`）

| 文件名                             | 方块       | AI 提示词                                                                                                                                                   |
| ------------------------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `threshold_crystal_cluster.png` | 阈界晶簇     | Minecraft 16x16 block texture, purple crystal cluster growing from stone, bioluminescent glow, amethyst-like formation, pixel art style                  |
| `pulse_forge_front.png`         | 脉冲锻炉（正面） | Minecraft 16x16 block texture, ancient stone forge with purple rune engravings, dark metal frame, mystical crafting station, front view, pixel art style |
| `pulse_forge_side.png`          | 脉冲锻炉（侧面） | Minecraft 16x16 block texture, ancient stone forge side view, purple rune engravings on dark stone, pixel art style                                      |
| `pulse_forge_top.png`           | 脉冲锻炉（顶面） | Minecraft 16x16 block texture, forge top view with circular rune pattern, purple energy lines on dark stone, pixel art style                             |

#### GUI 贴图（256×256，`gui/`）

| 文件名                   | 用途          | AI 提示词                                                                                                                                                |
| --------------------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `pulse_forge_gui.png` | 脉冲锻炉 GUI 背景 | Minecraft GUI background 256x256, dark stone frame with purple rune border, two slots (input and output), crafting furnace interface, pixel art style |

---

### Kill_line V2.0 mod 新增贴图（`assets/kill_line/textures/`）

#### 物品贴图（16×16，`item/`）

| 文件名                       | 物品   | AI 提示词                                                                                                                                        |
| ------------------------- | ---- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `ancient_cornerstone.png` | 远古今石 | Minecraft 16x16 item icon, ancient dark stone block with glowing cyan runes, weathered surface, pixel art style                               |
| `void_frame.png`          | 虚空框架 | Minecraft 16x16 item icon, dark obsidian frame with void portal energy inside, rectangular border, pixel art style                            |
| `blood_moon_core.png`     | 血月核心 | Minecraft 16x16 item icon, dark red glowing orb, blood moon energy, crimson pulsing core, pixel art style                                     |
| `silence_dust.png`        | 寂灭尘埃 | Minecraft 16x16 item icon, dark grey dust pile with faint purple sparkle, ash-like particles, pixel art style                                 |
| `fate_thread.png`         | 命运丝线 | Minecraft 16x16 item icon, golden glowing thread of fate, fine silk strand with sparkles, pixel art style                                     |
| `ancient_page.png`        | 古神残页 | Minecraft 16x16 item icon, torn ancient parchment page with faded golden runes, aged paper, pixel art style                                   |
| `critical_essence.png`    | 临界精粹 | Minecraft 16x16 item icon, concentrated purple-gold crystalline essence, fusion of threshold energy, glowing dual-color core, pixel art style |
| `critical_scroll.png`     | 临界卷轴 | Minecraft 16x16 item icon, sealed scroll with dark red ribbon, glowing purple runes on parchment, pixel art style                             |
| `ancient_iron_block.png`  | 远古铁块 | Minecraft 16x16 item icon, dark iron ingot with ancient fragment embedded, netherite-like metal, pixel art style                              |
| `soul_spark.png`          | 灵魂火花 | Minecraft 16x16 item icon, floating soul fire spark, blue-white ghostly flame, wispy ethereal fire, pixel art style                           |
| `bleeding_rose.png`       | 泣血玫瑰 | Minecraft 16x16 item icon, dark red rose dripping crimson drops, withered stem, blood-red petals, pixel art style                             |
| `moon_shadow_altar.png`   | 月影祭坛 | Minecraft 16x16 item icon, small snow and lapis altar, crescent moon symbol, icy blue glow, pixel art style                                   |

#### 方块贴图（16×16，`block/`）

| 文件名                           | 方块       | AI 提示词                                                                                                                                                                    |
| ----------------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `critical_workshop_front.png` | 临界工坊（正面） | Minecraft 16x16 block texture, mystical crafting workshop front, dark stone with purple and gold rune circuits, 3x3 grid indentation, ancient technology, pixel art style |
| `critical_workshop_side.png`  | 临界工坊（侧面） | Minecraft 16x16 block texture, workshop side panel, dark stone with faint rune lines, pixel art style                                                                     |
| `critical_workshop_top.png`   | 临界工坊（顶面） | Minecraft 16x16 block texture, workshop top, flat dark stone with circular rune pattern, pixel art style                                                                  |
| `permeation_altar.png`        | 渗透祭坛     | Minecraft 16x16 block texture, dark stone altar with flowing purple energy veins, central receptacle, rune-inscribed base, pixel art style                                |
| `edict_anvil_top.png`         | 诫命之砧（顶面） | Minecraft 16x16 block texture, ancient dark iron anvil top, crimson runes engraved on surface, heavy mystical anvil, pixel art style                                      |
| `edict_anvil_side.png`        | 诫命之砧（侧面） | Minecraft 16x16 block texture, anvil side view, dark iron with red vein patterns, ancient and heavy, pixel art style                                                      |

#### GUI 贴图（256×256，`gui/`）

| 文件名                         | 用途          | AI 提示词                                                                                                                                                                         |
| --------------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `critical_workshop_gui.png` | 临界工坊 GUI 背景 | Minecraft GUI background 256x256, dark stone frame with purple-gold rune border, 3x3 crafting grid slots, ancient workshop interface, pixel art style                          |
| `permeation_altar_gui.png`  | 渗透祭坛 GUI 背景 | Minecraft GUI background 256x256, dark stone altar with flowing purple energy veins, single item slot center with surrounding rune circle, infusion interface, pixel art style |
| `edict_anvil_gui.png`       | 诫命之砧 GUI 背景 | Minecraft GUI background 256x256, dark iron anvil interface, crimson rune accents, weapon slot + scroll slot + output slot, commandment inscription theme, pixel art style     |

---

## 贴图统计汇总

| 模组                   | 物品贴图 | 方块贴图 | GUI 贴图 | 合计     |
| -------------------- | ---- | ---- | ------ | ------ |
| 临界核心 (Critical Core) | 5    | 4    | 1      | **10** |
| 斩杀线 V2.0 (Kill_line) | 12   | 6    | 3      | **21** |
| **总计**               | 17   | 10   | 4      | **31** |

---

## 第三步：临界核心 Mod 实现计划

### 项目结构

```
CriticalCore/1.20.1/src/main/
├── java/com/kill_line/critical_core/
│   ├── CriticalCore.java                    # 入口类
│   ├── Constants.java                       # MOD_ID 等
│   ├── init/
│   │   ├── ModItems.java                    # 物品注册
│   │   ├── ModBlocks.java                   # 方块注册
│   │   ├── ModBlockEntities.java            # BE 注册
│   │   └── ModCreativeTabs.java             # 创造模式标签页
│   ├── block/
│   │   ├── ThresholdCrystalClusterBlock.java  # 阈界晶簇矿石
│   │   └── PulseForgeBlock.java              # 脉冲锻炉方块
│   ├── blockentity/
│   │   └── PulseForgeBlockEntity.java        # 脉冲锻炉 BE
│   ├── item/
│   │   └── CriticalCoreItem.java             # 临界核心（阈值感知被动）
│   ├── client/
│   │   ├── ClientSetup.java
│   │   ├── screen/
│   │   │   └── PulseForgeScreen.java         # 脉冲锻炉 GUI
│   │   └── menu/
│   │       └── PulseForgeMenu.java           # 脉冲锻炉容器
│   ├── worldgen/
│   │   └── ThresholdOrePlacement.java        # 阈界晶簇矿石生成
│   └── api/
│       └── CriticalCoreAPI.java              # 对外 API（供 Kill_line 调用）
├── resources/
│   ├── assets/critical_core/
│   │   ├── lang/en_us.json
│   │   ├── lang/zh_cn.json
│   │   ├── textures/                         # AI 生成的贴图
│   │   ├── models/
│   │   │   ├── item/                         # 物品模型 JSON
│   │   │   └── block/                        # 方块模型 JSON
│   │   └── blockstates/                      # 方块状态 JSON
│   ├── data/critical_core/
│   │   ├── recipes/                          # 合成配方
│   │   └── loot_tables/                      # 掉落表
│   └── META-INF/mods.toml
```

### 核心功能清单

#### 1. 矿物与材料

| 物品   | 注册名                         | 类型         | 说明                            |
| ---- | --------------------------- | ---------- | ----------------------------- |
| 阈界晶簇 | `threshold_crystal_cluster` | Block (矿石) | 生成于基岩层 Y=-64~-30，稀有，挖掘掉落阈界碎片  |
| 阈界碎片 | `threshold_shard`           | Item       | 矿石掉落物，用于脉冲锻炉合成临界核心            |
| 临界核心 | `critical_core`             | Item       | 核心产物，脉冲锻炉合成。持有时激活「阈值感知」被动     |
| 临界手札 | `critical_handbook`         | Item       | 引导道具，右键阅读，高亮提示后续步骤            |
| 虚空瓶  | `void_bottle`               | Item       | 玻璃瓶+末影珍珠合成，用于收集龙息残渣（V2.0 合成链） |
| 稳定核心 | `stable_core`               | Item       | 铁锭+红石+青金石合成，用于保险槽防止合成失败       |

#### 2. 脉冲锻炉 (Pulse Forge)

- **方块行为**：右键打开 GUI，1 输入槽 + 1 输出槽 + 1 燃料槽
- **唯一配方**：阈界碎片 ×N → 临界核心 ×1（消耗经验）
- **BlockEntity**：继承 `BaseContainerBlockEntity`，实现 `Ticker` 逻辑
- **GUI**：类似熔炉界面，但使用自定义纹理

#### 3. 阈值感知 (Threshold Perception)

- **实现方式**：`TickEvent.PlayerTickEvent` 监听，检测背包是否持有临界核心
- **效果**：持有时屏幕边缘脉冲光提示附近血量 <20% 的生物
- **客户端渲染**：`RenderGuiOverlayEvent` 叠加层

#### 4. 对外 API

```java
public class CriticalCoreAPI {
    /** Kill_line 调用：检查临界核心 mod 是否加载 */
    public static boolean isLoaded();

    /** 获取临界核心物品（供 Kill_line 用作合成材料） */
    public static Item getCriticalCoreItem();
}
```

### 矿石生成配置

- 最低高度: Y=-64, 最高高度: Y=-30
- 每区块矿脉数: 2 (普通) / 1 (困难) / 1 (极限)
- 每矿脉方块数: 4-6
- 需要深板岩作为基座

---

## 第四步：Kill_line V2.0 实现计划

### 新增项目结构（在现有 Kill_line 项目中添加）

```
Kill_line/1.20.1/src/main/java/com/kill_line/kill_line/
├── init/
│   ├── ModItems.java                        # 新增：V2.0 物品注册
│   ├── ModBlocks.java                       # 新增：V2.0 方块注册
│   ├── ModBlockEntities.java                # 新增：V2.0 BE 注册
│   └── ModMenuTypes.java                    # 新增：容器类型注册
├── block/
│   ├── CriticalWorkshopBlock.java           # 临界工坊
│   ├── PermeationAltarBlock.java            # 渗透祭坛
│   └── EdictAnvilBlock.java                 # 诫命之砧
├── blockentity/
│   ├── CriticalWorkshopBlockEntity.java
│   ├── PermeationAltarBlockEntity.java
│   └── EdictAnvilBlockEntity.java
├── item/                                    # 12 个新物品（简单 Item 子类）
├── client/
│   ├── screen/
│   │   ├── CriticalWorkshopScreen.java
│   │   ├── PermeationAltarScreen.java
│   │   └── EdictAnvilScreen.java
│   └── menu/
│       ├── CriticalWorkshopMenu.java
│       ├── PermeationAltarMenu.java
│       └── EdictAnvilMenu.java
├── recipe/
│   ├── CriticalWorkshopRecipe.java          # 自定义配方类型
│   ├── PermeationAltarRecipe.java           # 附魔渗透配方
│   └── EdictAnvilRecipe.java               # 诫命附魔配方
├── config/
│   └── KillLineConfig.java                  # Forge 配置（3 级难度）
└── integration/
    └── CriticalCoreIntegration.java         # 前置 mod 互操作
```

### 新增物品清单

| 注册名                   | 物品名  | 获取方式                 |
| --------------------- | ---- | -------------------- |
| `ancient_cornerstone` | 远古今石 | 远古城市共鸣石碑挖掘           |
| `void_frame`          | 虚空框架 | 末影之眼×2 + 黑曜石×4 工作台合成 |
| `blood_moon_core`     | 血月核心 | 泣血玫瑰 + 月影祭坛合成        |
| `silence_dust`        | 寂灭尘埃 | 虚空瓶收集龙息残渣 + 哭泣黑曜石    |
| `fate_thread`         | 命运丝线 | 蜘蛛网×2 + 金锭×1 工作台合成   |
| `ancient_page`        | 古神残页 | 林地府邸书架/废弃矿井陷阱箱/钓鱼    |
| `critical_essence`    | 临界精粹 | 临界工坊内用临界核心提纯         |
| `critical_scroll`     | 临界卷轴 | 渗透祭坛合成（有失败概率）        |
| `ancient_iron_block`  | 远古铁块 | 铁锭×4 + 远古碎片×1 烧炼     |
| `soul_spark`          | 灵魂火花 | 凋灵骷髅掉落               |
| `bleeding_rose`       | 泣血玫瑰 | 凋灵骷髅头 + 玫瑰合成         |
| `moon_shadow_altar`   | 月影祭坛 | 雪块×4 + 青金石×1 合成      |

### 新增方块

| 方块   | 注册名                 | GUI 类型     | 功能                      |
| ---- | ------------------- | ---------- | ----------------------- |
| 临界工坊 | `critical_workshop` | 3×3 合成网格   | 特殊合成：临界核心→临界精粹、武器雏形等    |
| 渗透祭坛 | `permeation_altar`  | 单物品槽+输出    | 将武器注入斩杀意志，合成临界卷轴（有失败风险） |
| 诫命之砧 | `edict_anvil`       | 武器槽+卷轴槽+输出 | 最终附魔：斩杀线附魔到武器上          |

### 合成链流程（简化版，基于 Qianwen 文档）

```
阶段1: 获取引导
  临界手札 → 右键阅读，解锁合成提示

阶段2: 合成临界工坊
  远古今石×4 + 虚空框架×1 + 血月核心×1
  → 在共鸣基座上合成（需夜晚）
  → 临界工坊方块

阶段3: 提纯临界精粹
  临界核心 + 寂灭尘埃×6 + 命运丝线×2 + 古神残页×1
  → 临界工坊内合成
  → 临界精粹

阶段4: 合成临界卷轴（渗透祭坛）
  临界精粹 + 古神残页×2 + 命运丝线×1
  → 渗透祭坛合成
  → 基础成功率 90%（配置可调）
  → 失败：祭坛破碎 + 材料 60%-90% 随机返还
  → 稳定核心可放入保险槽 = 100% 成功

阶段5: 铸造诫命之砧 + 附魔
  临界核心×1 + 远古铁块×2 + 普通铁砧
  → 临界工坊合成（消耗经验）
  → 诫命之砧方块

  武器 + 临界卷轴 + 灵魂火花×1
  → 诫命之砧附魔
  → 成功率 85%（配置可调）
  → 失败仅消耗卷轴，武器耐久保留 90%
```

### 3 级难度配置

```toml
# config/kill_line_v2.toml

[difficulty]
# 难度级别: NORMAL, HARD, EXTREME
level = "NORMAL"

[normal]
ore_per_chunk = 2
scroll_success_rate = 0.90
enchant_success_rate = 0.85
material_return_min = 0.60
material_return_max = 0.90
anvil_xp_cost = 100
stable_core_enabled = true
auto_stabilize_after = 2

[hard]
ore_per_chunk = 1
scroll_success_rate = 0.70
enchant_success_rate = 0.65
material_return_min = 0.40
material_return_max = 0.70
anvil_xp_cost = 500
stable_core_enabled = true
auto_stabilize_after = 3

[extreme]
ore_per_chunk = 0   # 仅限 BOSS 掉落和宝箱
scroll_success_rate = 0.50
enchant_success_rate = 0.40
material_return_min = 0.20
material_return_max = 0.50
anvil_xp_cost = 1000
stable_core_enabled = false  # 无保险
auto_stabilize_after = 5
```

### 前置 mod 互操作逻辑

```java
// Kill_line 中检测临界核心是否安装
public class CriticalCoreIntegration {
    private static final boolean CRITICAL_CORE_LOADED =
        ModList.get().isLoaded("critical_core");

    /** 如果临界核心已安装，斩杀线附魔不能通过附魔台获取 */
    public static boolean shouldBlockEnchantingTable() {
        return CRITICAL_CORE_LOADED;
    }

    /** 如果临界核心已安装，只能通过诫命之砧附魔 */
    public static boolean requiresSpecialAnvil() {
        return CRITICAL_CORE_LOADED;
    }
}
```

关键文件：`Kill_line/1.20.1/src/main/java/com/kill_line/kill_line/mixin/LivingEntityMixin.java`

- 需要修改 Mixin，在斩杀逻辑中检查是否安装了临界核心
- 安装时：只有通过诫命之砧附魔的武器才生效
- 未安装时：保持现有行为（附魔台/交易均可获取）

---

## 第五步：实现顺序

| 顺序  | 任务                                 | 依赖         |
| --- | ---------------------------------- | ---------- |
| 1   | 用户通过 IDE 创建 CriticalCore 项目        | 无          |
| 2   | 用户用 AI 生成 31 张贴图                   | 无          |
| 3   | 临界核心：基础物品注册（阈界碎片、临界核心等）            | 步骤 1       |
| 4   | 临界核心：阈界晶簇矿石 + 世界生成                 | 步骤 3       |
| 5   | 临界核心：脉冲锻炉方块 + BE + GUI             | 步骤 3       |
| 6   | 临界核心：阈值感知被动 + API                  | 步骤 3       |
| 7   | Kill_line：Forge 配置系统（3 级难度）        | 无          |
| 8   | Kill_line：12 个新物品注册                | 步骤 2       |
| 9   | Kill_line：临界工坊方块 + BE + GUI        | 步骤 8       |
| 10  | Kill_line：渗透祭坛方块 + BE + GUI + 失败机制 | 步骤 8       |
| 11  | Kill_line：诫命之砧方块 + BE + GUI        | 步骤 8       |
| 12  | Kill_line：合成链配方实现                  | 步骤 9,10,11 |
| 13  | Kill_line：前置 mod 互操作（拦截附魔台）        | 步骤 6,7     |
| 14  | 集成测试                               | 全部         |

---

## 验证方案

### 功能验证

1. **矿石生成**：创建新世界，传送到 Y=-50，确认阈界晶簇生成
2. **脉冲锻炉**：放入阈界碎片，确认产出临界核心
3. **阈值感知**：持有临界核心靠近残血生物，确认脉冲提示
4. **临界工坊**：确认所有合成配方正常工作
5. **渗透祭坛失败**：多次合成卷轴，确认失败时祭坛破碎 + 材料按比例返还
6. **稳定核心保险**：放入稳定核心，确认 100% 成功
7. **诫命之砧附魔**：确认斩杀线附魔成功
8. **附魔台拦截**：安装临界核心后，确认附魔台无法获得斩杀线

### 前置 mod 解耦验证

1. **仅安装 Kill_line**（不安临界核心）：行为与 V1.x 完全一致
2. **两个都安装**：必须走完整合成链才能获得斩杀线附魔
3. **卸载临界核心后**：已附魔的武器仍然有效，新武器无法通过附魔台获取斩杀线