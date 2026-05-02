package com.kill_line.animation.client.render;

import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class FragmentRenderer {

    /**
     * Render a single fragment with interpolated position and optional color overlay.
     */
    public static void renderFragment(EntitySnapshot snapshot, AnimatedFragment fragment,
                                       PoseStack poseStack, MultiBufferSource consumers,
                                       int light, float tickDelta) {
        if (!snapshot.hasModelData() || snapshot.getTexture() == null) return;

        String partName = fragment.getPartName();
        ModelPart root = snapshot.getRootPart();

        ModelPart targetPart;
        if ("root".equals(partName)) {
            targetPart = root;
        } else if (root.hasChild(partName)) {
            targetPart = root.getChild(partName);
        } else {
            return;
        }

        // Use interpolated position for smooth rendering
        double offX = fragment.getLerpedOffsetX(tickDelta);
        double offY = fragment.getLerpedOffsetY(tickDelta);
        double offZ = fragment.getLerpedOffsetZ(tickDelta);
        float rotX = fragment.getLerpedRotationX(tickDelta);
        float rotY = fragment.getLerpedRotationY(tickDelta);
        float rotZ = fragment.getLerpedRotationZ(tickDelta);

        poseStack.pushPose();

        poseStack.translate(offX, offY, offZ);

        if (rotX != 0 || rotY != 0 || rotZ != 0) {
            poseStack.mulPose(new org.joml.Quaternionf()
                    .rotateX(rotX)
                    .rotateY(rotY)
                    .rotateZ(rotZ));
        }

        float s = fragment.getScale();
        if (s != 1.0f) poseStack.scale(s, s, s);

        ResourceLocation texture = snapshot.getTexture();
        // Always use translucent for smooth alpha blending (no cutout->translucent jump)
        RenderType renderLayer = RenderType.entityTranslucent(texture);
        VertexConsumer consumer = consumers.getBuffer(renderLayer);

        // Apply color overlay if present
        if (fragment.hasColorOverlay()) {
            consumer = new TintedVertexConsumer(consumer, fragment.getColorOverlay());
        }

        targetPart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    /**
     * Render a fragment with trail effect behind it.
     */
    public static void renderFragmentWithTrail(EntitySnapshot snapshot, AnimatedFragment fragment,
                                                PoseStack poseStack, MultiBufferSource consumers,
                                                int light, float tickDelta,
                                                int trailColor, float maxWidth, float minWidth) {
        // Render the fragment itself
        renderFragment(snapshot, fragment, poseStack, consumers, light, tickDelta);

        // Render trail
        if (fragment.isTrailEnabled() && fragment.getTrailCount() >= 2) {
            // Get entity base position for trail offset
            double baseX = snapshot.getX();
            double baseY = snapshot.getY();
            double baseZ = snapshot.getZ();

            // Trail positions are offsets, need to convert to world-relative for rendering
            double[] worldTrailX = new double[fragment.getTrailCount()];
            double[] worldTrailY = new double[fragment.getTrailCount()];
            double[] worldTrailZ = new double[fragment.getTrailCount()];
            for (int i = 0; i < fragment.getTrailCount(); i++) {
                worldTrailX[i] = fragment.getTrailX()[i];
                worldTrailY[i] = fragment.getTrailY()[i];
                worldTrailZ[i] = fragment.getTrailZ()[i];
            }

            RenderHelper.renderTrail(poseStack, consumers,
                    worldTrailX, worldTrailY, worldTrailZ,
                    fragment.getTrailCount(), trailColor, maxWidth, minWidth);
        }
    }

    /**
     * Render a fragment with a glow halo effect.
     * Renders the fragment at slightly larger scale with low alpha for bloom effect.
     */
    public static void renderFragmentGlow(EntitySnapshot snapshot, AnimatedFragment fragment,
                                           PoseStack poseStack, MultiBufferSource consumers,
                                           float glowIntensity) {
        if (!snapshot.hasModelData() || snapshot.getTexture() == null) return;
        if (fragment.getAlpha() < 0.01f) return;

        String partName = fragment.getPartName();
        ModelPart root = snapshot.getRootPart();

        ModelPart targetPart;
        if ("root".equals(partName)) {
            targetPart = root;
        } else if (root.hasChild(partName)) {
            targetPart = root.getChild(partName);
        } else {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(fragment.getOffsetX(), fragment.getOffsetY(), fragment.getOffsetZ());

        if (fragment.getRotationX() != 0 || fragment.getRotationY() != 0 || fragment.getRotationZ() != 0) {
            poseStack.mulPose(new org.joml.Quaternionf()
                    .rotateX(fragment.getRotationX())
                    .rotateY(fragment.getRotationY())
                    .rotateZ(fragment.getRotationZ()));
        }

        // Slightly larger scale for glow halo
        float glowScale = fragment.getScale() * (1.0f + glowIntensity * 0.15f);
        poseStack.scale(glowScale, glowScale, glowScale);

        ResourceLocation texture = snapshot.getTexture();
        RenderType renderLayer = RenderType.entityTranslucent(texture);
        VertexConsumer consumer = consumers.getBuffer(renderLayer);

        // Apply glow as white tint with low alpha
        int glowAlpha = (int) (fragment.getAlpha() * glowIntensity * 80); // subtle glow
        int glowARGB = (glowAlpha << 24) | 0xFFFFFF;
        consumer = new TintedVertexConsumer(consumer, glowARGB);

        targetPart.render(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    // --- Legacy compatibility methods ---

    /** Legacy render without tickDelta interpolation */
    public static void renderFragment(EntitySnapshot snapshot, AnimatedFragment fragment,
                                       PoseStack poseStack, MultiBufferSource consumers, int light) {
        renderFragment(snapshot, fragment, poseStack, consumers, light, 0.0f);
    }

    /** Render all fragments (legacy compatibility) */
    public static void renderAllFragments(EntitySnapshot snapshot, Iterable<AnimatedFragment> fragments,
                                           PoseStack poseStack, MultiBufferSource consumers, int light) {
        for (AnimatedFragment fragment : fragments) {
            if (fragment.getAlpha() > 0.01f) {
                renderFragment(snapshot, fragment, poseStack, consumers, light, 0.0f);
            }
        }
    }

    /** Render all fragments with tickDelta interpolation */
    public static void renderAllFragments(EntitySnapshot snapshot, Iterable<AnimatedFragment> fragments,
                                           PoseStack poseStack, MultiBufferSource consumers,
                                           int light, float tickDelta) {
        for (AnimatedFragment fragment : fragments) {
            if (fragment.getAlpha() > 0.01f) {
                renderFragment(snapshot, fragment, poseStack, consumers, light, tickDelta);
            }
        }
    }
}
