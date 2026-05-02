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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Ice shatter: freeze with blue tint -> crack glow lines -> shatter with ice physics ->
 * melt glow fade
 */
public class IceShatterAnimation extends AbstractDeathAnimation {

    private static final String[] BIPED_PARTS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};

    // Ice blue tint
    private static final int ICE_TINT = 0x9966CCFF;

    private final List<AnimatedFragment> fragments = new ArrayList<>();
    private final List<float[]> crackLines = new ArrayList<>();
    private final Random random = new Random();
    private boolean initialized = false;

    private final Interpolator freezeInterp = Interpolator.of(Easing::easeOutCubic, 0, 10);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeInQuad, 35, 45);

    public IceShatterAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    private void initFragments() {
        if (initialized) return;
        initialized = true;

        if (snapshot.hasModelData()) {
            net.minecraft.client.model.geom.ModelPart root = snapshot.getRootPart();

            List<String> foundParts = new ArrayList<>();
            for (String part : BIPED_PARTS) {
                if (root.hasChild(part)) foundParts.add(part);
            }
            if (foundParts.isEmpty()) {
                Set<String> allChildren = new java.util.LinkedHashSet<>(snapshot.getAllChildNames());
                foundParts.addAll(allChildren);
            }

            for (String partName : foundParts) {
                AnimatedFragment frag = new AnimatedFragment(partName);
                frag.velocity(0, 0, 0);
                frag.angularVelocity(0, 0, 0);
                frag.groundY(-0.3f);
                // Ice physics: heavy gravity, low bounce, high drag
                frag.physics(
                        PhysicsConstants.GRAVITY_HEAVY,        // -0.06
                        PhysicsConstants.BOUNCE_LOW,            // 0.15
                        PhysicsConstants.DRAG_HIGH,              // 0.96
                        PhysicsConstants.ANGULAR_DRAG_FAST,     // 0.93
                        PhysicsConstants.GROUND_FRICTION_LOW    // 0.6 (ice slides)
                );
                fragments.add(frag);
            }

            // Generate crack lines
            for (int i = 0; i < 5; i++) {
                float y = (float) (random.nextDouble() * snapshot.getHeight());
                float angle = (float) (random.nextDouble() * Math.PI * 2);
                float len = 0.2f + random.nextFloat() * 0.4f;
                crackLines.add(new float[]{
                        (float) Math.cos(angle) * len, y, (float) Math.sin(angle) * len,
                        (float) Math.cos(angle + 0.3f + random.nextFloat() * 0.5f) * len,
                        y + random.nextFloat() * 0.1f,
                        (float) Math.sin(angle + 0.3f + random.nextFloat() * 0.5f) * len
                });
            }
        }

        if (fragments.isEmpty()) {
            fragments.add(new AnimatedFragment("root"));
        }
    }

    @Override
    protected void onTick() {
        initFragments();

        // Phase 1: Freeze (0-10)
        if (tick < 10) {
            if (tick % 3 == 0) spawnFreezeParticles();
            return;
        }

        // Phase 2: Cracks appear (10-15)
        if (tick >= 10 && tick < 15) {
            if (tick % 2 == 0) spawnIceParticles();
            return;
        }

        // Phase 3: Shatter (15+)
        if (tick == 15) {
            for (AnimatedFragment frag : fragments) {
                double speed = 0.06 + random.nextDouble() * 0.10;
                double angle = random.nextDouble() * Math.PI * 2;
                double upSpeed = 0.04 + random.nextDouble() * 0.08;

                frag.velocity(Math.cos(angle) * speed, upSpeed, Math.sin(angle) * speed);
                // High angular velocity (ice tumbles), but fast decay
                frag.angularVelocity(
                        (random.nextFloat() - 0.5f) * 0.4f,
                        (random.nextFloat() - 0.5f) * 0.3f,
                        (random.nextFloat() - 0.5f) * 0.4f
                );
            }
        }

        if (tick >= 15 && tick < 35) {
            for (AnimatedFragment frag : fragments) {
                frag.tick();
            }
            if (tick % 3 == 0) spawnIceParticles();
        }

        // Phase 4: Fade with melt glow
        if (tick >= 35) {
            float fadeP = fadeInterp.getProgress(tick);
            for (AnimatedFragment frag : fragments) {
                // Blue glow intensifies before disappearing (melting/evaporating)
                float glowAlpha = fadeP < 0.5f ? fadeP * 2.0f : 2.0f - fadeP * 2.0f;
                frag.alpha(1.0f - fadeP);
                frag.scale(Math.max(0.1f, 1.0f - fadeP * 0.5f));
                // Tint gets more blue/white as it fades (ice melting)
                int meltTint = ((int)(glowAlpha * 100) << 24) | 0xCCDDFF;
                frag.colorOverlay(meltTint);
            }
        }
    }

    private void spawnFreezeParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        double x = snapshot.getX(), y = snapshot.getY() + snapshot.getHeight() * 0.5, z = snapshot.getZ();
        for (int i = 0; i < 3; i++) {
            client.level.addParticle(ParticleTypes.SNOWFLAKE,
                    x + (random.nextDouble() - 0.5) * 0.8,
                    y + (random.nextDouble() - 0.5) * 0.8,
                    z + (random.nextDouble() - 0.5) * 0.8,
                    0, 0.01, 0);
        }
    }

    private void spawnIceParticles() {
        var client = Minecraft.getInstance();
        if (client.level == null) return;
        double x = snapshot.getX(), y = snapshot.getY() + snapshot.getHeight() * 0.5, z = snapshot.getZ();
        for (int i = 0; i < 5; i++) {
            client.level.addParticle(ParticleTypes.SNOWFLAKE,
                    x + (random.nextDouble() - 0.5) * 0.8,
                    y + (random.nextDouble() - 0.5) * 0.8,
                    z + (random.nextDouble() - 0.5) * 0.8,
                    (random.nextDouble() - 0.5) * 0.05, 0.02 + random.nextDouble() * 0.02,
                    (random.nextDouble() - 0.5) * 0.05);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Pre-shatter: render with ice tint and FULL_BRIGHT
        if (tick < 15) {
            float freezeP = freezeInterp.getProgress(tick, tickDelta);
            // Ice tint intensifies as freeze progresses
            int tintAlpha = (int) (freezeP * 0x99);
            int iceTint = (tintAlpha << 24) | 0x66CCFF;

            AnimatedFragment frag = new AnimatedFragment("root");
            frag.colorOverlay(iceTint);
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers,
                    LightTexture.FULL_BRIGHT, tickDelta);

            // Crack lines (tick 10-15)
            if (tick >= 10) {
                int numCracks = Math.min(crackLines.size(), (tick - 10) + 1);
                for (int i = 0; i < numCracks; i++) {
                    float[] cl = crackLines.get(i);
                    int crackColor = 0xCCAADDFF;
                    RenderHelper.renderGlowLine(poseStack, consumers,
                            cl[0], cl[1], cl[2], cl[3], cl[4], cl[5],
                            crackColor, 0.01f);
                }
            }
        } else {
            // Post-shatter: render ice fragments
            FragmentRenderer.renderAllFragments(snapshot, fragments, poseStack, consumers, light, tickDelta);

            // Glow on fragments (melt glow in fade phase)
            if (tick >= 35) {
                float fadeP = fadeInterp.getProgress(tick, tickDelta);
                if (fadeP > 0 && fadeP < 0.8f) {
                    float glowIntensity = fadeP < 0.4f ? fadeP * 2.5f : (0.8f - fadeP) * 2.5f;
                    for (AnimatedFragment frag : fragments) {
                        FragmentRenderer.renderFragmentGlow(snapshot, frag, poseStack, consumers, glowIntensity);
                    }
                }
            }
        }
    }
}
