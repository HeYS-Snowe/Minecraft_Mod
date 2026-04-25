package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Ascension: slow rise -> dissolve from feet to head into golden-white light particles
 */
public class AscensionAnimation extends AbstractDeathAnimation {

    private float riseOffsetY = 0;
    private float dissolveY = 0;
    private float alpha = 1.0f;
    private float glow = 0;

    public AscensionAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Rise and glow (0-10)
        if (tick < 10) {
            float riseProgress = tick / 10.0f;
            riseOffsetY = riseProgress * 0.5f;
            glow = riseProgress;
        }

        // Phase 2: Dissolve bottom-to-top (10-40)
        if (tick >= 10 && tick < 40) {
            float dissolveProgress = (tick - 10) / 30.0f;
            dissolveY = dissolveProgress;
            riseOffsetY = 0.5f + dissolveProgress * 1.0f;

            if (tick % 2 == 0 && snapshot.getEntityType() != null) {
                spawnParticles();
            }
        }

        // Phase 3: Final fade (40-50)
        if (tick >= 40) {
            float fadeProgress = (tick - 40) / 10.0f;
            alpha = 1.0f - fadeProgress;
            dissolveY = 1.0f;
            riseOffsetY += 0.05f;
        }
    }

    private void spawnParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;

        double baseX = snapshot.getX();
        double baseY = snapshot.getY() + riseOffsetY;
        double baseZ = snapshot.getZ();

        float height = snapshot.getHeight();
        double dissolveWorldY = baseY + dissolveY * height;

        for (int i = 0; i < 4; i++) {
            double px = baseX + (client.level.random.nextDouble() - 0.5) * 0.6;
            double py = dissolveWorldY + client.level.random.nextDouble() * 0.3;
            double pz = baseZ + (client.level.random.nextDouble() - 0.5) * 0.6;
            // Golden-white rising particles
            client.level.addParticle(ParticleTypes.END_ROD, px, py, pz,
                    (client.level.random.nextDouble() - 0.5) * 0.05, 0.08 + client.level.random.nextDouble() * 0.04,
                    (client.level.random.nextDouble() - 0.5) * 0.05);
        }
        // Extra cloud particles for more visible effect
        if (tick % 4 == 0) {
            for (int i = 0; i < 2; i++) {
                double px = baseX + (client.level.random.nextDouble() - 0.5) * 0.4;
                double py = dissolveWorldY + client.level.random.nextDouble() * 0.2;
                double pz = baseZ + (client.level.random.nextDouble() - 0.5) * 0.4;
                client.level.addParticle(ParticleTypes.CLOUD, px, py, pz, 0, 0.06, 0);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        String[] parts = {"right_leg", "left_leg", "body", "right_arm", "left_arm", "head"};
        float height = snapshot.getHeight();

        float[] partMinY = {0.0f, 0.0f, 0.4f, 0.4f, 0.4f, 0.8f};
        float[] partMaxY = {0.4f, 0.4f, 0.8f, 0.8f, 0.8f, 1.0f};

        for (int i = 0; i < parts.length; i++) {
            if (partMaxY[i] <= dissolveY) continue;

            float partAlpha;
            if (partMinY[i] >= dissolveY) {
                partAlpha = alpha;
            } else {
                float dissolvedFraction = (dissolveY - partMinY[i]) / (partMaxY[i] - partMinY[i]);
                partAlpha = alpha * (1.0f - dissolvedFraction);
            }

            if (partAlpha < 0.01f) continue;

            AnimatedFragment frag = new AnimatedFragment(parts[i]);
            frag.offset(0, riseOffsetY, 0);
            frag.alpha(partAlpha);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers,
                    LightTexture.FULL_BRIGHT);
        }
    }
}
