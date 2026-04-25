package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;

/**
 * 灰化：伤口开始 → 身体从内向外化为灰色余烬
 */
public class AshAnimation extends AbstractDeathAnimation {

    private float dissolveProgress = 0; // 0 = intact, 1 = fully ashed
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

            // Spawn ash particles
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
        var client = MinecraftClient.getInstance();
        if (client.world == null) return;

        double baseX = snapshot.getX();
        double baseY = snapshot.getY();
        double baseZ = snapshot.getZ();
        float height = snapshot.getHeight();

        // Ash particles fall from the body surface
        for (int i = 0; i < 5; i++) {
            double px = baseX + (client.world.random.nextDouble() - 0.5) * 0.6;
            double py = baseY + client.world.random.nextDouble() * height * shrink;
            double pz = baseZ + (client.world.random.nextDouble() - 0.5) * 0.6;
            client.world.addParticle(ParticleTypes.WHITE_ASH, px, py, pz,
                    (client.world.random.nextDouble() - 0.5) * 0.02,
                    -0.02 - client.world.random.nextDouble() * 0.03,
                    (client.world.random.nextDouble() - 0.5) * 0.02);
        }

        // Some larger ember particles
        if (tick % 4 == 0) {
            client.world.addParticle(ParticleTypes.ASH,
                    baseX + (client.world.random.nextDouble() - 0.5) * 0.4,
                    baseY + height * 0.5 * shrink,
                    baseZ + (client.world.random.nextDouble() - 0.5) * 0.4,
                    0, 0.03, 0);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        // Dissolve from center outward: body first, then limbs, then head
        String[] parts = {"body", "right_arm", "left_arm", "right_leg", "left_leg", "head"};
        float[] dissolveThreshold = {0.0f, 0.2f, 0.2f, 0.3f, 0.3f, 0.6f}; // when each part starts dissolving

        for (int i = 0; i < parts.length; i++) {
            if (dissolveProgress >= dissolveThreshold[i] + 0.4f) continue; // fully dissolved

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
            FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers, light);
        }
    }
}
