package com.kill_line.animation.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * VertexConsumer wrapper that applies a color tint (ARGB multiply) to all vertex colors.
 * In MC 1.20.1, VertexConsumer is an interface with void return types.
 * We intercept color() calls and multiply by tint values.
 */
public class TintedVertexConsumer implements VertexConsumer {

    private final VertexConsumer inner;
    private final float tintR, tintG, tintB, tintA;

    public TintedVertexConsumer(VertexConsumer inner, int argb) {
        this.inner = inner;
        this.tintA = ((argb >> 24) & 0xFF) / 255.0f;
        this.tintR = ((argb >> 16) & 0xFF) / 255.0f;
        this.tintG = ((argb >> 8) & 0xFF) / 255.0f;
        this.tintB = (argb & 0xFF) / 255.0f;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        inner.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        inner.color(
                (int) (red * tintR),
                (int) (green * tintG),
                (int) (blue * tintB),
                (int) (alpha * tintA)
        );
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        inner.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        inner.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        inner.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        inner.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        inner.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        inner.defaultColor((int)(r * tintR), (int)(g * tintG), (int)(b * tintB), (int)(a * tintA));
    }

    @Override
    public void unsetDefaultColor() {
        inner.unsetDefaultColor();
    }
}
