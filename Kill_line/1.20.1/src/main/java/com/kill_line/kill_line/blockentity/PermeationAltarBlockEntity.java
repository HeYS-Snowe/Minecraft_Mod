package com.kill_line.kill_line.blockentity;

import com.kill_line.kill_line.config.KillLineConfig;
import com.kill_line.kill_line.init.ModBlockEntities;
import com.kill_line.kill_line.init.ModItems;
import com.kill_line.kill_line.init.ModMenuTypes;
import com.kill_line.kill_line.menu.PermeationAltarMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 渗透祭坛 BlockEntity
 * 合成临界卷轴 — 有失败概率
 * 失败时祭坛破碎，材料随机返还 60%-90%
 */
public class PermeationAltarBlockEntity extends BaseContainerBlockEntity {

    private static final int INPUT_SLOT_ESSENCE = 0;  // 临界精粹
    private static final int INPUT_SLOT_PAGE = 1;       // 古神残页
    private static final int INPUT_SLOT_THREAD = 2;     // 命运丝线
    private static final int INSURANCE_SLOT = 3;        // 稳定核心（保险）
    private static final int OUTPUT_SLOT = 4;
    private static final int SLOT_COUNT = 5;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int craftingProgress = 0;
    private static final int CRAFTING_TIME = 100; // 5 seconds
    private int consecutiveFailures = 0;

    protected final ContainerData data;

    public PermeationAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PERMEATION_ALTAR_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> craftingProgress;
                    case 1 -> consecutiveFailures;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> craftingProgress = value;
                    case 1 -> consecutiveFailures = value;
                }
            }

            @Override
            public int getCount() { return 2; }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PermeationAltarBlockEntity be) {
        if (be.hasRecipe()) {
            be.craftingProgress++;
            if (be.craftingProgress >= CRAFTING_TIME) {
                be.attemptCraft((ServerLevel) level, pos);
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
        ItemStack essence = itemHandler.getStackInSlot(INPUT_SLOT_ESSENCE);
        ItemStack page = itemHandler.getStackInSlot(INPUT_SLOT_PAGE);
        ItemStack thread = itemHandler.getStackInSlot(INPUT_SLOT_THREAD);

        if (essence.isEmpty() || page.getCount() < 2 || thread.isEmpty()) return false;
        if (!essence.is(ModItems.CRITICAL_ESSENCE.get())) return false;
        if (!page.is(ModItems.ANCIENT_PAGE.get())) return false;
        if (!thread.is(ModItems.FATE_THREAD.get())) return false;

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || (output.is(ModItems.CRITICAL_SCROLL.get()) && output.getCount() < output.getMaxStackSize());
    }

    private void attemptCraft(ServerLevel level, BlockPos pos) {
        // Check if stable core is in insurance slot
        ItemStack insurance = itemHandler.getStackInSlot(INSURANCE_SLOT);
        boolean hasInsurance = insurance.getItem().getDescriptionId().equals("item.critical_core.stable_core");

        // Check auto-stabilize
        int autoStabilize = KillLineConfig.SCROLL_CRAFTING.autoStabilizeAfter.get();
        boolean autoStabilized = autoStabilize > 0 && consecutiveFailures >= autoStabilize;

        // Determine success
        boolean success;
        if (hasInsurance) {
            success = true;
            // Don't consume insurance item
        } else if (autoStabilized) {
            success = level.random.nextFloat() < 0.98f;
        } else {
            float rate = KillLineConfig.SCROLL_CRAFTING.baseSuccessRate.get().floatValue();
            success = level.random.nextFloat() < rate;
        }

        if (success) {
            // Consume inputs
            itemHandler.getStackInSlot(INPUT_SLOT_ESSENCE).shrink(1);
            itemHandler.getStackInSlot(INPUT_SLOT_PAGE).shrink(2);
            itemHandler.getStackInSlot(INPUT_SLOT_THREAD).shrink(1);

            // Produce scroll
            ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.CRITICAL_SCROLL.get()));
            } else {
                output.grow(1);
            }
            consecutiveFailures = 0;
        } else {
            consecutiveFailures++;
            // Failure: break altar and scatter materials
            handleFailure(level, pos);
        }
    }

    private void handleFailure(ServerLevel level, BlockPos pos) {
        net.minecraft.util.RandomSource random = level.random;
        float returnMin = KillLineConfig.SCROLL_CRAFTING.materialReturnMin.get().floatValue();
        float returnMax = KillLineConfig.SCROLL_CRAFTING.materialReturnMax.get().floatValue();

        Vec3 dropPos = pos.getCenter();

        // Return materials with random retention
        for (int i = 0; i < 3; i++) { // Only input slots, not insurance
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                float retainRate = returnMin + random.nextFloat() * (returnMax - returnMin);
                int retainCount = Math.max(0, Math.round(stack.getCount() * retainRate));
                if (retainCount > 0) {
                    ItemStack returnStack = stack.copy();
                    returnStack.setCount(retainCount);
                    ItemEntity itemEntity = new ItemEntity(level, dropPos.x, dropPos.y + 0.5, dropPos.z, returnStack);
                    itemEntity.setExtendedLifetime(); // 30 seconds
                    level.addFreshEntity(itemEntity);
                }
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        // Keep insurance slot intact
        // Break the altar block
        level.destroyBlock(pos, false);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("craftingProgress", craftingProgress);
        tag.putInt("consecutiveFailures", consecutiveFailures);
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        craftingProgress = tag.getInt("craftingProgress");
        consecutiveFailures = tag.getInt("consecutiveFailures");
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.kill_line.permeation_altar");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new PermeationAltarMenu(ModMenuTypes.PERMEATION_ALTAR_MENU.get(), containerId, inventory, this, this.data);
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
