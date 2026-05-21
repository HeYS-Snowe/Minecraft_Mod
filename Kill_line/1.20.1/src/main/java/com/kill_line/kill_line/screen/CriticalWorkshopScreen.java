package com.kill_line.kill_line.screen;

import com.kill_line.kill_line.Constants;
import com.kill_line.kill_line.menu.CriticalWorkshopMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class CriticalWorkshopScreen extends AbstractContainerScreen<CriticalWorkshopMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/critical_workshop_gui.png");

    public CriticalWorkshopScreen(CriticalWorkshopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        float progress = this.menu.getProgressScaled();
        int arrowWidth = (int) (progress * 22);
        if (arrowWidth > 0) {
            guiGraphics.blit(TEXTURE, x + 100, y + 35, 176, 0, arrowWidth, 16);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
