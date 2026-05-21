package com.kill_line.kill_line.blockentity;

import com.kill_line.kill_line.Constants;
import com.kill_line.kill_line.config.KillLineConfig;
import com.kill_line.kill_line.init.ModBlockEntities;
import com.kill_line.kill_line.init.ModItems;
import com.kill_line.kill_line.init.ModMenuTypes;
import com.kill_line.kill_line.menu.CriticalWorkshopMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * 临界工坊 BlockEntity
 * 3x3 合成网格风格的特殊工作台
 * 配方：临界核心 → 临界精粹，合成诫命之砧等
 */
public class CriticalWorkshopBlockEntity extends BaseContainerBlockEntity {

    private static final int GRID_SIZE = 9;
    private static final int OUTPUT_SLOT = 9;
    private static final int SLOT_COUNT = 10;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int craftingProgress = 0;
    private static final int CRAFTING_TIME = 80; // 4 seconds

    protected final ContainerData data;

    public CriticalWorkshopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRITICAL_WORKSHOP_BE.get(), pos, state);
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
            public int getCount() {
                return 1;
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CriticalWorkshopBlockEntity be) {
        if (be.hasRecipe()) {
            be.craftingProgress++;
            if (be.craftingProgress >= CRAFTING_TIME) {
                be.craftItem();
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
        // Check for critical essence recipe: 1 critical_core in center + 6 silence_dust + 2 fate_thread + 1 ancient_page
        ItemStack center = itemHandler.getStackInSlot(4); // center slot
        if (!center.is(ModItems.CRITICAL_ESSENCE.get()) && !center.isEmpty()) {
            // Check if this is the critical essence recipe
            if (!isCriticalEssenceRecipe()) return false;
        }
        return isCriticalEssenceRecipe() || isEdictAnvilRecipe() || isCriticalWorkshopRecipe();
    }

    private boolean isCriticalEssenceRecipe() {
        // Center: critical_core (from CriticalCore mod or this mod)
        // Surrounding: silence_dust ×6, fate_thread ×2, ancient_page ×1
        // Simplified: just check center slot has critical core
        ItemStack center = itemHandler.getStackInSlot(4);
        // Check for critical_core from CriticalCore mod
        if (center.isEmpty()) return false;
        if (!center.getItem().getDescriptionId().equals("item.critical_core.critical_core")) return false;

        // Check output slot is empty or compatible
        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || (output.is(ModItems.CRITICAL_ESSENCE.get()) && output.getCount() < output.getMaxStackSize());
    }

    private boolean isEdictAnvilRecipe() {
        // Recipe: critical_core + 2 ancient_iron_block + anvil item
        // Simplified: check specific slots
        return false; // Will be implemented with recipe system
    }

    private boolean isCriticalWorkshopRecipe() {
        return false; // Placeholder for additional recipes
    }

    private void craftItem() {
        if (isCriticalEssenceRecipe()) {
            // Consume center critical core
            itemHandler.getStackInSlot(4).shrink(1);
            // Produce critical essence
            ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.CRITICAL_ESSENCE.get()));
            } else {
                output.grow(1);
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
        return Component.translatable("container.kill_line.critical_workshop");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new CriticalWorkshopMenu(ModMenuTypes.CRITICAL_WORKSHOP_MENU.get(), containerId, inventory, this, this.data);
    }

    @Override
    public int getContainerSize() { return SLOT_COUNT; }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) { return itemHandler.getStackInSlot(slot); }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = itemHandler.extractItem(slot, amount, false);
        setChanged();
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(getBlockPos()) != this) return false;
        return player.distanceToSqr(getBlockPos().getCenter()) <= 64.0;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public ItemStackHandler getItemHandler() { return itemHandler; }
    public ContainerData getContainerData() { return data; }
}
