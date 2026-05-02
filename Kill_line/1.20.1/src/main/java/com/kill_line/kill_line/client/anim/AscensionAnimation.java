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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Ascension: smooth rise with easeOutQuad + hover wobble -> vertex-alpha dissolve ->
 * white tint overlay + golden light pillar trail -> enhanced END_ROD particles -> fade
 */
public class AscensionAnimation extends AbstractDeathAnimation {

    private float riseOffsetY = 0;
    private float dissolveY = 0;
    private float alpha = 1.0f;

    // Interpolators
    private final Interpolator riseInterp = Interpolator.of(Easing::easeOutQuad, 0, 10);
    private final Interpolator dissolveInterp = Interpolator.of(Easing::linear, 10, 40);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeOutCubic, 40, 50);

    // White tint overlay intensity
    private float glowIntensity = 0;

    public AscensionAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Rise with hover wobble
        if (tick < 10) {
            riseOffsetY = riseInterp.getProgress(tick) * 0.5f;
            glowIntensity = riseInterp.getProgress(tick) * 0.5f;
        }

        // Phase 2: Dissolve bottom-to-top with continued rise
        if (tick >= 10 && tick < 40) {
            float dp = dissolveInterp.getProgress(tick);
            dissolveY = dp;
            riseOffsetY = 0.5f + dp * 1.0f + (float) Math.sin(tick * 0.3f) * 0.03f; // hover wobble
            glowIntensity = 0.5f + dp * 0.3f;

            // Enhanced particles: double the count
            if (tick % 2 == 0) spawnParticles();
        }

        // Phase 3: Final fade
        if (tick >= 40) {
            float fp = fadeInterp.getProgress(tick);
            alpha = 1.0f - fp;
            dissolveY = 1.0f;
            riseOffsetY += 0.05f;
            glowIntensity = 0.8f * (1.0f - fp);
        }
    }

    private void spawnParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;

        double bx = snapshot.getX();
        double by = snapshot.getY() + riseOffsetY;
        double bz = snapshot.getZ();
        float h = snapshot.getHeight();
        double dissolveWorldY = by + dissolveY * h;

        // END_ROD particles (golden-white)
        for (int i = 0; i < 6; i++) {
            double px = bx + (client.level.random.nextDouble() - 0.5) * 0.6;
            double py = dissolveWorldY + client.level.random.nextDouble() * 0.3;
            double pz = bz + (client.level.random.nextDouble() - 0.5) * 0.6;
            client.level.addParticle(ParticleTypes.END_ROD, px, py, pz,
                    (client.level.random.nextDouble() - 0.5) * 0.05,
                    0.08 + client.level.random.nextDouble() * 0.04,
                    (client.level.random.nextDouble() - 0.5) * 0.05);
        }

        // HAPPY_VILLAGER for golden sparkle
        if (tick % 3 == 0) {
            for (int i = 0; i < 2; i++) {
                double px = bx + (client.level.random.nextDouble() - 0.5) * 0.4;
                double py = dissolveWorldY + client.level.random.nextDouble() * 0.2;
                double pz = bz + (client.level.random.nextDouble() - 0.5) * 0.4;
                client.level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.04, 0);
            }
        }

        // Cloud particles
        if (tick % 4 == 0) {
            for (int i = 0; i < 3; i++) {
                double px = bx + (client.level.random.nextDouble() - 0.5) * 0.4;
                double py = dissolveWorldY + client.level.random.nextDouble() * 0.2;
                double pz = bz + (client.level.random.nextDouble() - 0.5) * 0.4;
                client.level.addParticle(ParticleTypes.CLOUD, px, py, pz, 0, 0.06, 0);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Light pillar trail below entity
        if (tick >= 5 && alpha > 0.1f) {
            float pillarAlpha = alpha * 0.3f;
            int pillarColor = ((int)(pillarAlpha * 255) << 24) | 0xFFEECC;
            float baseY = (float) snapshot.getY();
            float topY = (float) (snapshot.getY() + riseOffsetY);
            RenderHelper.renderGlowLine(poseStack, consumers,
                    0, baseY, 0, 0, topY, 0,
                    pillarColor, 0.03f);
        }

        // White tint overlay color
        int whiteTint = ((int)(glowIntensity * 128) << 24) | 0xFFFFFF;

        if (!snapshot.hasStandardBipedParts()) {
            java.util.List<String> children = snapshot.getAllChildNames();
            for (String childName : children) {
                AnimatedFragment frag = new AnimatedFragment(childName);
                frag.offset(0, riseOffsetY, 0);
                frag.alpha(alpha);
                frag.colorOverlay(whiteTint);
                FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers,
                        LightTexture.FULL_BRIGHT, tickDelta);
            }
            return;
        }

        String[] parts = {"right_leg", "left_leg", "body", "right_arm", "left_arm", "head"};
        float[] partMinY = {0.0f, 0.0f, 0.4f, 0.4f, 0.4f, 0.8f};
        float[] partMaxY = {0.4f, 0.4f, 0.8f, 0.8f, 0.8f, 1.0f};

        for (int i = 0; i < parts.length; i++) {
            // Vertex noise dissolve: skip parts based on dissolve threshold
            if (partMaxY[i] <= dissolveY) continue;

            float partAlpha;
            if (partMinY[i] >= dissolveY) {
                partAlpha = alpha;
            } else {
                float dissolvedFraction = (dissolveY - partMinY[i]) / (partMaxY[i] - partMinY[i]);
                // Add noise-like variation to dissolve boundary
                float noise = (float) Math.sin(i * 7.3 + tick * 0.5) * 0.15f;
                dissolvedFraction = Math.max(0, Math.min(1, dissolvedFraction + noise));
                partAlpha = alpha * (1.0f - dissolvedFraction);
            }

            if (partAlpha < 0.01f) continue;

            AnimatedFragment frag = new AnimatedFragment(parts[i]);
            frag.offset(0, riseOffsetY, 0);
            frag.alpha(partAlpha);
            frag.colorOverlay(whiteTint);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers,
                    LightTexture.FULL_BRIGHT, tickDelta);
        }
    }
}
