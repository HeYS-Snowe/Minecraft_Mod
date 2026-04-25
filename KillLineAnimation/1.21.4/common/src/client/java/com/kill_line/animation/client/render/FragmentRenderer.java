package com.kill_line.animation.client.render;

import com.kill_line.animation.client.api.AnimatedFragment;
import com.kill_line.animation.client.api.EntitySnapshot;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Utility for rendering individual model parts as animated fragments.
 */
public class FragmentRenderer {

    /**
     * Render a single model part fragment with custom transform.
     */
    public static void renderFragment(EntitySnapshot snapshot, AnimatedFragment fragment,
                                       MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        if (!snapshot.hasModelData() || snapshot.getTexture() == null) return;

        String partName = fragment.getPartName();
        ModelPart root = snapshot.getRootPart();

        // Get the specific part — use hasChild() to avoid NoSuchElementException
        ModelPart targetPart;
        if ("root".equals(partName)) {
            targetPart = root;
        } else if (root.hasChild(partName)) {
            targetPart = root.getChild(partName);
        } else {
            return; // part not found in this model, skip silently
        }

        matrices.push();

        // Apply fragment offset
        matrices.translate(fragment.getOffsetX(), fragment.getOffsetY(), fragment.getOffsetZ());

        // Apply fragment rotation using Quaternionf
        if (fragment.getRotationX() != 0 || fragment.getRotationY() != 0 || fragment.getRotationZ() != 0) {
            matrices.multiply(new org.joml.Quaternionf()
                    .rotateX(fragment.getRotationX())
                    .rotateY(fragment.getRotationY())
                    .rotateZ(fragment.getRotationZ()));
        }

        // Apply fragment scale
        float s = fragment.getScale();
        if (s != 1.0f) matrices.scale(s, s, s);

        // Get vertex consumer with entity texture
        Identifier texture = snapshot.getTexture();
        RenderLayer renderLayer = RenderLayer.getEntityCutout(texture);

        // Handle alpha by using translucent layer if needed
        if (fragment.getAlpha() < 1.0f) {
            renderLayer = RenderLayer.getEntityTranslucent(texture);
        }

        VertexConsumer consumer = consumers.getBuffer(renderLayer);

        // Render only the target part (render() only draws this part's cuboids + its children)
        targetPart.render(matrices, consumer, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    /**
     * Render all fragments from a snapshot.
     */
    public static void renderAllFragments(EntitySnapshot snapshot, Iterable<AnimatedFragment> fragments,
                                           MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        for (AnimatedFragment fragment : fragments) {
            if (fragment.getAlpha() > 0.01f) {
                renderFragment(snapshot, fragment, matrices, consumers, light);
            }
        }
    }
}
