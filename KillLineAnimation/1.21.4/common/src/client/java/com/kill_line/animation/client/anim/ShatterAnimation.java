package com.kill_line.animation.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Built-in shatter animation: entity explodes into body parts that fly outward.
 */
public class ShatterAnimation extends AbstractDeathAnimation {

    private static final String[] BIPED_PARTS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final String FALLBACK_PART = "root";

    private final List<AnimatedFragment> fragments = new ArrayList<>();
    private final Random random = new Random();

    public ShatterAnimation(EntitySnapshot snapshot, com.kill_line.animation.api.DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
        initFragments();
    }

    private void initFragments() {
        // Create fragments for each body part
        String[] parts = BIPED_PARTS;

        // Check if model has biped parts; fall back to whole model if not
        if (snapshot.hasModelData()) {
            boolean hasBipedParts = false;
            for (String part : parts) {
                try {
                    if (snapshot.getRootPart().hasChild(part)) {
                        hasBipedParts = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            if (!hasBipedParts) {
                parts = new String[]{FALLBACK_PART};
            }
        } else {
            parts = new String[]{FALLBACK_PART};
        }

        for (String partName : parts) {
            AnimatedFragment fragment = new AnimatedFragment(partName);

            // Random explosion velocity
            double speed = 0.1 + random.nextDouble() * 0.15;
            double angle = random.nextDouble() * Math.PI * 2;
            double upSpeed = 0.1 + random.nextDouble() * 0.15;

            fragment.velocity(
                    Math.cos(angle) * speed,
                    upSpeed,
                    Math.sin(angle) * speed
            );

            // Random angular velocity
            fragment.angularVelocity(
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f
            );

            fragment.groundY(-0.5f); // fragments can fall slightly below ground

            fragments.add(fragment);
        }
    }

    @Override
    protected void onTick() {
        // Phase 1: Flash (ticks 0-3) — no movement
        // Phase 2: Explode (ticks 3-25)
        // Phase 3: Fade (ticks 25-35)

        if (tick < 3) return; // Pause for dramatic effect

        for (AnimatedFragment fragment : fragments) {
            fragment.tick();

            // Fade out in final phase
            if (tick > 25) {
                float fadeProgress = (float) (tick - 25) / (duration - 25);
                fragment.alpha(1.0f - fadeProgress);
                fragment.scale(Math.max(0.01f, 1.0f - fadeProgress * 0.5f));
            }
        }

        // Spawn particles during explosion phase
        if (tick >= 3 && tick <= 15 && tick % 2 == 0 && snapshot.getEntityType() != null) {
            spawnParticles();
        }
    }

    private void spawnParticles() {
        // We can't easily spawn world particles from here since we don't have world access
        // Particles will be handled by the consumer mod or a separate particle system
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        if (tick < 3 && !fragments.isEmpty()) {
            // Phase 1: Flash white — render all parts with full brightness
            FragmentRenderer.renderAllFragments(snapshot, fragments, matrices, consumers,
                    net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
        } else {
            FragmentRenderer.renderAllFragments(snapshot, fragments, matrices, consumers, light);
        }
    }
}
