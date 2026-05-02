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
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Vertical split: vertical flash -> eased separation -> gravity tilt -> fade
 */
public class VerticalSplitAnimation extends AbstractDeathAnimation {

    private static final String[] LEFT_PARTS = {"left_arm", "left_leg"};
    private static final String[] RIGHT_PARTS = {"right_arm", "right_leg"};

    // Cut face tint (dark red for exposed cross-section)
    private static final int CUT_TINT = 0x60880000;

    private float leftOffsetX = 0, rightOffsetX = 0;
    private float leftRotZ = 0, rightRotZ = 0;
    private float fallOffsetY = 0;
    private float alpha = 1.0f;
    private float scale = 1.0f;

    private final Interpolator flashInterp = Interpolator.of(Easing::easeOutExpo, 0, 4);
    private final Interpolator splitInterp = Interpolator.of(Easing::easeOutCubic, 5, 35);
    private final Interpolator tiltInterp = Interpolator.of(Easing::easeInQuad, 15, 40);
    private final Interpolator fallInterp = Interpolator.of(Easing::easeInCubic, 25, 40);
    private final Interpolator fadeInterp = Interpolator.of(Easing::easeOutQuad, 35, 45);

    public VerticalSplitAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        if (tick < 5) return;

        // Split motion with easeOutCubic
        if (tick < 35) {
            float p = splitInterp.getProgress(tick);
            leftOffsetX = -p * 0.8f;
            rightOffsetX = p * 0.8f;
        }

        // Gravity tilt (increasing rotation as halves fall)
        if (tick >= 15) {
            float tiltP = tiltInterp.getProgress(tick);
            leftRotZ = tiltP * 0.8f;
            rightRotZ = -tiltP * 0.8f;
        }

        // Y-axis fall
        if (tick >= 25) {
            fallOffsetY = -fallInterp.getProgress(tick) * 0.3f;
        }

        // Fade
        if (tick >= 35) {
            float fadeP = fadeInterp.getProgress(tick);
            alpha = 1.0f - fadeP;
            scale = Math.max(0.1f, 1.0f - fadeP * 0.4f);
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        // Vertical flash line (tick 0-4)
        if (tick < 5) {
            float flashP = flashInterp.getProgress(tick, tickDelta);
            float flashAlpha = flashP * (1.0f - flashP) * 4;
            if (flashAlpha > 0.01f) {
                int flashColor = ((int)(flashAlpha * 220) << 24) | 0xCCDDFF;
                float height = snapshot.getHeight();
                RenderHelper.renderGlowLine(poseStack, consumers,
                        0, 0, 0, 0, height, 0,
                        flashColor, 0.02f);
            }
        }

        // Interpolated values for smooth rendering
        float lOff = (float) net.minecraft.util.Mth.lerp(tickDelta, leftOffsetX - (float)(tick >= 5 && tick < 35 ? getSplitDelta() : 0), leftOffsetX);
        float rOff = (float) net.minecraft.util.Mth.lerp(tickDelta, rightOffsetX - (float)(tick >= 5 && tick < 35 ? getSplitDelta() : 0), rightOffsetX);
        lOff = leftOffsetX; // use current tick value (already smoothed by easing)
        rOff = rightOffsetX;

        if (!snapshot.hasStandardBipedParts()) {
            java.util.List<String> leftParts = snapshot.findLeftChildNames();
            java.util.List<String> rightParts = snapshot.findRightChildNames();
            java.util.List<String> centerParts = snapshot.findCenterChildNames();

            for (String childName : rightParts) {
                renderTintedFragment(childName, rOff, fallOffsetY, 0, 0, 0, rightRotZ, poseStack, consumers, light);
            }
            for (String childName : leftParts) {
                renderTintedFragment(childName, lOff, fallOffsetY, 0, 0, 0, leftRotZ, poseStack, consumers, light);
            }
            // Center parts split with cut tint
            for (String childName : centerParts) {
                renderTintedFragment(childName, lOff * 0.5f, fallOffsetY, 0, 0, 0, leftRotZ * 0.3f, poseStack, consumers, light);
                renderTintedFragment(childName, rOff * 0.5f, fallOffsetY, 0, 0, 0, rightRotZ * 0.3f, poseStack, consumers, light);
            }
            return;
        }

        // Left half
        for (String partName : LEFT_PARTS) {
            renderTintedFragment(partName, lOff, fallOffsetY, 0, 0, 0, leftRotZ, poseStack, consumers, light);
        }
        renderTintedFragment("head", lOff * 0.5f, fallOffsetY, 0, 0, 0, leftRotZ * 0.3f, poseStack, consumers, light);
        renderTintedFragment("body", lOff * 0.7f, fallOffsetY, 0, 0, 0, leftRotZ * 0.5f, poseStack, consumers, light);

        // Right half
        for (String partName : RIGHT_PARTS) {
            renderTintedFragment(partName, rOff, fallOffsetY, 0, 0, 0, rightRotZ, poseStack, consumers, light);
        }
        renderTintedFragment("head", rOff * 0.5f, fallOffsetY, 0, 0, 0, rightRotZ * 0.3f, poseStack, consumers, light);
        renderTintedFragment("body", rOff * 0.7f, fallOffsetY, 0, 0, 0, rightRotZ * 0.5f, poseStack, consumers, light);
    }

    private float getSplitDelta() {
        return 0; // Not needed since we use Interpolator directly
    }

    private void renderTintedFragment(String partName, float offX, float offY, float offZ,
                                       float rotX, float rotY, float rotZ,
                                       PoseStack poseStack, MultiBufferSource consumers, int light) {
        AnimatedFragment frag = new AnimatedFragment(partName);
        frag.offset(offX, offY, offZ);
        frag.rotation(rotX, rotY, rotZ);
        frag.scale(scale);
        frag.alpha(alpha);
        // Apply cut tint for exposed surfaces
        if (tick >= 8) {
            frag.colorOverlay(CUT_TINT);
        }
        FragmentRenderer.renderFragment(snapshot, frag, poseStack, consumers, light, 0);
    }
}
