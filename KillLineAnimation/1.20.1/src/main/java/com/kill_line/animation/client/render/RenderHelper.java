package com.kill_line.animation.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * Rendering utility methods for animation effects: glow lines, trails, billboard quads.
 */
public final class RenderHelper {

    private RenderHelper() {}

    /**
     * Render a glowing line between two 3D points.
     * Draws the core line plus offset semi-transparent lines for glow effect.
     */
    public static void renderGlowLine(PoseStack poseStack, MultiBufferSource consumers,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       int color, float glowRadius) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        if (a <= 0) return;

        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        // Core line (full brightness)
        vertexLine(consumer, matrix, (float) x1, (float) y1, (float) z1, r, g, b, a);
        vertexLine(consumer, matrix, (float) x2, (float) y2, (float) z2, r, g, b, a);

        // Glow lines (offset, semi-transparent)
        if (glowRadius > 0) {
            float ga = a * 0.3f;
            // Offset in Y
            vertexLine(consumer, matrix, (float) x1, (float) y1 + glowRadius, (float) z1, r, g, b, ga);
            vertexLine(consumer, matrix, (float) x2, (float) y2 + glowRadius, (float) z2, r, g, b, ga);
            vertexLine(consumer, matrix, (float) x1, (float) y1 - glowRadius, (float) z1, r, g, b, ga);
            vertexLine(consumer, matrix, (float) x2, (float) y2 - glowRadius, (float) z2, r, g, b, ga);
        }
    }

    /**
     * Helper to render a single line vertex for RenderType.lines().
     * MC 1.20.1 format: vertex(x,y,z) -> color(r,g,b,a) -> normal(nx,ny,nz) -> endVertex()
     */
    private static void vertexLine(VertexConsumer consumer, Matrix4f matrix,
                                    float x, float y, float z,
                                    float r, float g, float b, float a) {
        consumer.vertex(matrix, x, y, z);
        consumer.color(r, g, b, a);
        consumer.normal(0f, 1f, 0f);
        consumer.endVertex();
    }

    /**
     * Render a trail from position history arrays.
     * Width interpolates from maxWidth (newest) to minWidth (oldest).
     */
    public static void renderTrail(PoseStack poseStack, MultiBufferSource consumers,
                                    double[] trailX, double[] trailY, double[] trailZ,
                                    int count, int color,
                                    float maxWidth, float minWidth) {
        if (count < 2) return;

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float baseA = ((color >> 24) & 0xFF) / 255.0f;
        if (baseA <= 0) return;

        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < count - 1; i++) {
            float t = i / (float) (count - 1);
            float segAlpha = baseA * (1.0f - t * 0.8f);

            float x1 = (float) trailX[i], y1 = (float) trailY[i], z1 = (float) trailZ[i];
            float x2 = (float) trailX[i + 1], y2 = (float) trailY[i + 1], z2 = (float) trailZ[i + 1];

            // Main trail line
            vertexLine(consumer, matrix, x1, y1, z1, r, g, b, segAlpha);
            vertexLine(consumer, matrix, x2, y2, z2, r, g, b, segAlpha * 0.5f);

            // Offset for width effect
            float width = maxWidth + (minWidth - maxWidth) * t;
            if (width > 0.01f) {
                vertexLine(consumer, matrix, x1, y1 + width, z1, r, g, b, segAlpha * 0.3f);
                vertexLine(consumer, matrix, x2, y2 + width, z2, r, g, b, segAlpha * 0.15f);
            }
        }
    }
}
