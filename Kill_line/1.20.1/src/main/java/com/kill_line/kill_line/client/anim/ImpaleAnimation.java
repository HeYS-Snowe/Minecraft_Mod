package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.math.Easing;
import com.kill_line.animation.client.math.Interpolator;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.client.render.RenderHelper;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Matrix4f;

/**
 * Impale: phantom swords with glow halo -> staggered slump (knees/torso/head) ->
 * crack tint -> fade with easeInExpo
 */
public class ImpaleAnimation extends AbstractDeathAnimation {

    private float bodyOffsetY = 0;
    private float kneeRot = 0;
    private float torsoRot = 0;
    private float headPitch = 0;
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private float swordAlpha = 0.0f;

    // Staggered interpolators for slump
    private final Interpolator swordAppearInterp = Interpolator.of(Easing::easeOutCubic, 0, 10);
    private final Interpolator kneeInterp = Interpolator.of(Easing::easeOutCubic, 10, 25);
    private final Interpolator torsoInterp = Interpolator.of(Easing::easeInOutCubic, 12, 30);
    private final Interpolator headInterp = Interpolator.of(Easing::easeOutQuad, 15, 35);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeInExpo, 40, 60);

    // Crack tint (dark red)
    private static final int CRACK_TINT = 0x40440000;

    public ImpaleAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Swords materialize
        swordAlpha = swordAppearInterp.getProgress(tick);

        // Phase 2: Staggered slump
        kneeRot = kneeInterp.getProgress(tick) * 0.5f;
        torsoRot = torsoInterp.getProgress(tick) * 0.4f;
        headPitch = headInterp.getProgress(tick) * 1.0f;
        bodyOffsetY = -kneeInterp.getProgress(tick) * 0.3f - torsoInterp.getProgress(tick) * 0.3f;

        // Particles at sword positions
        if (tick >= 8 && tick <= 12 && tick % 2 == 0) spawnImpactParticles();
        if (tick >= 10 && tick <= 35 && tick % 3 == 0) spawnSwordParticles();

        // Phase 3: Crack + fade
        if (tick >= 40) {
            float fadeP = fadeInterp.getProgress(tick);
            alpha = 1.0f - fadeP;
            scale = Math.max(0.1f, 1.0f - fadeP * 0.3f);
            swordAlpha = Math.max(0, 1.0f - fadeP * 2.0f);
        }
    }

    private void spawnImpactParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        double bx = snapshot.getX(), by = snapshot.getY(), bz = snapshot.getZ();
        float h = snapshot.getHeight();
        float[] swordHeights = {0.3f, 0.55f, 0.8f};
        for (float sh : swordHeights) {
            double py = by + sh * h;
            for (int i = 0; i < 4; i++) {
                client.level.addParticle(ParticleTypes.ENCHANTED_HIT,
                        bx + (client.level.random.nextDouble() - 0.5) * 0.5, py,
                        bz + (client.level.random.nextDouble() - 0.5) * 0.5,
                        (client.level.random.nextDouble() - 0.5) * 0.1,
                        client.level.random.nextDouble() * 0.05,
                        (client.level.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    private void spawnSwordParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        double bx = snapshot.getX(), by = snapshot.getY(), bz = snapshot.getZ();
        float h = snapshot.getHeight();
        float[] swordHeights = {0.3f, 0.55f, 0.8f};
        for (float sh : swordHeights) {
            double py = by + sh * h + bodyOffsetY;
            for (int i = 0; i < 2; i++) {
                client.level.addParticle(ParticleTypes.CRIT,
                        bx + (client.level.random.nextDouble() - 0.5) * 0.5, py,
                        bz + (client.level.random.nextDouble() - 0.5) * 0.5,
                        (client.level.random.nextDouble() - 0.5) * 0.08,
                        client.level.random.nextDouble() * 0.03,
                        (client.level.random.nextDouble() - 0.5) * 0.08);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Phantom swords with glow
        if (swordAlpha > 0.01f) {
            renderPhantomSwords(poseStack, consumers, tickDelta);
        }

        if (!snapshot.hasStandardBipedParts()) {
            java.util.List<String> children = snapshot.getAllChildNames();
            for (String childName : children) {
                AnimatedFragment frag = new AnimatedFragment(childName);
                frag.offset(0, bodyOffsetY, 0);
                frag.rotation(headPitch * 0.2f, 0, 0);
                frag.scale(scale);
                frag.alpha(alpha);
                if (tick >= 35) frag.colorOverlay(CRACK_TINT);
                FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
            }
            return;
        }

        String[] allParts = {"right_leg", "left_leg", "body", "right_arm", "left_arm", "head"};
        for (String partName : allParts) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.offset(0, bodyOffsetY, 0);

            switch (partName) {
                case "head":
                    frag.rotation(headPitch, 0, 0);
                    break;
                case "body":
                    frag.rotation(torsoRot, 0, 0);
                    break;
                case "right_leg":
                case "left_leg":
                    frag.rotation(kneeRot, 0, "right_leg".equals(partName) ? 0.05f : -0.05f);
                    break;
                case "right_arm":
                case "left_arm":
                    frag.rotation(torsoRot * 0.8f, 0, "right_arm".equals(partName) ? 0.1f : -0.1f);
                    break;
            }

            frag.scale(scale);
            frag.alpha(alpha);

            // Apply crack tint in late phase
            if (tick >= 35) frag.colorOverlay(CRACK_TINT);

            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
        }
    }

    private void renderPhantomSwords(PoseStack poseStack, MultiBufferSource consumers, float tickDelta) {
        float height = snapshot.getHeight();

        float[][] swords = {
                {0.30f, 1.0f, 0.0f, 2.0f},
                {0.55f, 0.0f, 1.0f, 1.8f},
                {0.80f, 1.0f, 0.3f, 2.0f},
        };

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());

        // Pulsing with combined frequencies for organic feel
        float pulse = 0.7f + 0.2f * (float) Math.sin(tick * 0.5f)
                            + 0.1f * (float) Math.sin(tick * 1.3f);

        for (float[] sword : swords) {
            float cy = sword[0] * height + bodyOffsetY;
            float dx = sword[1], dz = sword[2];
            float len = sword[3];
            float halfLen = len / 2;

            float x1 = -dx * halfLen, y1 = cy, z1 = -dz * halfLen;
            float x2 = dx * halfLen, y2 = cy, z2 = dz * halfLen;

            // Core sword line (bright)
            int coreA = (int) (swordAlpha * pulse * 200);
            int coreColor = argb(coreA, 180, 220, 255);
            drawLine(consumer, matrix, x1, y1, z1, x2, y2, z2, coreColor);

            // Glow halo (wider, dimmer, offset)
            int glowA = coreA / 2;
            int glowColor = argb(glowA, 140, 180, 255);
            float off = 0.06f;
            drawLine(consumer, matrix, x1 - dz * off, y1, z1 + dx * off, x2 - dz * off, y2, z2 + dx * off, glowColor);
            drawLine(consumer, matrix, x1 + dz * off, y1, z1 - dx * off, x2 + dz * off, y2, z2 - dx * off, glowColor);
        }
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawLine(VertexConsumer consumer, Matrix4f matrix,
                          float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        consumer.vertex(matrix, x1, y1, z1).color(color).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(color).normal(0f, 1f, 0f).endVertex();
    }
}
