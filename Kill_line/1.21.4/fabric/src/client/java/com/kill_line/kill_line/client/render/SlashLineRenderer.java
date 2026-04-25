package com.kill_line.kill_line.client.render;

import com.kill_line.kill_line.client.slash.SlashLineManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Renders diagonal slash lines on invulnerable entities.
 * Multi-layer rendering: outer glow → inner glow → core line.
 * Progress coloring: gold glow (completed) → dim red (incomplete).
 */
public class SlashLineRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();

            for (var entry : SlashLineManager.getInstance().getActiveLines().entrySet()) {
                int entityId = entry.getKey();
                float progress = entry.getValue().progress();

                Entity entity = client.world.getEntityById(entityId);
                if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;

                renderSlashLine(matrices, context.consumers(), living, progress, camera);
            }
        });
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void renderSlashLine(MatrixStack matrices, VertexConsumerProvider consumers,
                                         LivingEntity entity, float progress, Vec3d camera) {
        Vec3d[] endpoints = SlashLineManager.computeLineEndpoints(entity);
        Vec3d start = endpoints[0];
        Vec3d end = endpoints[1];

        // Camera-relative coordinates
        double startX = start.x - camera.x;
        double startY = start.y - camera.y;
        double startZ = start.z - camera.z;
        double endX = end.x - camera.x;
        double endY = end.y - camera.y;
        double endZ = end.z - camera.z;

        // Progress point
        double progX = startX + (endX - startX) * progress;
        double progY = startY + (endY - startY) * progress;
        double progZ = startZ + (endZ - startZ) * progress;

        // Compute screen-perpendicular direction for line thickness
        Vec3d lineDir = new Vec3d(endX - startX, endY - startY, endZ - startZ);
        double lineLen = lineDir.length();
        if (lineLen < 0.01) return;
        lineDir = lineDir.multiply(1.0 / lineLen);

        // Perpendicular: cross product of line direction with direction toward camera
        Vec3d midToCamera = new Vec3d(
                -(startX + endX) / 2,
                -(startY + endY) / 2,
                -(startZ + endZ) / 2
        );
        if (midToCamera.lengthSquared() < 0.001) midToCamera = new Vec3d(0, 0, 1);
        midToCamera = midToCamera.normalize();

        Vec3d perpDir = lineDir.crossProduct(midToCamera);
        if (perpDir.lengthSquared() < 0.001) perpDir = new Vec3d(0, 0, 1);
        perpDir = perpDir.normalize();

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getLines());

        // Pulse animation
        float time = (float) (System.currentTimeMillis() % 2000) / 2000.0f;
        float pulse = 0.7f + 0.3f * (float) Math.sin(time * Math.PI * 2);

        // ======== Completed portion: start → progress ========

        // Layer 1: Outer glow (widest, faint)
        int glowOuter = argb((int) (30 * pulse), 255, 140, 20);
        drawMultiLine(consumer, matrix, startX, startY, startZ, progX, progY, progZ,
                glowOuter, perpDir, 0.10f, 7);

        // Layer 2: Inner glow
        int glowInner = argb((int) (70 * pulse), 255, 190, 50);
        drawMultiLine(consumer, matrix, startX, startY, startZ, progX, progY, progZ,
                glowInner, perpDir, 0.05f, 5);

        // Layer 3: Core (brightest)
        int coreColor = argb((int) (235 * pulse), 255, 240, 200);
        drawMultiLine(consumer, matrix, startX, startY, startZ, progX, progY, progZ,
                coreColor, perpDir, 0.01f, 1);

        // ======== Incomplete portion: progress → end ========

        int incompleteGlow = argb(25, 160, 35, 35);
        drawMultiLine(consumer, matrix, progX, progY, progZ, endX, endY, endZ,
                incompleteGlow, perpDir, 0.05f, 3);
        int incompleteCore = argb(90, 190, 45, 45);
        drawLineSegment(consumer, matrix, progX, progY, progZ, endX, endY, endZ, incompleteCore);

        // ======== Endpoint markers ========

        // Start dot
        int dotColor = argb((int) (160 * pulse), 255, 220, 140);
        float dotR = 0.03f;
        drawLineSegment(consumer, matrix,
                startX - dotR, startY, startZ, startX + dotR, startY, startZ, dotColor);
        drawLineSegment(consumer, matrix,
                startX, startY - dotR, startZ, startX, startY + dotR, startZ, dotColor);

        // ======== Flash at 100% ========
        if (progress >= 1.0f) {
            // Cross burst at endpoint
            int flashColor = argb((int) (255 * pulse), 255, 255, 255);
            float off = 0.10f;
            drawLineSegment(consumer, matrix,
                    endX - off, endY, endZ, endX + off, endY, endZ, flashColor);
            drawLineSegment(consumer, matrix,
                    endX, endY - off, endZ, endX, endY + off, endZ, flashColor);
            drawLineSegment(consumer, matrix,
                    endX, endY, endZ - off, endX, endY, endZ + off, flashColor);

            // Glow burst along slash at endpoint
            int burstColor = argb((int) (100 * pulse), 255, 255, 180);
            drawMultiLine(consumer, matrix,
                    endX - off * 1.5f, endY - off * 1.5f, endZ,
                    endX + off * 1.5f, endY + off * 1.5f, endZ,
                    burstColor, perpDir, 0.08f, 5);
        }
    }

    /**
     * Draw multiple parallel lines offset perpendicular to the line direction.
     * Creates a "thick" line appearance.
     */
    private static void drawMultiLine(VertexConsumer consumer, Matrix4f matrix,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       int color, Vec3d perpDir, float halfWidth, int count) {
        for (int i = 0; i < count; i++) {
            float t = count == 1 ? 0 : (2.0f * i / (count - 1) - 1.0f); // -1 to 1
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
