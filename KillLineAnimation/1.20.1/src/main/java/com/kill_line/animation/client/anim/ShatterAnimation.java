package com.kill_line.animation.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.math.Easing;
import com.kill_line.animation.client.math.Interpolator;
import com.kill_line.animation.client.math.PhysicsConstants;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Shatter: entity explodes into fragments with improved physics and easing.
 * Heavy parts (head/body) fall faster, light parts fly further.
 */
public class ShatterAnimation extends AbstractDeathAnimation {

    private static final String[] BIPED_PARTS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final String FALLBACK_PART = "root";

    // Heavy parts get different physics
    private static final String[] HEAVY_PARTS = {"head", "body"};

    private final List<AnimatedFragment> fragments = new ArrayList<>();
    private final Random random = new Random();

    // Interpolators
    private final Interpolator freezeInterp = Interpolator.of(Easing::linear, 0, 3);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeOutCubic, 25, 35);

    public ShatterAnimation(EntitySnapshot snapshot, com.kill_line.animation.api.DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
        initFragments();
    }

    private void initFragments() {
        if (snapshot.hasModelData()) {
            ModelPart root = snapshot.getRootPart();

            List<String> foundParts = new ArrayList<>();
            for (String part : BIPED_PARTS) {
                if (root.hasChild(part)) foundParts.add(part);
            }

            if (!foundParts.isEmpty()) {
                for (String partName : foundParts) {
                    fragments.add(createFragment(partName));
                }
                return;
            }

            Set<String> allChildren = new java.util.LinkedHashSet<>(snapshot.getAllChildNames());
            if (!allChildren.isEmpty()) {
                for (String childName : allChildren) {
                    fragments.add(createFragment(childName));
                }
                return;
            }
        }

        fragments.add(createFragment(FALLBACK_PART));
    }

    private AnimatedFragment createFragment(String partName) {
        AnimatedFragment fragment = new AnimatedFragment(partName);

        // Gaussian-like speed distribution using Box-Muller
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();
        double gaussian = Math.sqrt(-2.0 * Math.log(Math.max(u1, 0.0001))) * Math.cos(2.0 * Math.PI * u2);
        double speed = 0.15 + gaussian * 0.08; // mean=0.15, stddev=0.08
        speed = Math.max(0.02, Math.min(speed, 0.4)); // clamp

        double angle = random.nextDouble() * Math.PI * 2;
        double upSpeed = 0.08 + random.nextDouble() * 0.18;

        fragment.velocity(Math.cos(angle) * speed, upSpeed, Math.sin(angle) * speed);

        // Angular velocity correlates with linear speed direction
        float angSpeed = (random.nextFloat() - 0.5f) * 0.4f;
        fragment.angularVelocity(
                angSpeed + (float) (speed * Math.sin(angle) * 0.3),
                (random.nextFloat() - 0.5f) * 0.2f,
                angSpeed + (float) (speed * Math.cos(angle) * 0.3)
        );

        fragment.groundY(-0.5f);

        // Assign physics based on part weight
        if (isHeavyPart(partName)) {
            fragment.physics(
                    PhysicsConstants.GRAVITY_HEAVY,    // -0.06
                    PhysicsConstants.BOUNCE_LOW,        // 0.15
                    PhysicsConstants.DRAG_HIGH,          // 0.96
                    PhysicsConstants.ANGULAR_DRAG_STANDARD // 0.97
            );
        } else {
            fragment.physics(
                    PhysicsConstants.GRAVITY_NORMAL,     // -0.04
                    PhysicsConstants.BOUNCE_STANDARD,    // 0.3
                    PhysicsConstants.DRAG_STANDARD,      // 0.98
                    PhysicsConstants.ANGULAR_DRAG_STANDARD
            );
        }

        return fragment;
    }

    private boolean isHeavyPart(String partName) {
        for (String hp : HEAVY_PARTS) {
            if (hp.equals(partName)) return true;
        }
        return false;
    }

    @Override
    protected void onTick() {
        if (tick < 3) return; // Brief freeze frame

        for (AnimatedFragment fragment : fragments) {
            fragment.tick();

            // Smooth fade out using easing
            if (tick > 25) {
                float fadeProgress = fadeInterp.getProgress(tick);
                fragment.alpha(1.0f - fadeProgress);
                fragment.scale(Math.max(0.01f, 1.0f - fadeProgress * 0.4f));
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        if (tick < 3 && !fragments.isEmpty()) {
            // Freeze frame: render full model with bright light
            FragmentRenderer.renderAllFragments(snapshot, fragments, poseStack, consumers, LightTexture.FULL_BRIGHT, tickDelta);
        } else {
            FragmentRenderer.renderAllFragments(snapshot, fragments, poseStack, consumers, light, tickDelta);
        }
    }
}
