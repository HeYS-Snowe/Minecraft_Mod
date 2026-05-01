package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Matrix4f;

/**
 * Impale: phantom swords pierce the body -> entity kneels and slumps -> body fades out
 */
public class ImpaleAnimation extends AbstractDeathAnimation {

    private float bodyOffsetY = 0;
    private float headPitch = 0;
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private float swordAlpha = 0.0f;

    public ImpaleAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Swords materialize (0-10)
        if (tick < 10) {
            swordAlpha = tick / 10.0f;
            return;
        }

        // Phase 2: Entity slumps (10-40)
        if (tick >= 10 && tick < 40) {
            float slumpProgress = (tick - 10) / 30.0f;
            float ease = slumpProgress * slumpProgress;
            bodyOffsetY = -ease * 0.6f;
            headPitch = ease * 1.0f;

            // Spawn impact particles at sword positions
            if (tick % 3 == 0) spawnSwordParticles();
        }

        // Phase 3: Body cracks and fades (40-60)
        if (tick >= 40) {
            float crackProgress = (tick - 40) / 20.0f;
            alpha = 1.0f - crackProgress;
            scale = Math.max(0.1f, 1.0f - crackProgress * 0.3f);
            swordAlpha = Math.max(0, 1.0f - crackProgress * 2.0f);
        }
    }

    private void spawnSwordParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;

        double baseX = snapshot.getX();
        double baseY = snapshot.getY();
        double baseZ = snapshot.getZ();
        float height = snapshot.getHeight();

        // Particles at sword entry/exit points
        float[] swordHeights = {0.3f, 0.55f, 0.8f};
        for (float h : swordHeights) {
            double py = baseY + h * height + bodyOffsetY;
            for (int i = 0; i < 2; i++) {
                double px = baseX + (client.level.random.nextDouble() - 0.5) * 0.5;
                double pz = baseZ + (client.level.random.nextDouble() - 0.5) * 0.5;
                client.level.addParticle(ParticleTypes.ENCHANTED_HIT, px, py, pz,
                        (client.level.random.nextDouble() - 0.5) * 0.1,
                        client.level.random.nextDouble() * 0.05,
                        (client.level.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Render phantom swords (always)
        if (swordAlpha > 0.01f) {
            renderPhantomSwords(poseStack, consumers);
        }

        if (!snapshot.hasStandardBipedParts()) {
            // Fallback: enumerate all children with slump effect
            java.util.List<String> children = snapshot.getAllChildNames();
            for (String childName : children) {
                AnimatedFragment frag = new AnimatedFragment(childName);
                frag.offset(0, bodyOffsetY, 0);
                frag.rotation(headPitch * 0.2f, 0, 0);
                frag.scale(scale);
                frag.alpha(alpha);
                FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light);
            }
            return;
        }

        // Render entity parts with slump
        String[] allParts = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
        for (String partName : allParts) {
            AnimatedFragment frag = new AnimatedFragment(partName);

            frag.offset(0, bodyOffsetY, 0);

            if ("head".equals(partName)) {
                frag.rotation(headPitch, 0, 0);
            }
            if ("right_arm".equals(partName) || "left_arm".equals(partName)) {
                frag.rotation(headPitch * 0.8f, 0, "right_arm".equals(partName) ? 0.1f : -0.1f);
            }

            frag.scale(scale);
            frag.alpha(alpha);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light);
        }
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void renderPhantomSwords(PoseStack poseStack, MultiBufferSource consumers) {
        float height = snapshot.getHeight();

        // Sword definitions: {centerY (fraction of height), dirX, dirZ, length}
        float[][] swords = {
                {0.30f, 1.0f, 0.0f, 0.8f},  // side-to-side at knee level
                {0.55f, 0.0f, 1.0f, 0.7f},  // front-to-back at waist
                {0.80f, 1.0f, 0.3f, 0.8f},  // side-to-side at chest, slightly angled
        };

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = consumers.getBuffer(RenderType.lines());

        float pulse = 0.7f + 0.3f * (float) Math.sin(tick * 0.5f);
        int a = (int) (swordAlpha * pulse * 200);

        // Core sword lines
        int coreColor = argb(a, 180, 220, 255);
        int glowColor = argb(a / 2, 140, 180, 255);

        for (float[] sword : swords) {
            float cy = sword[0] * height + bodyOffsetY;
            float dx = sword[1];
            float dz = sword[2];
            float len = sword[3];
            float halfLen = len / 2;

            float x1 = -dx * halfLen;
            float y1 = cy;
            float z1 = -dz * halfLen;
            float x2 = dx * halfLen;
            float y2 = cy;
            float z2 = dz * halfLen;

            // Glow layer (wider, dimmer)
            float glowOffset = 0.015f;
            drawLine(consumer, matrix, x1 - dz * glowOffset, y1, z1 + dx * glowOffset,
                    x2 - dz * glowOffset, y2, z2 + dx * glowOffset, glowColor);
            drawLine(consumer, matrix, x1 + dz * glowOffset, y1, z1 - dx * glowOffset,
                    x2 + dz * glowOffset, y2, z2 - dx * glowOffset, glowColor);

            // Core line
            drawLine(consumer, matrix, x1, y1, z1, x2, y2, z2, coreColor);
        }
    }

    private void drawLine(VertexConsumer consumer, Matrix4f matrix,
                          float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        consumer.vertex(matrix, x1, y1, z1).color(color).normal(0f, 1f, 0f);
        consumer.vertex(matrix, x2, y2, z2).color(color).normal(0f, 1f, 0f);
    }
}
