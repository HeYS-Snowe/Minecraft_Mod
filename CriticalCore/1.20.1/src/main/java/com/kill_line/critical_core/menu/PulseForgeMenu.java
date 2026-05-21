package com.kill_line.critical_core.menu;

import com.kill_line.critical_core.blockentity.PulseForgeBlockEntity;
import com.kill_line.critical_core.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class PulseForgeMenu extends AbstractContainerMenu {

    private final PulseForgeBlockEntity blockEntity;
    private final ContainerData data;

    public PulseForgeMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(ModMenuTypes.PULSE_FORGE_MENU.get(), containerId, inventory,
                getBlockEntity(inventory, extraData), getContainerData(inventory, extraData));
    }

    public PulseForgeMenu(MenuType<?> menuType, int containerId, Inventory inventory,
                          PulseForgeBlockEntity blockEntity, ContainerData data) {
        super(menuType, containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        // Input slot
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 56, 35));

        // Output slot
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 116, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    private static PulseForgeBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf data) {
        BlockEntity be = inventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof PulseForgeBlockEntity pulseForge) {
            return pulseForge;
        }
        throw new IllegalStateException("Block entity is not a PulseForgeBlockEntity");
    }

    private static ContainerData getContainerData(Inventory inventory, FriendlyByteBuf data) {
        BlockEntity be = inventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof PulseForgeBlockEntity pulseForge) {
            return pulseForge.getContainerData();
        }
        throw new IllegalStateException("Block entity is not a PulseForgeBlockEntity");
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            result = slotItem.copy();

            if (index < 2) {
                // Move from block inventory to player inventory
                if (!this.moveItemStackTo(slotItem, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to input slot
                if (!this.moveItemStackTo(slotItem, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.blockEntity.stillValid(player);
    }

    public int getCraftingProgress() {
        return data.get(0);
    }

    public int getCraftingTime() {
        return data.get(1);
    }

    public int getShardsRequired() {
        return data.get(2);
    }

    public int getXpCost() {
        return data.get(3);
    }

    public float getCraftingProgressScaled() {
        int progress = getCraftingProgress();
        int time = getCraftingTime();
        return time > 0 ? (float) progress / time : 0.0f;
    }
}
