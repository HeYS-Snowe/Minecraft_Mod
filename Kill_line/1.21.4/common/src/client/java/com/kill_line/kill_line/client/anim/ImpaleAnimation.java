package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.client.render.FragmentRenderer;
import com.kill_line.animation.api.DeathAnimationType;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

/**
 * 穿刺：幽灵剑穿身 → 半跪低头钉地 → 碎裂
 */
public class ImpaleAnimation extends AbstractDeathAnimation {

    private float bodyOffsetY = 0;
    private float headPitch = 0;
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private float swordAlpha = 0.0f;

    public ImpaleAnimation(EntitySnapshot snapshot, DeathAnimationType type) {
        super(snapshot, type.getDefaultDurationTicks());
    }

    @Override
    protected void onTick() {
        // Phase 1: Swords materialize (0-10)
        if (tick < 10) {
            swordAlpha = tick / 10.0f;
            return;
        }

        // Phase 2: Entity slumps (10-40)
        if (tick >= 10 && tick < 40) {
            float slumpProgress = (tick - 10) / 30.0f;
            float ease = slumpProgress * slumpProgress;
            bodyOffsetY = -ease * 0.5f;
            headPitch = ease * 0.8f;
        }

        // Phase 3: Body cracks and fades (40-60)
        if (tick >= 40) {
            float crackProgress = (tick - 40) / 20.0f;
            alpha = 1.0f - crackProgress;
            scale = Math.max(0.1f, 1.0f - crackProgress * 0.3f);
            swordAlpha = Math.max(0, 1.0f - crackProgress * 2.0f);
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta) {
        // Render entity parts with slump
        String[] allParts = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
        for (String partName : allParts) {
            AnimatedFragment frag = new AnimatedFragment(partName);

            frag.offset(0, bodyOffsetY, 0);

            if ("head".equals(partName)) {
                frag.rotation(headPitch, 0, 0);
            }
            if ("right_arm".equals(partName) || "left_arm".equals(partName)) {
                frag.rotation(headPitch * 0.5f, 0, "right_arm".equals(partName) ? 0.1f : -0.1f);
            }

            frag.scale(scale);
            frag.alpha(alpha);
            FragmentRenderer.renderFragment(snapshot, frag, matrices, consumers, light);
        }

        // Render phantom swords
        if (swordAlpha > 0.01f) {
            renderPhantomSwords(matrices, consumers);
        }
    }

    private void renderPhantomSwords(MatrixStack matrices, VertexConsumerProvider consumers) {
        // Sword positions: {height_offset, forward_offset}
        float[][] swords = {
                {0.3f, 0.0f},    // chest, front
                {-0.2f, 0.5f},   // left shoulder
                {0.1f, -0.4f}    // right hip
        };

        Identifier texture = snapshot.getTexture();
        if (texture == null) return;

        VertexConsumer consumer = consumers.getBuffer(
                net.minecraft.client.render.RenderLayer.getEntityTranslucent(texture));

        for (float[] sword : swords) {
            matrices.push();

            float swordHeight = 1.0f;
            float swordWidth = 0.06f;
            float yPos = sword[0] * snapshot.getHeight() + 0.2f;
            float zOffset = sword[1] * 0.3f;

            matrices.translate(sword[1] * 0.15f, yPos, zOffset);
            matrices.scale(swordWidth, swordHeight, swordWidth);

            int a = (int) (swordAlpha * 0.6f * 255);
            int color = ColorHelper.getArgb(a, 180, 200, 255);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            // Draw a simple quad as the sword blade
            consumer.vertex(matrix, -0.5f, 0.0f, 0.0f).color(color).texture(0.0f, 0.0f)
                    .overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                    .light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 0, 1);
            consumer.vertex(matrix, 0.5f, 0.0f, 0.0f).color(color).texture(1.0f, 0.0f)
                    .overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                    .light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 0, 1);
            consumer.vertex(matrix, 0.5f, 1.0f, 0.0f).color(color).texture(1.0f, 1.0f)
                    .overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                    .light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 0, 1);
            consumer.vertex(matrix, -0.5f, 1.0f, 0.0f).color(color).texture(0.0f, 1.0f)
                    .overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
                    .light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 0, 1);

            matrices.pop();
        }
    }
}
