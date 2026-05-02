package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.math.Easing;
import com.kill_line.animation.client.math.Interpolator;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Ash: burn wound glow ->灼烧 red/orange tint overlay spreading from center ->
 * noise dissolve from center -> FLAME+SMOKE+ASH particles -> easeInQuad shrink -> fade
 */
public class AshAnimation extends AbstractDeathAnimation {

    private float dissolveProgress = 0;
    private float alpha = 1.0f;
    private float shrink = 1.0f;

    // Burn tint (orange-red, intensifies over time)
    private static final int BURN_TINT_LIGHT = 0x30CC4400;
    private static final int BURN_TINT_HEAVY = 0x60FF6600;

    private final Interpolator burnInterp = Interpolator.of(Easing::easeInQuad, 0, 5);
    private final Interpolator dissolveInterp = Interpolator.of(Easing::easeInCubic, 5, 35);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeOutCubic, 35, 40);

    public AshAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Wound mark glow (0-5)
        if (tick < 5) return;

        // Phase 2: Burn + dissolve (5-35)
        if (tick >= 5 && tick < 35) {
            dissolveProgress = dissolveInterp.getProgress(tick);
            shrink = 1.0f - Easing.easeInQuad(dissolveProgress) * 0.3f;

            // Spawn enhanced particles
            if (tick % 2 == 0) spawnBurnParticles();
            if (tick % 3 == 0) spawnFlameParticles();
        }

        // Phase 3: Final dispersal (35-40)
        if (tick >= 35) {
            float fp = fadeInterp.getProgress(tick);
            alpha = 1.0f - fp;
            shrink = 0.7f - fp * 0.3f;
            dissolveProgress = 1.0f;
            if (tick % 2 == 0) spawnBurnParticles();
        }
    }

    private void spawnBurnParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;

        double bx = snapshot.getX(), by = snapshot.getY(), bz = snapshot.getZ();
        float h = snapshot.getHeight();

        // WHITE_ASH + ASH particles
        for (int i = 0; i < 5; i++) {
            double px = bx + (client.level.random.nextDouble() - 0.5) * 0.6;
            double py = by + client.level.random.nextDouble() * h * shrink;
            double pz = bz + (client.level.random.nextDouble() - 0.5) * 0.6;
            // Mix rising (hot air) and falling (ash) particles
            double vy = client.level.random.nextBoolean()
                    ? -0.02 - client.level.random.nextDouble() * 0.03  // falling ash
                    : 0.03 + client.level.random.nextDouble() * 0.04;  // rising hot air
            client.level.addParticle(ParticleTypes.WHITE_ASH, px, py, pz,
                    (client.level.random.nextDouble() - 0.5) * 0.02, vy,
                    (client.level.random.nextDouble() - 0.5) * 0.02);
        }

        if (tick % 4 == 0) {
            client.level.addParticle(ParticleTypes.ASH,
                    bx + (client.level.random.nextDouble() - 0.5) * 0.4,
                    by + h * 0.5 * shrink,
                    bz + (client.level.random.nextDouble() - 0.5) * 0.4,
                    0, 0.03, 0);
        }
    }

    private void spawnFlameParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;

        double bx = snapshot.getX(), by = snapshot.getY(), bz = snapshot.getZ();
        float h = snapshot.getHeight();

        // FLAME particles near the model center
        for (int i = 0; i < 3; i++) {
            double px = bx + (client.level.random.nextDouble() - 0.5) * 0.4;
            double py = by + h * 0.3 * shrink + client.level.random.nextDouble() * h * 0.4 * shrink;
            double pz = bz + (client.level.random.nextDouble() - 0.5) * 0.4;
            client.level.addParticle(ParticleTypes.FLAME, px, py, pz,
                    (client.level.random.nextDouble() - 0.5) * 0.03,
                    0.02 + client.level.random.nextDouble() * 0.03,
                    (client.level.random.nextDouble() - 0.5) * 0.03);
        }

        // SMOKE particles (occasional)
        if (tick % 6 == 0) {
            double py = by + h * 0.6 * shrink;
            client.level.addParticle(ParticleTypes.LARGE_SMOKE,
                    bx + (client.level.random.nextDouble() - 0.5) * 0.3, py,
                    bz + (client.level.random.nextDouble() - 0.5) * 0.3,
                    (client.level.random.nextDouble() - 0.5) * 0.01,
                    0.03 + client.level.random.nextDouble() * 0.02,
                    (client.level.random.nextDouble() - 0.5) * 0.01);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Burn tint intensifies with dissolve progress
        int burnTint;
        if (dissolveProgress < 0.3f) {
            burnTint = BURN_TINT_LIGHT;
        } else {
            // Lerp from light to heavy burn tint
            float heavyT = Math.min(1, (dissolveProgress - 0.3f) / 0.4f);
            int lightA = (BURN_TINT_LIGHT >> 24) & 0xFF;
            int heavyA = (BURN_TINT_HEAVY >> 24) & 0xFF;
            int tA = (int) (lightA + (heavyA - lightA) * heavyT);
            burnTint = (tA << 24) | 0xFF6600;
        }

        if (!snapshot.hasStandardBipedParts()) {
            java.util.List<String> children = snapshot.getAllChildNames();
            for (String childName : children) {
                // Noise-based dissolve: random skip based on dissolveProgress
                float partDissolve = getNoiseDissolve(childName, dissolveProgress);
                if (partDissolve >= 1.0f) continue;

                AnimatedFragment frag = new AnimatedFragment(childName);
                frag.scale(shrink);
                frag.alpha(alpha * (1.0f - partDissolve));
                frag.colorOverlay(burnTint);
                FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
            }
            return;
        }

        String[] parts = {"body", "right_arm", "left_arm", "right_leg", "left_leg", "head"};
        float[] dissolveThreshold = {0.0f, 0.2f, 0.2f, 0.3f, 0.3f, 0.6f};

        for (int i = 0; i < parts.length; i++) {
            float partDissolve = getNoiseDissolve(parts[i], dissolveProgress, dissolveThreshold[i]);
            if (partDissolve >= 1.0f) continue;

            float partAlpha = alpha * (1.0f - partDissolve);
            if (partAlpha < 0.01f) continue;

            AnimatedFragment frag = new AnimatedFragment(parts[i]);
            frag.scale(shrink);
            frag.alpha(partAlpha);
            frag.colorOverlay(burnTint);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
        }
    }

    /** Simple noise-based dissolve using hash of part name + tick */
    private float getNoiseDissolve(String partName, float globalProgress) {
        return getNoiseDissolve(partName, globalProgress, 0);
    }

    private float getNoiseDissolve(String partName, float globalProgress, float threshold) {
        if (globalProgress < threshold) return 0;
        float partProgress = (globalProgress - threshold) / 0.4f;
        // Add noise variation based on part name hash
        int hash = partName.hashCode();
        float noise = (float) Math.sin(hash * 13.7 + tick * 0.3) * 0.15f;
        return Math.max(0, Math.min(1, partProgress + noise));
    }
}
