package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.math.Easing;
import com.kill_line.animation.client.math.Interpolator;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.client.render.RenderHelper;
import com.kill_line.animation.api.DeathAnimationType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Horizontal slash: flash line at waist -> upper body slides with arc + trail -> both halves fade
 */
public class HorizontalSlashAnimation extends AbstractDeathAnimation {

    private static final String[] UPPER_PARTS = {"head", "body", "right_arm", "left_arm"};
    private static final String[] LOWER_PARTS = {"right_leg", "left_leg"};

    private final AnimatedFragment upperBody;
    private final AnimatedFragment lowerBody;
    private final float slashDirX;
    private final float slashDirZ;

    // Interpolators for smooth animation phases
    private final Interpolator flashInterp = Interpolator.of(Easing::easeOutExpo, 0, 4);
    private final Interpolator slideInterp = Interpolator.of(Easing::easeOutCubic, 5, 30);
    private final Interpolator lowerFallInterp = Interpolator.of(Easing::easeInCubic, 8, 35);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeOutCubic, 30, 40);

    public HorizontalSlashAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
        this.slashDirX = snapshot.getDirectionX();
        this.slashDirZ = snapshot.getDirectionZ();

        upperBody = new AnimatedFragment("body");
        upperBody.trailEnabled(true);
        lowerBody = new AnimatedFragment("right_leg");
    }

    @Override
    protected void onTick() {
        if (tick < 5) {
            // Phase 1: Flash appears (no movement yet)
            return;
        }

        if (tick < 30) {
            // Phase 2: Upper body slides with eased motion + Y arc
            float p = slideInterp.getProgress(tick);

            float slideX = slashDirX * p * 1.5f;
            float slideZ = slashDirZ * p * 1.5f;

            // Parabolic Y arc: rises slightly then falls
            float arcY = -p * 0.1f + p * (1 - p) * 0.3f; // slight lift in middle

            upperBody.offset(slideX, arcY, slideZ);
            // Rotation: lean in slash direction with eased tilt
            upperBody.rotation(p * 1.2f, 0, slashDirX != 0 ? p * 0.3f : 0);

            // Lower body: sinks with easeInCubic (slow then fast, losing stability)
            float lowerP = lowerFallInterp.getProgress(tick);
            lowerBody.offset(0, -lowerP * 0.2f, 0);
            lowerBody.rotation(lowerP * 0.15f, 0, 0);
        }

        // Phase 3: Smooth fade
        if (tick >= 30) {
            float fade = fadeInterp.getProgress(tick);
            upperBody.alpha(1.0f - fade);
            upperBody.scale(Math.max(0.1f, 1.0f - fade * 0.5f));
            lowerBody.alpha(1.0f - fade);
            lowerBody.scale(Math.max(0.1f, 1.0f - fade * 0.3f));
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Slash flash line (tick 0-4)
        if (tick < 5) {
            float flashP = flashInterp.getProgress(tick, tickDelta);
            float flashAlpha = flashP * (1.0f - flashP) * 4; // peak at 0.5
            if (flashAlpha > 0.01f) {
                int flashColor = ((int)(flashAlpha * 220) << 24) | 0xCCDDFF;
                float height = snapshot.getHeight();
                float waistY = height * 0.45f;
                float len = 0.8f;
                float dirX = slashDirX == 0 && slashDirZ == 0 ? 1.0f : slashDirX;
                float dirZ = slashDirZ;
                // Normalize direction
                float len2 = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
                if (len2 > 0) { dirX /= len2; dirZ /= len2; }
                RenderHelper.renderGlowLine(poseStack, consumers,
                        -dirX * len, waistY, -dirZ * len,
                        dirX * len, waistY, dirZ * len,
                        flashColor, 0.03f);
            }
        }

        if (!snapshot.hasStandardBipedParts()) {
            java.util.List<String> children = snapshot.getAllChildNames();
            for (String childName : children) {
                AnimatedFragment frag = new AnimatedFragment(childName);
                frag.offset(upperBody.getLerpedOffsetX(tickDelta) * 0.5f,
                            upperBody.getLerpedOffsetY(tickDelta),
                            upperBody.getLerpedOffsetZ(tickDelta) * 0.5f);
                frag.rotation(upperBody.getLerpedRotationX(tickDelta), 0,
                              upperBody.getLerpedRotationZ(tickDelta));
                frag.scale(upperBody.getScale());
                frag.alpha(upperBody.getAlpha());
                FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
            }
            return;
        }

        // Upper body parts with trail
        for (String partName : UPPER_PARTS) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.offset(upperBody.getLerpedOffsetX(tickDelta),
                        upperBody.getLerpedOffsetY(tickDelta),
                        upperBody.getLerpedOffsetZ(tickDelta));
            frag.rotation(upperBody.getLerpedRotationX(tickDelta),
                          upperBody.getLerpedRotationY(tickDelta),
                          upperBody.getLerpedRotationZ(tickDelta));
            frag.scale(upperBody.getScale());
            frag.alpha(upperBody.getAlpha());
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
        }

        // Upper body trail (faint slash trail)
        if (tick >= 5 && tick < 35 && upperBody.getAlpha() > 0.1f) {
            float trailAlpha = upperBody.getAlpha() * 0.4f;
            int trailColor = ((int)(trailAlpha * 255) << 24) | 0xCCDDEE;
            FragmentRenderer.renderFragmentWithTrail(snapshot, upperBody, poseStack, consumers,
                    light, tickDelta, trailColor, 0.08f, 0.01f);
        }

        // Lower body parts
        for (String partName : LOWER_PARTS) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.offset(lowerBody.getLerpedOffsetX(tickDelta),
                        lowerBody.getLerpedOffsetY(tickDelta),
                        lowerBody.getLerpedOffsetZ(tickDelta));
            frag.rotation(lowerBody.getLerpedRotationX(tickDelta),
                          lowerBody.getLerpedRotationY(tickDelta),
                          lowerBody.getLerpedRotationZ(tickDelta));
            frag.scale(lowerBody.getScale());
            frag.alpha(lowerBody.getAlpha());
            FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, tickDelta);
        }
    }
}
