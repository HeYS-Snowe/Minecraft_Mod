package com.kill_line.critical_core.blockentity;

import com.kill_line.critical_core.config.CriticalCoreConfig;
import com.kill_line.critical_core.init.ModBlockEntities;
import com.kill_line.critical_core.init.ModItems;
import com.kill_line.critical_core.init.ModMenuTypes;
import com.kill_line.critical_core.menu.PulseForgeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PulseForgeBlockEntity extends BaseContainerBlockEntity implements MenuProvider {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private int craftingProgress = 0;
    private int craftingTime = CriticalCoreConfig.PULSE_FORGE.craftingTimeTicks.get();
    private int shardsRequired = CriticalCoreConfig.PULSE_FORGE.shardsRequired.get();
    private int xpCost = CriticalCoreConfig.PULSE_FORGE.experienceCost.get();

    protected final ContainerData data;

    public PulseForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PULSE_FORGE_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> craftingProgress;
                    case 1 -> craftingTime;
                    case 2 -> shardsRequired;
                    case 3 -> xpCost;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> craftingProgress = value;
                    case 1 -> craftingTime = value;
                    case 2 -> shardsRequired = value;
                    case 3 -> xpCost = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        // Refresh config values on load
        craftingTime = CriticalCoreConfig.PULSE_FORGE.craftingTimeTicks.get();
        shardsRequired = CriticalCoreConfig.PULSE_FORGE.shardsRequired.get();
        xpCost = CriticalCoreConfig.PULSE_FORGE.experienceCost.get();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PulseForgeBlockEntity be) {
        if (be.hasRecipe()) {
            be.craftingProgress++;
            if (be.craftingProgress >= be.craftingTime) {
                be.craftItem();
                be.craftingProgress = 0;
            }
            be.setChanged();
        } else {
            be.craftingProgress = 0;
        }
    }

    private boolean hasRecipe() {
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (input.getCount() < shardsRequired) return false;
        if (!input.is(ModItems.THRESHOLD_SHARD.get())) return false;
        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        return output.is(ModItems.CRITICAL_CORE.get()) && output.getCount() < output.getMaxStackSize();
    }

    private void craftItem() {
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Consume shards
        input.shrink(shardsRequired);

        // Produce critical core
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.CRITICAL_CORE.get()));
        } else {
            output.grow(1);
        }

        // Consume experience from nearby players
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.phys.AABB searchBox = net.minecraft.world.phys.AABB.ofSize(
                    getBlockPos().getCenter(), 10.0, 10.0, 10.0);
            for (Player player : serverLevel.getEntitiesOfClass(Player.class, searchBox)) {
                if (player.experienceLevel >= xpCost) {
                    player.giveExperienceLevels(-xpCost);
                    ExperienceOrb.award(serverLevel, player.position(), xpCost * 10);
                    break;
                }
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
        return Component.translatable("container.critical_core.pulse_forge");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory) {
        return new PulseForgeMenu(ModMenuTypes.PULSE_FORGE_MENU.get(), containerId, inventory, this, this.data);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

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

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getContainerData() {
        return data;
    }
}
