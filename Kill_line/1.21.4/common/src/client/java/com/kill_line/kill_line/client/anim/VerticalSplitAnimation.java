package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 纵裂：头顶到脚底纵切 → 左右两半分开倒下
 */
public class VerticalSplitAnimation extends AbstractDeathAnimation {

    private static final String[] LEFT_PARTS = {"left_arm", "left_leg"};
    private static final String[] RIGHT_PARTS = {"right_arm", "right_leg"};
    // head and body render on both sides, we'll split them

    private float leftOffsetX = 0;
    private float rightOffsetX = 0;
    private float leftRotZ = 0;
    private float rightRotZ = 0;
    private float alpha = 1.0f;
    private float scale = 1.0f;

    public VerticalSplitAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        if (tick < 5) {
            // Phase 1: Vertical slash line
            return;
        }

        if (tick < 35) {
            // Phase 2: Halves separate and tilt outward
            float splitProgress = (tick - 5) / 30.0f;
            float ease = splitProgress * splitProgress; // ease-in

            leftOffsetX = -ease * 0.8f;
            rightOffsetX = ease * 0.8f;

            // Tilt outward (rotate around Z axis)
            leftRotZ = ease * 0.8f;  // tilt left
            rightRotZ = -ease * 0.8f; // tilt right
        }

        // Phase 3: Fade out (ticks 35-45)
        if (tick >= 35) {
            float fadeProgress = (tick - 35) / 10.0f;
            alpha = 1.0f - fadeProgress;
            scale = Math.max(0.1f, 1.0f - fadeProgress * 0.4f);

            // Continue falling
            leftOffsetX -= 0.02f;
            rightOffsetX += 0.02f;
            leftRotZ += 0.05f;
            rightRotZ -= 0.05f;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        // Render left half
        for (String partName : LEFT_PARTS) {
            renderFragment(partName, leftOffsetX, 0, 0, 0, 0, leftRotZ, matrices, consumers, light);
        }
        // Left side of head and body (render as left-shifted)
        renderFragment("head", leftOffsetX * 0.5f, 0, 0, 0, 0, leftRotZ * 0.3f, matrices, consumers, light);
        renderFragment("body", leftOffsetX * 0.7f, 0, 0, 0, 0, leftRotZ * 0.5f, matrices, consumers, light);

        // Render right half
        for (String partName : RIGHT_PARTS) {
            renderFragment(partName, rightOffsetX, 0, 0, 0, 0, rightRotZ, matrices, consumers, light);
        }
        // Right side of head and body
        renderFragment("head", rightOffsetX * 0.5f, 0, 0, 0, 0, rightRotZ * 0.3f, matrices, consumers, light);
        renderFragment("body", rightOffsetX * 0.7f, 0, 0, 0, 0, rightRotZ * 0.5f, matrices, consumers, light);
    }

    private void renderFragment(String partName, float offX, float offY, float offZ,
                                 float rotX, float rotY, float rotZ,
                                 MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        AnimatedFragment frag = new AnimatedFragment(partName);
        frag.offset(offX, offY, offZ);
        frag.rotation(rotX, rotY, rotZ);
        frag.scale(scale);
        frag.alpha(alpha);
        FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers, light);
    }
}
