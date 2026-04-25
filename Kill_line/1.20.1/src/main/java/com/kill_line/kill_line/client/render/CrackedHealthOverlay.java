package com.kill_line.kill_line.client.render;

import com.kill_line.kill_line.client.mark.ClientMarkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CrackedHealthOverlay implements IGuiOverlay {

    public static final CrackedHealthOverlay INSTANCE = new CrackedHealthOverlay();

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        Entity target = client.crosshairPickEntity;
        if (!(target instanceof LivingEntity livingTarget)) return;

        ClientMarkManager.MarkInfo mark = ClientMarkManager.getInstance().getMark(target.getId());
        if (mark == null || !mark.thresholdReached()) return;

        if (!shouldShowOverlay(client.player, livingTarget, mark)) return;

        renderCrackedHealthBar(guiGraphics, livingTarget, client, screenWidth, screenHeight);
    }

    private static boolean shouldShowOverlay(Player localPlayer, LivingEntity target, ClientMarkManager.MarkInfo mark) {
        if (target instanceof Mob) return true;
        if (target instanceof Player) {
            return localPlayer.getId() == mark.attackerEntityId();
        }
        return true;
    }

    private static void renderCrackedHealthBar(GuiGraphics guiGraphics, LivingEntity target, Minecraft client, int screenWidth, int screenHeight) {
        float healthPercent = target.getHealth() / target.getMaxHealth();
        int barWidth = 100;
        int barHeight = 6;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight / 2 + 30;

        guiGraphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFF000000);

        int healthWidth = (int) (barWidth * healthPercent);
        guiGraphics.fill(x, y, x + healthWidth, y + barHeight, 0xFFCC0000);

        for (int i = 0; i < 4; i++) {
            int crackX = x + (int) (barWidth * (0.2 + i * 0.2));
            if (crackX < x + healthWidth) {
                guiGraphics.fill(crackX, y, crackX + 1, y + barHeight, 0xFF000000);
                guiGraphics.fill(crackX - 1, y + barHeight / 2, crackX + 2, y + barHeight / 2 + 1, 0xFF000000);
            }
        }

        guiGraphics.drawString(client.font, "X", x + barWidth + 5, y - 2, 0xFFFF3333, true);
    }
}
