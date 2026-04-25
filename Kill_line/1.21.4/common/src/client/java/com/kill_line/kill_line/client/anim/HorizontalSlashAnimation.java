package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 横斩：腰部横切 → 上半身滑落 → 两半碎散
 */
public class HorizontalSlashAnimation extends AbstractDeathAnimation {

    private static final String[] UPPER_PARTS = {"head", "body", "right_arm", "left_arm"};
    private static final String[] LOWER_PARTS = {"right_leg", "left_leg"};

    private final AnimatedFragment upperBody;
    private final AnimatedFragment lowerBody;
    private final List<AnimatedFragment> allFragments = new ArrayList<>();
    private final float slashDirX;
    private final float slashDirZ;

    public HorizontalSlashAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
        this.slashDirX = snapshot.getDirectionX();
        this.slashDirZ = snapshot.getDirectionZ();

        float dirX = slashDirX == 0 && slashDirZ == 0 ? 1.0f : slashDirX;
        float dirZ = slashDirZ;

        upperBody = new AnimatedFragment("body"); // use body as representative part for upper
        lowerBody = new AnimatedFragment("right_leg"); // use a leg part for lower
    }

    @Override
    protected void onTick() {
        float progress = getProgress();

        if (tick < 5) {
            // Phase 1: Slash line appears — no movement yet
            return;
        }

        if (tick < 30) {
            // Phase 2: Upper body slides in attack direction
            float slideProgress = (tick - 5) / 25.0f;
            float slideX = slashDirX * slideProgress * 1.5f;
            float slideZ = slashDirZ * slideProgress * 1.5f;

            upperBody.offset(slideX, -slideProgress * 0.1f, slideZ);
            // Upper body tilts forward/backward
            upperBody.rotation(slideProgress * 1.2f, 0, slashDirX != 0 ? slideProgress * 0.3f : 0);

            // Lower body sinks slightly
            lowerBody.offset(0, -slideProgress * 0.15f, 0);
            lowerBody.rotation(slideProgress * 0.2f, 0, 0);
        }

        // Phase 3: Fade out (ticks 30-40)
        if (tick >= 30) {
            float fadeProgress = (tick - 30) / 10.0f;
            upperBody.alpha(1.0f - fadeProgress);
            upperBody.scale(Math.max(0.1f, 1.0f - fadeProgress * 0.5f));
            lowerBody.alpha(1.0f - fadeProgress);
            lowerBody.scale(Math.max(0.1f, 1.0f - fadeProgress * 0.3f));
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        // Render upper body parts
        for (String partName : UPPER_PARTS) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.offset(upperBody.getOffsetX(), upperBody.getOffsetY(), upperBody.getOffsetZ());
            frag.rotation(upperBody.getRotationX(), upperBody.getRotationY(), upperBody.getRotationZ());
            frag.scale(upperBody.getScale());
            frag.alpha(upperBody.getAlpha());
            FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers, light);
        }

        // Render lower body parts
        for (String partName : LOWER_PARTS) {
            AnimatedFragment frag = new AnimatedFragment(partName);
            frag.offset(lowerBody.getOffsetX(), lowerBody.getOffsetY(), lowerBody.getOffsetZ());
            frag.rotation(lowerBody.getRotationX(), lowerBody.getRotationY(), lowerBody.getRotationZ());
            frag.scale(lowerBody.getScale());
            frag.alpha(lowerBody.getAlpha());
            FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers, light);
        }
    }
}
