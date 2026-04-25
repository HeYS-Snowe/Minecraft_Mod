package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Ash: wound mark appears -> body dissolves from inside out into grey embers
 */
public class AshAnimation extends AbstractDeathAnimation {

    private float dissolveProgress = 0;
    private float alpha = 1.0f;
    private float shrink = 1.0f;

    public AshAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Wound mark (0-5)
        if (tick < 5) return;

        // Phase 2: Crumble outward (5-35)
        if (tick >= 5 && tick < 35) {
            dissolveProgress = (tick - 5) / 30.0f;
            shrink = 1.0f - dissolveProgress * 0.3f;

            if (tick % 2 == 0) spawnAshParticles();
        }

        // Phase 3: Final dispersal (35-40)
        if (tick >= 35) {
            float fadeProgress = (tick - 35) / 5.0f;
            alpha = 1.0f - fadeProgress;
            shrink = 0.7f - fadeProgress * 0.3f;
            dissolveProgress = 1.0f;

            if (tick % 2 == 0) spawnAshParticles();
        }
    }

    private void spawnAshParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;

        double baseX = snapshot.getX();
        double baseY = snapshot.getY();
        double baseZ = snapshot.getZ();
        float height = snapshot.getHeight();

        for (int i = 0; i < 5; i++) {
            double px = baseX + (client.level.random.nextDouble() - 0.5) * 0.6;
            double py = baseY + client.level.random.nextDouble() * height * shrink;
            double pz = baseZ + (client.level.random.nextDouble() - 0.5) * 0.6;
            client.level.addParticle(ParticleTypes.WHITE_ASH, px, py, pz,
                    (client.level.random.nextDouble() - 0.5) * 0.02,
                    -0.02 - client.level.random.nextDouble() * 0.03,
                    (client.level.random.nextDouble() - 0.5) * 0.02);
        }

        if (tick % 4 == 0) {
            client.level.addParticle(ParticleTypes.ASH,
                    baseX + (client.level.random.nextDouble() - 0.5) * 0.4,
                    baseY + height * 0.5 * shrink,
                    baseZ + (client.level.random.nextDouble() - 0.5) * 0.4,
                    0, 0.03, 0);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        String[] parts = {"body", "right_arm", "left_arm", "right_leg", "left_leg", "head"};
        float[] dissolveThreshold = {0.0f, 0.2f, 0.2f, 0.3f, 0.3f, 0.6f};

        for (int i = 0; i < parts.length; i++) {
            if (dissolveProgress >= dissolveThreshold[i] + 0.4f) continue;

            float partAlpha;
            if (dissolveProgress < dissolveThreshold[i]) {
                partAlpha = alpha;
            } else {
                float partDissolve = (dissolveProgress - dissolveThreshold[i]) / 0.4f;
                partAlpha = alpha * (1.0f - partDissolve);
            }

            if (partAlpha < 0.01f) continue;

            AnimatedFragment frag = new AnimatedFragment(parts[i]);
            frag.scale(shrink);
            frag.alpha(partAlpha);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light);
        }
    }
}
