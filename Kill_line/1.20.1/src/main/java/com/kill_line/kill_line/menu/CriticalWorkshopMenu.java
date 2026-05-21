package com.kill_line.kill_line.menu;

import com.kill_line.kill_line.blockentity.CriticalWorkshopBlockEntity;
import com.kill_line.kill_line.init.ModMenuTypes;
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

public class CriticalWorkshopMenu extends AbstractContainerMenu {

    private final CriticalWorkshopBlockEntity blockEntity;
    private final ContainerData data;

    public CriticalWorkshopMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(ModMenuTypes.CRITICAL_WORKSHOP_MENU.get(), containerId, inventory,
                getBE(inventory, extraData), getBE(inventory, extraData).getContainerData());
    }

    public CriticalWorkshopMenu(MenuType<?> type, int containerId, Inventory inventory,
                                CriticalWorkshopBlockEntity be, ContainerData data) {
        super(type, containerId);
        this.blockEntity = be;
        this.data = data;

        // 3x3 crafting grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(be.getItemHandler(), col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        // Output slot
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 9, 124, 35) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    private static CriticalWorkshopBlockEntity getBE(Inventory inventory, FriendlyByteBuf data) {
        BlockEntity be = inventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof CriticalWorkshopBlockEntity workshop) return workshop;
        throw new IllegalStateException("Block entity is not a CriticalWorkshopBlockEntity");
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            result = slotItem.copy();
            if (index < 10) {
                if (!this.moveItemStackTo(slotItem, 10, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotItem, 0, 9, false)) return ItemStack.EMPTY;
            }
            if (slotItem.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) { return blockEntity.stillValid(player); }

    public float getProgressScaled() {
        int p = data.get(0);
        return p / 80.0f;
    }
}
