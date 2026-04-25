package com.kill_line.kill_line.client.render;

import com.kill_line.kill_line.client.slash.SlashLineManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Renders diagonal slash lines on invulnerable entities.
 * Multi-layer rendering: outer glow -> inner glow -> core line.
 */
@Mod.EventBusSubscriber(modid = "kill_line", value = Dist.CLIENT)
public class SlashLineRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        for (var entry : SlashLineManager.getInstance().getActiveLines().entrySet()) {
            int entityId = entry.getKey();
            float progress = entry.getValue().progress();

            Entity entity = client.level.getEntity(entityId);
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;

            MultiBufferSource consumers = client.renderBuffers().bufferSource();
            renderSlashLine(poseStack, consumers, living, progress, camera);
        }
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void renderSlashLine(PoseStack poseStack, MultiBufferSource consumers,
                                         LivingEntity entity, float progress, Vec3 camera) {
        Vec3[] endpoints = SlashLineManager.computeLineEndpoints(entity);
        Vec3 start = endpoints[0];
        Vec3 end = endpoints[1];

        double startX = start.x - camera.x;
        double startY = start.y - camera.y;
        double startZ = start.z - camera.z;
        double endX = end.x - camera.x;
        double endY = end.y - camera.y;
        double endZ = end.z - camera.z;

        double progX = startX + (endX - startX) * progress;
        double progY = startY + (endY - startY) * progress;
        double progZ = startZ + (endZ - startZ) * progress;

        Vec3 lineDir = new Vec3(endX - startX, endY - startY, endZ - startZ);
        double lineLen = lineDir.length();
        if (lineLen < 0.01) return;
        lineDir = lineDir.scale(1.0 / lineLen);

        Vec3 midToCamera = new Vec3(
                -(startX + endX) / 2,
                -(startY + endY) / 2,
                -(startZ + endZ) / 2
        );
        if (midToCamera.lengthSqr() < 0.001) midToCamera = new Vec3(0, 0, 1);
        midToCamera = midToCamera.normalize();

        Vec3 perpDir = lineDir.cross(midToCamera);
        if (perpDir.lengthSqr() < 0.001) perpDir = new Vec3(0, 0, 1);
        perpDir = perpDir.normalize();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());

        float time = (float) (System.currentTimeMillis() % 2000) / 2000.0f;
        float pulse = 0.7f + 0.3f * (float) Math.sin(time * Math.PI * 2);

        // Completed portion: start -> progress
        int glowOuter = argb((int) (30 * pulse), 255, 140, 20);
        drawMultiLine(consumer, matrix, startX, startY, startZ, progX, progY, progZ,
                glowOuter, perpDir, 0.10f, 7);

        int glowInner = argb((int) (70 * pulse), 255, 190, 50);
        drawMultiLine(consumer, matrix, startX, startY, startZ, progX, progY, progZ,
                glowInner, perpDir, 0.05f, 5);

        int coreColor = argb((int) (235 * pulse), 255, 240, 200);
        drawMultiLine(consumer, matrix, startX, startY, startZ, progX, progY, progZ,
                coreColor, perpDir, 0.01f, 1);

        // Incomplete portion: progress -> end
        int incompleteGlow = argb(25, 160, 35, 35);
        drawMultiLine(consumer, matrix, progX, progY, progZ, endX, endY, endZ,
                incompleteGlow, perpDir, 0.05f, 3);
        int incompleteCore = argb(90, 190, 45, 45);
        drawLineSegment(consumer, matrix, progX, progY, progZ, endX, endY, endZ, incompleteCore);

        // Start dot
        int dotColor = argb((int) (160 * pulse), 255, 220, 140);
        float dotR = 0.03f;
        drawLineSegment(consumer, matrix,
                startX - dotR, startY, startZ, startX + dotR, startY, startZ, dotColor);
        drawLineSegment(consumer, matrix,
                startX, startY - dotR, startZ, startX, startY + dotR, startZ, dotColor);

        // Flash at 100%
        if (progress >= 1.0f) {
            int flashColor = argb((int) (255 * pulse), 255, 255, 255);
            float off = 0.10f;
            drawLineSegment(consumer, matrix,
                    endX - off, endY, endZ, endX + off, endY, endZ, flashColor);
            drawLineSegment(consumer, matrix,
                    endX, endY - off, endZ, endX, endY + off, endZ, flashColor);
            drawLineSegment(consumer, matrix,
                    endX, endY, endZ - off, endX, endY, endZ + off, flashColor);

            int burstColor = argb((int) (100 * pulse), 255, 255, 180);
            drawMultiLine(consumer, matrix,
                    endX - off * 1.5f, endY - off * 1.5f, endZ,
                    endX + off * 1.5f, endY + off * 1.5f, endZ,
                    burstColor, perpDir, 0.08f, 5);
        }
    }

    private static void drawMultiLine(VertexConsumer consumer, Matrix4f matrix,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       int color, Vec3 perpDir, float halfWidth, int count) {
        for (int i = 0; i < count; i++) {
            float t = count == 1 ? 0 : (2.0f * i / (count - 1) - 1.0f);
            float offset = t * halfWidth;
            float ox = (float) perpDir.x * offset;
            float oy = (float) perpDir.y * offset;
            float oz = (float) perpDir.z * offset;
            drawLineSegment(consumer, matrix,
                    x1 + ox, y1 + oy, z1 + oz,
                    x2 + ox, y2 + oy, z2 + oz, color);
        }
    }

    private static void drawLineSegment(VertexConsumer consumer, Matrix4f matrix,
                                         double x1, double y1, double z1,
                                         double x2, double y2, double z2, int color) {
        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1)
                .color(color)
                .normal(0f, 1f, 0f);
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2)
                .color(color)
                .normal(0f, 1f, 0f);
    }
}
