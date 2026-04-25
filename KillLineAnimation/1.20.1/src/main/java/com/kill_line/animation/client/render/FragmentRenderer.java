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

public class FragmentRenderer {

    public static void renderFragment(EntitySnapshot snapshot, AnimatedFragment fragment,
                                       PoseStack poseStack, MultiBufferSource consumers, int light) {
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

        poseStack.pushPose();

        poseStack.translate(fragment.getOffsetX(), fragment.getOffsetY(), fragment.getOffsetZ());

        if (fragment.getRotationX() != 0 || fragment.getRotationY() != 0 || fragment.getRotationZ() != 0) {
            poseStack.mulPose(new org.joml.Quaternionf()
                    .rotateX(fragment.getRotationX())
                    .rotateY(fragment.getRotationY())
                    .rotateZ(fragment.getRotationZ()));
        }

        float s = fragment.getScale();
        if (s != 1.0f) poseStack.scale(s, s, s);

        ResourceLocation texture = snapshot.getTexture();
        RenderType renderLayer = fragment.getAlpha() < 1.0f
                ? RenderType.entityTranslucent(texture)
                : RenderType.entityCutout(texture);

        VertexConsumer consumer = consumers.getBuffer(renderLayer);

        targetPart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, fragment.getAlpha());

        poseStack.popPose();
    }

    public static void renderAllFragments(EntitySnapshot snapshot, Iterable<AnimatedFragment> fragments,
                                           PoseStack poseStack, MultiBufferSource consumers, int light) {
        for (AnimatedFragment fragment : fragments) {
            if (fragment.getAlpha() > 0.01f) {
                renderFragment(snapshot, fragment, poseStack, consumers, light);
            }
        }
    }
}
