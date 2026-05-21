package com.kill_line.kill_line.blockentity;

import com.kill_line.kill_line.config.KillLineConfig;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.init.ModBlockEntities;
import com.kill_line.kill_line.init.ModItems;
import com.kill_line.kill_line.init.ModMenuTypes;
import com.kill_line.kill_line.menu.EdictAnvilMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 诫命之砧 BlockEntity
 * 最终附魔：武器 + 临界卷轴 + 灵魂火花 → 斩杀线附魔
 * 成功率可配置，失败仅消耗卷轴
 */
public class EdictAnvilBlockEntity extends BaseContainerBlockEntity {

    private static final int WEAPON_SLOT = 0;
    private static final int SCROLL_SLOT = 1;
    private static final int SPARK_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;
    private static final int SLOT_COUNT = 4;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int craftingProgress = 0;
    private static final int CRAFTING_TIME = 120; // 6 seconds

    protected final ContainerData data;

    public EdictAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EDICT_ANVIL_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? craftingProgress : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) craftingProgress = value;
            }

            @Override
            public int getCount() { return 1; }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EdictAnvilBlockEntity be) {
        if (be.hasRecipe()) {
            be.craftingProgress++;
            if (be.craftingProgress >= CRAFTING_TIME) {
                be.attemptEnchant((ServerLevel) level);
                be.craftingProgress = 0;
            }
            be.setChanged();
        } else {
            if (be.craftingProgress > 0) {
                be.craftingProgress = 0;
                be.setChanged();
            }
        }
    }

    private boolean hasRecipe() {
        ItemStack weapon = itemHandler.getStackInSlot(WEAPON_SLOT);
        ItemStack scroll = itemHandler.getStackInSlot(SCROLL_SLOT);
        ItemStack spark = itemHandler.getStackInSlot(SPARK_SLOT);
        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Weapon must be a sword/axe type and not already have kill_line
        if (weapon.isEmpty() || !isWeapon(weapon)) return false;
        if (EnchantmentHelper.getEnchantments(weapon).containsKey(ModEnchantments.KILL_LINE.get())) return false;

        // Must have scroll and spark
        if (!scroll.is(ModItems.CRITICAL_SCROLL.get())) return false;
        if (!spark.is(ModItems.SOUL_SPARK.get())) return false;

        return output.isEmpty();
    }

    private boolean isWeapon(ItemStack stack) {
        // Check if item can be enchanted (weapons and tools)
        return stack.getItem().getDefaultInstance().getEnchantmentValue() > 0;
    }

    private void attemptEnchant(ServerLevel level) {
        float rate = KillLineConfig.ENCHANTING.baseSuccessRate.get().floatValue();
        boolean success = level.random.nextFloat() < rate;

        ItemStack weapon = itemHandler.getStackInSlot(WEAPON_SLOT);
        ItemStack scroll = itemHandler.getStackInSlot(SCROLL_SLOT);
        ItemStack spark = itemHandler.getStackInSlot(SPARK_SLOT);

        if (success) {
            // Apply Kill Line enchantment
            ItemStack enchantedWeapon = weapon.copy();
            enchantedWeapon.enchant(ModEnchantments.KILL_LINE.get(), 1);
            itemHandler.setStackInSlot(OUTPUT_SLOT, enchantedWeapon);

            // Consume all inputs
            itemHandler.setStackInSlot(WEAPON_SLOT, ItemStack.EMPTY);
            scroll.shrink(1);
            spark.shrink(1);
        } else {
            // Failure: only consume scroll, weapon retains durability
            scroll.shrink(1);
            spark.shrink(1);

            float retention = KillLineConfig.ENCHANTING.weaponDurabilityRetentionOnFail.get().floatValue();
            if (retention < 1.0f && weapon.isDamageableItem()) {
                int newDamage = weapon.getMaxDamage() - (int) ((weapon.getMaxDamage() - weapon.getDamageValue()) * retention);
                weapon.setDamageValue(newDamage);
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("craftingProgress", craftingProgress);
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        craftingProgress = tag.getInt("craftingProgress");
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.kill_line.edict_anvil");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new EdictAnvilMenu(ModMenuTypes.EDICT_ANVIL_MENU.get(), containerId, inventory, this, this.data);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() {
        for (int i = 0; i < SLOT_COUNT; i++) if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        return true;
    }
    @Override public @NotNull ItemStack getItem(int slot) { return itemHandler.getStackInSlot(slot); }
    @Override public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack r = itemHandler.extractItem(slot, amount, false); setChanged(); return r;
    }
    @Override public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack s = itemHandler.getStackInSlot(slot); itemHandler.setStackInSlot(slot, ItemStack.EMPTY); return s;
    }
    @Override public void setItem(int slot, @NotNull ItemStack stack) { itemHandler.setStackInSlot(slot, stack); setChanged(); }
    @Override public boolean stillValid(@NotNull Player player) {
        if (level == null || level.getBlockEntity(getBlockPos()) != this) return false;
        return player.distanceToSqr(getBlockPos().getCenter()) <= 64.0;
    }
    @Override public void clearContent() { for (int i = 0; i < SLOT_COUNT; i++) itemHandler.setStackInSlot(i, ItemStack.EMPTY); }

    public ItemStackHandler getItemHandler() { return itemHandler; }
    public ContainerData getContainerData() { return data; }
}
