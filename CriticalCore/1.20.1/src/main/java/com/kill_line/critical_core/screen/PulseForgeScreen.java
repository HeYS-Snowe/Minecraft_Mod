package com.kill_line.critical_core.screen;

import com.kill_line.critical_core.CriticalCore;
import com.kill_line.critical_core.menu.PulseForgeMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class PulseForgeScreen extends AbstractContainerScreen<PulseForgeMenu> {

    @SuppressWarnings("deprecation")
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CriticalCore.MODID, "textures/gui/pulse_forge_gui.png");

    public PulseForgeScreen(PulseForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render progress arrow
        float progress = this.menu.getCraftingProgressScaled();
        int arrowWidth = (int) (progress * 24);
        if (arrowWidth > 0) {
            guiGraphics.blit(TEXTURE, x + 79, y + 35, 176, 0, arrowWidth, 16);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);

        // XP cost display
        int xpCost = this.menu.getXpCost();
        int shardsRequired = this.menu.getShardsRequired();
        String infoText = "XP: " + xpCost + " | Shards: " + shardsRequired;
        guiGraphics.drawString(this.font, infoText, this.imageWidth - this.font.width(infoText) - 8, 6, 0x808080, false);
    }
}
