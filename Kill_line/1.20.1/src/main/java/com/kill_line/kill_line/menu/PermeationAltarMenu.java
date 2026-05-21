package com.kill_line.kill_line.menu;

import com.kill_line.kill_line.blockentity.PermeationAltarBlockEntity;
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

public class PermeationAltarMenu extends AbstractContainerMenu {

    private final PermeationAltarBlockEntity blockEntity;
    private final ContainerData data;

    public PermeationAltarMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(ModMenuTypes.PERMEATION_ALTAR_MENU.get(), containerId, inventory,
                getBE(inventory, extraData), getBE(inventory, extraData).getContainerData());
    }

    public PermeationAltarMenu(MenuType<?> type, int containerId, Inventory inventory,
                               PermeationAltarBlockEntity be, ContainerData data) {
        super(type, containerId);
        this.blockEntity = be;
        this.data = data;

        // Input slots: essence, page, thread
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 0, 36, 25));
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 1, 62, 25));
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 2, 88, 25));
        // Insurance slot (stable core)
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 3, 62, 55));
        // Output slot
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 4, 134, 40) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });

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

    private static PermeationAltarBlockEntity getBE(Inventory inventory, FriendlyByteBuf data) {
        BlockEntity be = inventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof PermeationAltarBlockEntity altar) return altar;
        throw new IllegalStateException("Block entity is not a PermeationAltarBlockEntity");
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            result = slotItem.copy();
            if (index < 5) {
                if (!this.moveItemStackTo(slotItem, 5, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotItem, 0, 4, false)) return ItemStack.EMPTY;
            }
            if (slotItem.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) { return blockEntity.stillValid(player); }

    public float getProgressScaled() { return data.get(0) / 100.0f; }
    public int getConsecutiveFailures() { return data.get(1); }
}
