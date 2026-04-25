package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 斩首：头飞出抛物线 → 无头身体摇晃倒下 → 头落地弹跳
 */
public class DecapitationAnimation extends AbstractDeathAnimation {

    private float headX = 0, headY = 0.5f, headZ = 0;
    private float headVelX, headVelY = 0.18f, headVelZ;
    private float headRotX = 0, headRotZ = 0;
    private float bodyRotZ = 0;
    private float bodySwayAngle = 0;
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private boolean headBounced = false;

    public DecapitationAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
        // Head flies in attack direction
        float dirX = snapshot.getDirectionX() == 0 ? 1.0f : snapshot.getDirectionX();
        float dirZ = snapshot.getDirectionZ();
        headVelX = dirX * 0.12f;
        headVelZ = dirZ * 0.12f;
    }

    @Override
    protected void onTick() {
        if (tick < 5) {
            // Phase 1: Slash at neck
            return;
        }

        // Head physics
        if (tick >= 5) {
            headX += headVelX;
            headY += headVelY;
            headZ += headVelZ;
            headVelY -= 0.008f; // gravity

            headRotX += 0.15f; // head tumbles
            headRotZ += 0.08f;

            // Bounce
            if (headY <= 0 && !headBounced) {
                headY = 0;
                headVelY = 0.06f;
                headVelX *= 0.5f;
                headVelZ *= 0.5f;
                headBounced = true;
            }
            if (headBounced && headY <= 0) {
                headY = 0;
                headVelY = 0;
                headVelX *= 0.8f;
                headVelZ *= 0.8f;
            }
        }

        // Body sway (ticks 5-30)
        if (tick >= 5 && tick < 30) {
            float swayDecay = 1.0f - (tick - 5) / 25.0f;
            bodySwayAngle = (float) Math.sin(tick * 0.4f) * 0.15f * swayDecay;
        }

        // Body collapse (ticks 20-35)
        if (tick >= 20 && tick < 35) {
            float collapseProgress = (tick - 20) / 15.0f;
            bodyRotZ = collapseProgress * 0.6f;
        }

        // Fade (ticks 40-50)
        if (tick >= 40) {
            float fadeProgress = (tick - 40) / 10.0f;
            alpha = 1.0f - fadeProgress;
            scale = Math.max(0.1f, 1.0f - fadeProgress * 0.3f);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        // Render head (detached, flying)
        AnimatedFragment headFrag = new AnimatedFragment("head");
        headFrag.offset(headX, headY, headZ);
        headFrag.rotation(headRotX, 0, headRotZ);
        headFrag.scale(scale);
        headFrag.alpha(alpha);
        FragmentRenderer.renderFragment(snapshot, headFrag, matrices, consumers, light);

        // Render body (swaying/collapsing, no head)
        String[] bodyParts = {"body", "right_arm", "left_arm", "right_leg", "left_leg"};
        for (String partName : bodyParts) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.rotation(0, 0, bodyRotZ + bodySwayAngle);
            frag.scale(scale);
            frag.alpha(alpha);
            FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers, light);
        }
    }
}
