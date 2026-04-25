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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 冰碎：冻结蓝白 → 裂纹 → 碎成冰晶碎片
 */
public class IceShatterAnimation extends AbstractDeathAnimation {

    private static final String[] BIPED_PARTS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};

    private final List<AnimatedFragment> fragments = new ArrayList<>();
    private final Random random = new Random();
    private boolean initialized = false;

    public IceShatterAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    private void initFragments() {
        if (initialized) return;
        initialized = true;

        for (String partName : BIPED_PARTS) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.velocity(0, 0, 0);
            frag.angularVelocity(0, 0, 0);
            frag.groundY(-0.3f);
            fragments.add(frag);
        }
    }

    @Override
    protected void onTick() {
        initFragments();

        // Phase 1: Freeze (0-10) — entity turns white-blue
        if (tick < 10) return;

        // Phase 2: Cracks appear (10-15)
        if (tick >= 10 && tick < 15) {
            if (tick % 2 == 0) spawnIceParticles();
            return;
        }

        // Phase 3: Shatter (15-35)
        if (tick == 15) {
            // Initialize explosion velocities
            for (AnimatedFragment frag : fragments) {
                double speed = 0.08 + random.nextDouble() * 0.12;
                double angle = random.nextDouble() * Math.PI * 2;
                double upSpeed = 0.05 + random.nextDouble() * 0.1;

                frag.velocity(Math.cos(angle) * speed, upSpeed, Math.sin(angle) * speed);
                frag.angularVelocity(
                        (random.nextFloat() - 0.5f) * 0.2f,
                        (random.nextFloat() - 0.5f) * 0.2f,
                        (random.nextFloat() - 0.5f) * 0.2f
                );
            }
        }

        if (tick >= 15 && tick < 35) {
            for (AnimatedFragment frag : fragments) {
                frag.tick();
            }
            if (tick % 3 == 0) spawnIceParticles();
        }

        // Phase 4: Fade (35-45)
        if (tick >= 35) {
            float fadeProgress = (tick - 35) / 10.0f;
            for (AnimatedFragment frag : fragments) {
                frag.alpha(1.0f - fadeProgress);
                frag.scale(Math.max(0.1f, 1.0f - fadeProgress * 0.5f));
            }
        }
    }

    private void spawnIceParticles() {
        var client = MinecraftClient.getInstance();
        if (client.world == null) return;
        double x = snapshot.getX();
        double y = snapshot.getY() + snapshot.getHeight() * 0.5;
        double z = snapshot.getZ();
        for (int i = 0; i < 4; i++) {
            client.world.addParticle(ParticleTypes.SNOWFLAKE,
                    x + (random.nextDouble() - 0.5) * 0.8,
                    y + (random.nextDouble() - 0.5) * 0.8,
                    z + (random.nextDouble() - 0.5) * 0.8,
                    0, 0.02, 0);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        if (tick < 15) {
            // Pre-shatter: render full model (will look frozen via bright light)
            String[] parts = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
            for (String partName : parts) {
                AnimatedFragment frag = new AnimatedFragment(partName);
                FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers,
                        net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
            }
        } else {
            // Post-shatter: render fragments
            FragmentRenderer.renderAllFragments(snapshot, fragments, matrices, consumers, light);
        }
    }
}
