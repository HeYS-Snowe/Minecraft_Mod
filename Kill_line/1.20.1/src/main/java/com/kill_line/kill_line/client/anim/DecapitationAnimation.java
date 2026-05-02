package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.math.Easing;
import com.kill_line.animation.client.math.Interpolator;
import com.kill_line.animation.client.math.PhysicsConstants;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.client.render.RenderHelper;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Decapitation: neck flash -> head launches with easeOutBack overshoot + multi-bounce ->
 * body sway with exponential decay sine -> staggered collapse -> fade
 */
public class DecapitationAnimation extends AbstractDeathAnimation {

    private final AnimatedFragment headFrag;
    private float bodyRotZ = 0;
    private float bodySwayAngle = 0;
    private float bodyOffsetY = 0;
    private float alpha = 1.0f;

    private int bounceCount = 0;
    private boolean headLaunched = false;

    // Interpolators
    private final Interpolator flashInterp = Interpolator.of(Easing::easeOutExpo, 0, 4);
    private final Interpolator swayDecayInterp = Interpolator.of(Easing::linear, 5, 30);
    private final Interpolator collapseInterp = Interpolator.of(Easing::easeInCubic, 20, 35);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeOutCubic, 40, 50);

    public DecapitationAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());

        String headName = snapshot.hasStandardBipedParts() ? "head" : snapshot.findHeadChildName();
        headFrag = new AnimatedFragment(headName);
        headFrag.groundY(-0.3f);
        headFrag.trailEnabled(true);
        headFrag.physics(
                PhysicsConstants.GRAVITY_NORMAL,
                0.4,  // first bounce is high
                PhysicsConstants.DRAG_STANDARD,
                PhysicsConstants.ANGULAR_DRAG_STANDARD
        );
    }

    @Override
    protected void onTick() {
        // Phase 1: Flash (0-4), no movement
        if (tick < 5) return;

        // Launch head with easeOutBack overshoot on tick 5
        if (!headLaunched) {
            headLaunched = true;
            float dirX = snapshot.getDirectionX() == 0 ? 1.0f : snapshot.getDirectionX();
            float dirZ = snapshot.getDirectionZ();

            // Initial velocity
            headFrag.velocity(dirX * 0.15, 0.20, dirZ * 0.15);
            headFrag.angularVelocity(0.15f, 0.08f, 0.1f);

            // Adjust bounce factor for successive bounces
            headFrag.bounceFactor((float) PhysicsConstants.BOUNCE_HIGH);
        }

        // Update head physics
        headFrag.tick();

        // Reduce bounce factor after each bounce
        if (headFrag.getVelocityY() > 0 && headFrag.getOffsetY() <= headFrag.getGroundY() + 0.01 && bounceCount == 0) {
            bounceCount++;
            headFrag.bounceFactor(0.15f);
            spawnBounceParticles();
        }

        // Body sway: exponential decay sine wave
        if (tick >= 5 && tick < 30) {
            float decay = 1.0f - swayDecayInterp.getProgress(tick);
            bodySwayAngle = (float) Math.sin(tick * 0.5f) * 0.15f * Easing.easeOutCubic(decay);
        }

        // Body collapse: staggered tilt
        if (tick >= 20) {
            bodyRotZ = collapseInterp.getProgress(tick) * 0.6f;
            bodyOffsetY = -collapseInterp.getProgress(tick) * 0.1f;
        }

        // Fade
        if (tick >= 40) {
            alpha = 1.0f - fadeInterp.getProgress(tick);
            headFrag.alpha(alpha);
        }
    }

    private void spawnBounceParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        double hx = snapshot.getX() + headFrag.getOffsetX();
        double hy = snapshot.getY() + headFrag.getOffsetY();
        double hz = snapshot.getZ() + headFrag.getOffsetZ();
        for (int i = 0; i < 5; i++) {
            client.level.addParticle(ParticleTypes.CRIT,
                    hx + (client.level.random.nextDouble() - 0.5) * 0.3,
                    hy,
                    hz + (client.level.random.nextDouble() - 0.5) * 0.3,
                    (client.level.random.nextDouble() - 0.5) * 0.1,
                    0.05 + client.level.random.nextDouble() * 0.05,
                    (client.level.random.nextDouble() - 0.5) * 0.1);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Neck flash line
        if (tick < 5) {
            float flashP = flashInterp.getProgress(tick, tickDelta);
            float flashAlpha = flashP * (1.0f - flashP) * 4;
            if (flashAlpha > 0.01f) {
                int flashColor = ((int)(flashAlpha * 220) << 24) | 0xFFCCCC;
                float neckY = snapshot.getHeight() * 0.82f;
                RenderHelper.renderGlowLine(poseStack, consumers,
                        -0.3f, neckY, 0, 0.3f, neckY, 0,
                        flashColor, 0.02f);
            }
        }

        // Render head with trail
        headFrag.alpha(alpha);
        FragmentRenderer.renderFragmentWithTrail(snapshot, headFrag, poseStack, consumers,
                LightTexture.FULL_BRIGHT, tickDelta,
                ((int)(alpha * 120) << 24) | 0xFFCCCC, 0.1f, 0.01f);

        // Render body
        if (!snapshot.hasStandardBipedParts()) {
            java.util.List<String> allChildren = snapshot.getAllChildNames();
            String headName = snapshot.findHeadChildName();
            for (String childName : allChildren) {
                if (childName.equals(headName)) continue;
                AnimatedFragment frag = new AnimatedFragment(childName);
                frag.offset(0, bodyOffsetY, 0);
                frag.rotation(0, 0, bodyRotZ + bodySwayAngle);
                frag.alpha(alpha);
                FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
            }
            return;
        }

        String[] bodyParts = {"body", "right_arm", "left_arm", "right_leg", "left_leg"};
        for (String partName : bodyParts) {
            AnimatedFragment frag = new AnimatedFragment(partName);

            frag.offset(0, bodyOffsetY, 0);

            // Staggered collapse: arms lag behind body
            if ("right_arm".equals(partName) || "left_arm".equals(partName)) {
                frag.rotation(bodyRotZ * 0.7f, 0, bodySwayAngle * 0.8f);
            } else {
                frag.rotation(0, 0, bodyRotZ + bodySwayAngle);
            }

            frag.alpha(alpha);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
        }
    }
}
