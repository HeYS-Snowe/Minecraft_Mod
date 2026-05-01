# Minecraft Mod 开发综合指南

> 面向新手的 Minecraft 模组开发参考手册，覆盖 Fabric / Forge / NeoForge 三个加载器。

---

## 一、开发环境搭建

### 1.1 必备工具

| 工具                       | 版本要求                               | 用途            | 下载                                 |
| ------------------------ | ---------------------------------- | ------------- | ---------------------------------- |
| JDK                      | 21（MC 1.20.5+）/ 17（MC 1.18-1.20.4） | 编译 Java 代码    | https://adoptium.net/              |
| IntelliJ IDEA            | Community 或 Ultimate               | 主力 IDE        | https://www.jetbrains.com/idea/    |
| Minecraft Development 插件 | 最新                                 | IDE 内 MC 开发辅助 | IDE 插件市场搜索 "Minecraft Development" |

### 1.2 推荐工具

| 工具              | 用途         | 下载                          |
| --------------- | ---------- | --------------------------- |
| Blockbench      | 自定义模型、纹理制作 | https://www.blockbench.net/ |
| Minecraft 官方启动器 | 测试运行       | https://www.minecraft.net/  |

### 1.3 JDK 版本对照表

| Minecraft 版本    | JDK 版本 | 说明         |
| --------------- | ------ | ---------- |
| 1.16.5          | 8 或 11 | 最低 8，推荐 11 |
| 1.17.x          | 16     | 强制要求       |
| 1.18.x - 1.20.4 | 17     | 强制要求       |
| 1.20.5+         | 21     | 强制要求       |

### 1.4 创建项目

**核心流程**: 通过 IDE 的 Minecraft Development 插件创建项目，AI 提供配置信息。

#### 标准流程

1. 确定需求：mod 名称、MC 版本、加载器类型
2. 向 AI 索取 IDE 插件所需的配置信息
3. 在 IntelliJ IDEA 中：File > New > Project... > 选择 "Minecraft" 模板
4. 填写 AI 提供的配置信息
5. 项目创建完成后，将项目放置到 `{mod名称}/{MC版本}/` 目录
6. 通知 AI 项目已创建，开始协作开发

#### AI 提供的配置信息示例

| 配置项            | 示例值 (Kill_line / Fabric / 1.21.4) |
| -------------- | --------------------------------- |
| Mod ID         | `kill_line`                       |
| Mod Name       | `Kill Line`                       |
| Package        | `com.kill_line`                   |
| MC Version     | `1.21.4`                          |
| Loader         | `Fabric`                          |
| Java Version   | `21`                              |
| Mappings       | `Yarn`                            |
| Loader Version | `0.16.9`                          |
| Fabric API     | `0.114.0+1.21.4`                  |

#### 注意事项

- **NEVER** 让 AI 手动创建项目基础结构（build.gradle、settings.gradle 等），必须通过 IDE 插件创建
- IDE 插件创建的项目会自动生成正确的运行配置（Minecraft Client、Minecraft Server 等）
- 手动创建的项目容易缺少正确的运行配置，导致锁文件等各种问题

### 1.5 IDEA 首次配置清单

- [ ] File > Project Structure > Project SDK 设置为正确的 JDK 版本
- [ ] 安装 Minecraft Development 插件
- [ ] 等待 Gradle 同步完成（右下角进度条）
- [ ] 确认 Run Configuration 中有 runClient 和 runServer

---

## 二、项目结构说明

### 2.1 本项目多层级结构

```
Minecraft_Mod/
├── CLAUDE.md                        # 项目 AI 规则
├── doc/                             # 全局开发文档
│   ├── development-guide.md         # 本文件
│   └── project-structure.md         # 项目结构说明
│
└── {mod名称}/                       # 例如: MoreTools
    ├── doc/                         # 该 mod 的开发文档
    │   ├── dependencies.md          # 依赖版本记录
    │   └── requirements.md          # 功能需求文档
    ├── build/                       # 编译产物
    │   ├── current/                 # 当前最新版本
    │   └── archive/                 # 历史版本存档
    │
    ├── fabric/                      # Fabric 版本
    │   └── 1.21.4/
    │       ├── src/main/java/
    │       ├── src/main/resources/
    │       ├── build.gradle
    │       ├── settings.gradle
    │       └── gradle.properties
    │
    ├── forge/                       # Forge 版本
    │   └── 1.21.4/
    │       ├── src/main/java/
    │       ├── src/main/resources/
    │       └── build.gradle
    │
    └── neoforge/                    # NeoForge 版本
        └── 1.21.4/
            ├── src/main/java/
            ├── src/main/resources/
            └── build.gradle
```

### 2.2 单个 mod 项目内部结构

```
{loader}/{mc_version}/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/modid/         # 你的包路径
│       │       ├── ModIdMod.java           # 主入口类
│       │       ├── item/                   # 物品类
│       │       │   └── MyItem.java
│       │       ├── block/                  # 方块类
│       │       │   └── MyBlock.java
│       │       ├── entity/                 # 实体类（如需要）
│       │       ├── recipe/                 # 自定义合成配方
│       │       ├── mixin/                  # Mixin 类
│       │       ├── event/                  # 事件处理类
│       │       ├── config/                 # 配置类
│       │       └── util/                   # 工具类
│       │
│       └── resources/
│           ├── fabric.mod.json             # [Fabric] Mod 元数据
│           ├── META-INF/
│           │   ├── mods.toml               # [Forge] Mod 元数据
│           │   └── neoforge.mods.toml      # [NeoForge] Mod 元数据
│           ├── {modid}.mixins.json         # Mixin 配置
│           ├── pack.mcmeta                 # 资源包元数据
│           │
│           ├── assets/{modid}/             # 客户端资源
│           │   ├── lang/                   # 语言文件
│           │   │   ├── en_us.json
│           │   │   └── zh_cn.json
│           │   ├── models/
│           │   │   ├── item/               # 物品模型
│           │   │   └── block/              # 方块模型
│           │   ├── textures/
│           │   │   ├── item/               # 物品纹理 (.png)
│           │   │   ├── block/              # 方块纹理 (.png)
│           │   │   ├── entity/             # 实体纹理
│           │   │   └── gui/                # GUI 纹理
│           │   ├── sounds/                 # 音效文件
│           │   └── sounds.json             # 音效注册
│           │
│           └── data/{modid}/               # 服务端数据
│               ├── recipes/                # 合成配方
│               ├── loot_tables/            # 战利品表
│               │   ├── blocks/
│               │   └── entities/
│               ├── tags/                   # 标签
│               │   ├── items/
│               │   └── blocks/
│               ├── advancements/           # 进度/成就
│               └── predicates/             # 战利品条件
│
├── gradle/
│   └── wrapper/                            # Gradle Wrapper
├── build.gradle                            # 构建配置
├── settings.gradle                         # 项目设置
├── gradle.properties                       # 版本变量
├── gradlew                                 # Linux/Mac 脚本
└── gradlew.bat                             # Windows 脚本
```

---

## 三、核心概念

### 3.1 Mod ID 命名规则

```
规则：仅使用小写字母 (a-z)、数字 (0-9)、下划线 (_)
长度：2-64 个字符
NEVER 使用大写字母、空格、连字符
```

示例：

- `more_tools` -- 正确
- `MoreTools` -- 错误（大写）
- `more-tools` -- 错误（连字符）

### 3.2 主入口类

#### Fabric

```java
package com.example.moretools;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreToolsMod implements ModInitializer {
    public static final String MOD_ID = "more_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("More Tools mod initialized!");
        // 在这里注册物品、方块等
    }
}
```

入口类在 `fabric.mod.json` 中声明：

```json
{
  "schemaVersion": 1,
  "id": "more_tools",
  "version": "${version}",
  "name": "More Tools",
  "entrypoints": {
    "main": ["com.example.moretools.MoreToolsMod"]
  },
  "depends": {
    "fabricloader": ">=0.15.0",
    "minecraft": "~1.21.4",
    "java": ">=21"
  }
}
```

#### Forge

```java
package com.example.moretools;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MoreToolsMod.MOD_ID)
public class MoreToolsMod {
    public static final String MOD_ID = "more_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MoreToolsMod() {
        LOGGER.info("More Tools mod initialized!");
        // 注册物品、方块等
    }
}
```

入口类通过 `@Mod` 注解自动发现，元数据在 `META-INF/mods.toml` 中。

#### NeoForge

```java
package com.example.moretools;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MoreToolsMod.MOD_ID)
public class MoreToolsMod {
    public static final String MOD_ID = "more_tools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MoreToolsMod() {
        LOGGER.info("More Tools mod initialized!");
    }
}
```

元数据在 `META-INF/neoforge.mods.toml` 中，格式与 Forge 类似但有差异。

---

## 四、注册系统

### 4.1 注册物品

#### Fabric

```java
// 在 ModInitializer.onInitialize() 中或静态初始化块中
public static final Item RUBY = Registry.register(
    Registries.ITEM,
    Identifier.of(MOD_ID, "ruby"),
    new Item(new Item.Settings())
);

public static final Item RUBY_SWORD = Registry.register(
    Registries.ITEM,
    Identifier.of(MOD_ID, "ruby_sword"),
    new SwordItem(RubyToolMaterial.INSTANCE, new Item.Settings())
);
```

#### Forge / NeoForge

```java
// 创建注册类
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);

    public static final RegistryObject<Item> RUBY =          // Forge
    public static final DeferredHolder<Item, Item> RUBY =    // NeoForge
        ITEMS.register("ruby", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RUBY_SWORD =    // Forge
    public static final DeferredHolder<Item, Item> RUBY_SWORD = // NeoForge
        ITEMS.register("ruby_sword", () -> new SwordItem(
            RubyToolMaterial.INSTANCE,
            new Item.Properties()
                .attributes(SwordItem.createAttributes(RubyToolMaterial.INSTANCE, 3, -2.4f))
        ));
}

// 在主类构造函数中注册
ModItems.ITEMS.register(bus);  // bus = FMLJavaModLoadingContext.get().getModEventBus()
```

### 4.2 注册方块

#### Fabric

```java
public static final Block RUBY_BLOCK = Registry.register(
    Registries.BLOCK,
    Identifier.of(MOD_ID, "ruby_block"),
    new Block(AbstractBlock.Settings.create().strength(3.0f, 3.0f))
);

// 方块必须同时注册对应的 BlockItem 才能拿在手中
public static final Item RUBY_BLOCK_ITEM = Registry.register(
    Registries.ITEM,
    Identifier.of(MOD_ID, "ruby_block"),
    new BlockItem(RUBY_BLOCK, new Item.Settings())
);
```

#### Forge / NeoForge

```java
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(BuiltInRegistries.BLOCK, MOD_ID);

    public static final RegistryObject<Block> RUBY_BLOCK =
        BLOCKS.register("ruby_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .strength(3.0f, 3.0f)
        ));

    // 方块物品自动注册（NeoForge 示例）
    public static final DeferredHolder<Item, Item> RUBY_BLOCK_ITEM =
        ModItems.ITEMS.register("ruby_block",
            () -> new BlockItem(RUBY_BLOCK.get(), new Item.Properties()));
}
```

### 4.3 工具材质（Tool Material）

```java
// 1.21.x 使用 ToolMaterial record
public record RubyToolMaterial(
    TagKey<Item> inverseTag,
    int itemDurability,
    float miningSpeed,
    float attackDamage,
    int enchantability,
    Supplier<Ingredient> repairIngredient
) implements ToolMaterial {
    // Fabric 中可直接实现 ToolMaterial 接口
    // Forge/NeoForge 中实现 Tier 接口

    public static final RubyToolMaterial INSTANCE = new RubyToolMaterial(
        // inverseTag (1.21+)
        ItemTags.INCORRECT_FOR_WOODEN_TOOL,
        2500,            // 耐久度
        10.0f,           // 挖掘速度
        4.0f,            // 攻击伤害
        22,              // 附魔等级
        () -> Ingredient.ofItems(ModItems.RUBY)  // 修复材料
    );
}
```

---

## 五、资源文件

### 5.1 语言文件

`assets/{modid}/lang/zh_cn.json`：

```json
{
  "item.more_tools.ruby": "红宝石",
  "item.more_tools.ruby_sword": "红宝石剑",
  "block.more_tools.ruby_block": "红宝石块",
  "itemGroup.more_tools": "更多工具"
}
```

`assets/{modid}/lang/en_us.json`：

```json
{
  "item.more_tools.ruby": "Ruby",
  "item.more_tools.ruby_sword": "Ruby Sword",
  "block.more_tools.ruby_block": "Ruby Block",
  "itemGroup.more_tools": "More Tools"
}
```

### 5.2 物品模型

`assets/{modid}/models/item/ruby.json`（简单物品）：

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "more_tools:item/ruby"
  }
}
```

手持工具（剑、镐等）使用 `"parent": "minecraft:item/handheld"`。

### 5.3 方块模型

`assets/{modid}/models/block/ruby_block.json`：

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "more_tools:block/ruby_block"
  }
}
```

`assets/{modid}/models/item/ruby_block.json`（方块对应的物品模型）：

```json
{
  "parent": "more_tools:block/ruby_block"
}
```

### 5.4 方块状态

`assets/{modid}/blockstates/ruby_block.json`：

```json
{
  "variants": {
    "": { "model": "more_tools:block/ruby_block" }
  }
}
```

### 5.5 纹理要求

| 类型     | 尺寸        | 格式  |
| ------ | --------- | --- |
| 物品纹理   | 16x16（推荐） | PNG |
| 方块纹理   | 16x16（推荐） | PNG |
| 模型纹理   | 可变（3D 模型） | PNG |
| GUI 纹理 | 按需        | PNG |

纹理文件放在 `assets/{modid}/textures/item/` 或 `textures/block/` 下，文件名与模型 JSON 中的路径一致。

### 5.6 合成配方

`data/{modid}/recipes/ruby_sword.json`（有序合成）：

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    " R ",
    " R ",
    " S "
  ],
  "key": {
    "R": { "item": "more_tools:ruby" },
    "S": { "item": "minecraft:stick" }
  },
  "result": {
    "id": "more_tools:ruby_sword",
    "count": 1
  }
}
```

### 5.7 战利品表

`data/{modid}/loot_tables/blocks/ruby_block.json`：

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "more_tools:ruby_block"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}
```

### 5.8 标签

`data/{modid}/tags/items/ruby_tools.json`：

```json
{
  "values": [
    "more_tools:ruby_sword",
    "more_tools:ruby_pickaxe",
    "more_tools:ruby_axe",
    "more_tools:ruby_shovel",
    "more_tools:ruby_hoe"
  ]
}
```

---

## 六、Mixin 系统

Mixin 是 Minecraft Modding 的核心技术，允许在不直接修改 Minecraft 源码的情况下注入代码。

### 6.1 Mixin 配置文件

`resources/{modid}.mixins.json`：

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.example.moretools.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "ServerPlayerMixin"
  ],
  "client": [
    "ClientPlayerMixin"
  ],
  "server": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### 6.2 Mixin 类示例

```java
package com.example.moretools.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    // 在方法返回前注入代码
    @Inject(method = "eatFood", at = @At("RETURN"))
    private void onEatFood(CallbackInfoReturnable<ItemStack> cir) {
        // 在玩家吃完食物后执行自定义逻辑
    }
}
```

### 6.3 常用 Mixin 注解

| 注解                   | 用途          | 说明          |
| -------------------- | ----------- | ----------- |
| `@Inject`            | 在方法指定位置注入代码 | 最常用         |
| `@Redirect`          | 重定向方法调用     | 修改方法参数或替换调用 |
| `@Overwrite`         | 完全替换方法      | 慎用，兼容性差     |
| `@ModifyArg`         | 修改方法参数      | 精细控制        |
| `@ModifyReturnValue` | 修改方法返回值     | 1.21+ 新增    |
| `@Accessor`          | 访问私有字段      | 替代反射        |
| `@Invoker`           | 调用私有方法      | 替代反射        |

### 6.4 @Inject 的 @At 定位器

| 定位器             | 含义      |
| --------------- | ------- |
| `@At("HEAD")`   | 方法开头    |
| `@At("RETURN")` | 方法返回前   |
| `@At("TAIL")`   | 方法末尾    |
| `@At("INVOKE")` | 调用指定方法时 |
| `@At("FIELD")`  | 访问指定字段时 |

---

## 七、事件系统

### 7.1 Fabric 事件

Fabric 使用回调接口（Callback）模式：

```java
// 使用 Fabric API 提供的事件
ServerLifecycleEvents.SERVER_STARTING.register(server -> {
    LOGGER.info("Server starting!");
});

// 自定义事件（使用 Event 回调）
public interface PlayerTickCallback {
    Event<PlayerTickCallback> EVENT = Event.create(
        PlayerTickCallback.class,
        (listeners) -> (player) -> {
            for (PlayerTickCallback listener : listeners) {
                listener.onTick(player);
            }
        }
    );
    void onTick(PlayerEntity player);
}
```

### 7.2 Forge 事件

Forge 使用 `@SubscribeEvent` 注解：

```java
@Mod(MOD_ID)
public class MoreToolsMod {
    public MoreToolsMod(IEventBus bus) {
        bus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup!");
    }
}

// 分离的事件处理类
@SubscribeEvent
public static void onBlockBreak(BlockEvent.BreakEvent event) {
    // 玩家破坏方块时触发
}
```

### 7.3 NeoForge 事件

NeoForge 与 Forge 类似，但使用不同的 Event 包路径：

```java
import net.neoforged.neoforge.event.level.BlockEvent;

@SubscribeEvent
public static void onBlockBreak(BlockEvent.BreakEvent event) {
    // 玩家破坏方块时触发
}
```

---

## 八、自定义创造模式标签页（Creative Tab）

### Fabric

```java
Registry.register(
    Registries.ITEM_GROUP,
    Identifier.of(MOD_ID, "more_tools"),
    FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModItems.RUBY))
        .displayName(Text.translatable("itemGroup.more_tools"))
        .entries((displayContext, entries) -> {
            entries.add(ModItems.RUBY);
            entries.add(ModItems.RUBY_SWORD);
            entries.add(ModBlocks.RUBY_BLOCK);
        })
        .build()
);
```

### Forge / NeoForge

```java
public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
    DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

public static final RegistryObject<CreativeModeTab> MORE_TOOLS_TAB =
    CREATIVE_TABS.register("more_tools", () -> CreativeModeTab.builder()
        .icon(() -> new ItemStack(ModItems.RUBY.get()))
        .title(Component.translatable("itemGroup.more_tools"))
        .displayItems((parameters, output) -> {
            output.accept(ModItems.RUBY.get());
            output.accept(ModItems.RUBY_SWORD.get());
            output.accept(ModBlocks.RUBY_BLOCK.get());
        })
        .build()
    );
```

---

## 九、数据生成（Data Generation）

数据生成器可以自动生成 JSON 文件（模型、配方、标签等），避免手动编写。

### Fabric

```java
// 实现 DataGeneratorEntrypoint
public class MoreToolsDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addModelProvider(ModModelProvider::new);
        pack.addRecipeProvider(ModRecipeProvider::new);
        pack.addTagProvider(ModTagProvider::new);
    }
}
```

需要在 `fabric.mod.json` 中注册：

```json
"entrypoints": {
    "fabric-datagen": ["com.example.moretools.MoreToolsDataGenerator"]
}
```

运行：`gradlew runDatagen`

### Forge / NeoForge

```java
@Mod(MOD_ID)
public class MoreToolsMod {
    public MoreToolsMod(IEventBus bus) {
        bus.addListener(this::gatherData);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        // 注册各种 provider
    }
}
```

运行：`gradlew runData`

---

## 十、调试技巧

### 10.1 日志

```java
// 使用 SLF4J Logger
private static final Logger LOGGER = LoggerFactory.getLogger("more_tools");

LOGGER.info("信息日志");
LOGGER.warn("警告日志");
LOGGER.error("错误日志");
LOGGER.debug("调试日志");  // 仅在开发环境可见
```

### 10.2 运行调试

```bash
# 运行客户端
gradlew runClient

# 运行服务端
gradlew runServer

# 生成数据
gradlew runDatagen   # Fabric
gradlew runData      # Forge/NeoForge
```

### 10.3 常用调试方法

1. **断点调试**：IDEA 中直接在代码行打断点，以 Debug 模式运行 runClient
2. **F3 调试屏幕**：游戏中按 F3 查看 blockstate、坐标等信息
3. **`/reload` 命令**：修改资源文件后可在游戏中热重载（仅资源，不含代码）
4. **日志排查**：查看 `logs/latest.log` 获取详细错误信息

### 10.4 常见构建错误排查

| 错误                       | 原因              | 解决                              |
| ------------------------ | --------------- | ------------------------------- |
| `Could not resolve`      | 依赖版本不对或仓库无法访问   | 检查 gradle.properties 中的版本号      |
| `Mixin apply error`      | Mixin 目标方法签名不匹配 | 使用 `gradlew genSources` 后检查目标方法 |
| `Missing texture`        | 纹理文件路径不匹配       | 检查模型 JSON 中的纹理路径                |
| `Syntax error in JSON`   | 资源文件 JSON 格式错误  | 使用 JSON 校验工具检查                  |
| `ClassNotFoundException` | 映射名错误           | 确认使用正确的映射（Mojmap / Yarn）        |

---

## 十一、发布与打包

### 11.1 构建

```bash
# 构建 release 版本
gradlew build

# 构建产物位置
# Fabric:  build/libs/{modid}-{version}.jar
# Forge:   build/libs/{modid}-{version}.jar
# NeoForge: build/libs/{modid}-{version}.jar
```

### 11.2 产物命名规范

**命名格式**：

```
# 普通命名（无前置 mod / 前置 mod 本身）
{mod名}-{版本号}-{加载器}-{MC版本}-{YYMMDD}.jar

# 有前置 mod
{mod名}-{版本号}+{前置mod名}-{前置版本}-{加载器}-{MC版本}-{YYMMDD}.jar
```

**字段说明**：

| 字目   | 说明                                                | 示例                           |
| ---- | ------------------------------------------------- | ---------------------------- |
| mod名 | Mod 名称                                            | Kill_line, KillLineAnimation |
| 版本号  | `{MAJOR}.{MINOR}.{PATCH}.{BUILD}[.fix{N}]`，每次构建更新 | 1.0.0.1, 1.0.1.2.fix1        |
| 加载器  | fabric / forge / neoforge                         | fabric                       |
| MC版本 | 目标 Minecraft 版本                                   | 1.21.4                       |
| 日期   | YYMMDD（6 位）                                       | 260421                       |
| 前置版本 | 前置 mod 的精确版本号                                     | 1.0.0.1                      |

**版本号语义**：每次构建都更新版本号

```
{MAJOR}.{MINOR}.{PATCH}.{BUILD}[.fix{N}]

MAJOR: 不兼容的大版本更新（重构、新 MC 版本适配）
MINOR: 功能/质量更新（新增物品、方块、机制）
PATCH: Bug 修复
BUILD: 构建序号，每次构建递增
fix{N}: 紧急修复（仅紧急修复时有此段，N 为修复次数）
```

**示例**：

```
# 前置 mod（普通命名）
KillLineAnimation-1.0.0.1-fabric-1.21.4-260421.jar
KillLineAnimation-1.0.1.2.fix1-fabric-1.21.4-260422.jar

# 依赖 KillLineAnimation 的 mod
Kill_line-1.0.0.1+KillLineAnimation-1.0.0.1-fabric-1.21.4-260421.jar

# 多个前置 mod（链式 +）
SomeMod-1.0.0.1+ModA-1.0.0.1+ModB-2.0.0.3-neoforge-1.21.4-260501.jar
```

### 11.3 构建后处理

1. 将构建产物从 `build/libs/` 复制到 `build/current/`
2. 重命名为规范格式
3. 将 current/ 中的旧版本移入 archive/
4. 更新 doc/dependencies.md 中的版本信息

### 11.4 发布平台

| 平台         | 地址                                           | 说明            |
| ---------- | -------------------------------------------- | ------------- |
| CurseForge | https://www.curseforge.com/minecraft/mc-mods | 最大的 MC mod 平台 |
| Modrinth   | https://modrinth.com/mods                    | 新兴平台，界面友好     |

---

## 十二、学习资源

### 官方文档

- Fabric: https://fabricmc.net/wiki/
- Forge: https://docs.minecraftforge.net/
- NeoForge: https://docs.neoforged.net/
- Minecraft Wiki (技术): https://minecraft.wiki/

### 社区资源

- Fabric Discord: https://discord.gg/v6v4pMv
- Forge Discord: 通过 Forge 论坛获取
- NeoForge Discord: 通过 NeoForge 论坛获取
- r/fabricmc / r/feedthebeast (Reddit)

### 推荐学习路径

1. 先从 Fabric 开始（入门门槛最低）
2. 学会注册简单的物品和方块
3. 学习资源文件（模型、纹理、语言文件）
4. 学习合成配方和战利品表
5. 尝试 Mixin 注入
6. 尝试 Forge/NeoForge 版本移植
7. 学习数据生成（自动生成 JSON）
8. 尝试更复杂的功能（自定义实体、维度等）
