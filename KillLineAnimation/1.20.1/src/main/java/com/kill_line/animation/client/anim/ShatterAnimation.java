package com.kill_line.animation.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        String[] parts = BIPED_PARTS;

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
            if (!hasBipedParts) parts = new String[]{FALLBACK_PART};
        } else {
            parts = new String[]{FALLBACK_PART};
        }

        for (String partName : parts) {
            AnimatedFragment fragment = new AnimatedFragment(partName);

            double speed = 0.1 + random.nextDouble() * 0.15;
            double angle = random.nextDouble() * Math.PI * 2;
            double upSpeed = 0.1 + random.nextDouble() * 0.15;

            fragment.velocity(Math.cos(angle) * speed, upSpeed, Math.sin(angle) * speed);
            fragment.angularVelocity(
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f
            );
            fragment.groundY(-0.5f);
            fragments.add(fragment);
        }
    }

    @Override
    protected void onTick() {
        if (tick < 3) return;

        for (AnimatedFragment fragment : fragments) {
            fragment.tick();
            if (tick > 25) {
                float fadeProgress = (float) (tick - 25) / (duration - 25);
                fragment.alpha(1.0f - fadeProgress);
                fragment.scale(Math.max(0.01f, 1.0f - fadeProgress * 0.5f));
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta) {
        if (tick < 3 && !fragments.isEmpty()) {
            FragmentRenderer.renderAllFragments(snapshot, fragments, poseStack, consumers, LightTexture.FULL_BRIGHT);
        } else {
            FragmentRenderer.renderAllFragments(snapshot, fragments, poseStack, consumers, light);
        }
    }
}
