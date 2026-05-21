package com.kill_line.kill_line.menu;

import com.kill_line.kill_line.blockentity.EdictAnvilBlockEntity;
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

public class EdictAnvilMenu extends AbstractContainerMenu {

    private final EdictAnvilBlockEntity blockEntity;
    private final ContainerData data;

    public EdictAnvilMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(ModMenuTypes.EDICT_ANVIL_MENU.get(), containerId, inventory,
                getBE(inventory, extraData), getBE(inventory, extraData).getContainerData());
    }

    public EdictAnvilMenu(MenuType<?> type, int containerId, Inventory inventory,
                          EdictAnvilBlockEntity be, ContainerData data) {
        super(type, containerId);
        this.blockEntity = be;
        this.data = data;

        // Weapon slot
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 0, 27, 35));
        // Scroll slot
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 1, 76, 35));
        // Soul spark slot
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 2, 76, 60));
        // Output slot
        this.addSlot(new SlotItemHandler(be.getItemHandler(), 3, 134, 35) {
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

    private static EdictAnvilBlockEntity getBE(Inventory inventory, FriendlyByteBuf data) {
        BlockEntity be = inventory.player.level().getBlockEntity(data.readBlockPos());
        if (be instanceof EdictAnvilBlockEntity anvil) return anvil;
        throw new IllegalStateException("Block entity is not an EdictAnvilBlockEntity");
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            result = slotItem.copy();
            if (index < 4) {
                if (!this.moveItemStackTo(slotItem, 4, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotItem, 0, 3, false)) return ItemStack.EMPTY;
            }
            if (slotItem.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) { return blockEntity.stillValid(player); }

    public float getProgressScaled() { return data.get(0) / 120.0f; }
}
