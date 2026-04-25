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
 * 升华：缓慢上升 → 脚到头化为金白光粒消散
 */
public class AscensionAnimation extends AbstractDeathAnimation {

    private float riseOffsetY = 0;
    private float dissolveY = 0; // how far up the dissolve has reached (0 = feet, 1 = head)
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

            // Spawn golden particles below the dissolve line
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
        var client = MinecraftClient.getInstance();
        if (client.world == null) return;

        double baseX = snapshot.getX();
        double baseY = snapshot.getY() + riseOffsetY;
        double baseZ = snapshot.getZ();

        // Spawn golden particles at the dissolve boundary
        float height = snapshot.getHeight();
        double dissolveWorldY = baseY + dissolveY * height;

        for (int i = 0; i < 3; i++) {
            double px = baseX + (client.world.random.nextDouble() - 0.5) * 0.5;
            double py = dissolveWorldY + client.world.random.nextDouble() * 0.3;
            double pz = baseZ + (client.world.random.nextDouble() - 0.5) * 0.5;
            client.world.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.05, 0);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        String[] parts = {"right_leg", "left_leg", "body", "right_arm", "left_arm", "head"};
        float height = snapshot.getHeight();

        // Dissolve threshold in model space (0 = bottom, ~1.0 = top)
        // Legs are bottom half, body+arms are middle, head is top
        float[] partMinY = {0.0f, 0.0f, 0.4f, 0.4f, 0.4f, 0.8f};
        float[] partMaxY = {0.4f, 0.4f, 0.8f, 0.8f, 0.8f, 1.0f};

        for (int i = 0; i < parts.length; i++) {
            // Skip parts that are fully dissolved
            if (partMaxY[i] <= dissolveY) continue;

            // Calculate alpha for this part based on dissolve progress
            float partAlpha;
            if (partMinY[i] >= dissolveY) {
                partAlpha = alpha; // not yet reached by dissolve
            } else {
                // Partially dissolved — fade based on how much of the part is dissolved
                float dissolvedFraction = (dissolveY - partMinY[i]) / (partMaxY[i] - partMinY[i]);
                partAlpha = alpha * (1.0f - dissolvedFraction);
            }

            if (partAlpha < 0.01f) continue;

            AnimatedFragment frag = new AnimatedFragment(parts[i]);
            frag.offset(0, riseOffsetY, 0);
            frag.alpha(partAlpha);
            FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers,
                    net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE); // glow
        }
    }
}
