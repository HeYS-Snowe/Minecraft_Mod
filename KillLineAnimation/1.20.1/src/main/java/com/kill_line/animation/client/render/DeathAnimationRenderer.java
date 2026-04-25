package com.kill_line.animation.client.render;

import com.kill_line.animation.client.DeathAnimationManager;
import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = "kill_line_animation", value = Dist.CLIENT)
public class DeathAnimationRenderer {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DeathAnimationManager.getInstance().tick();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        for (AbstractDeathAnimation animation : DeathAnimationManager.getInstance().getActiveAnimations()) {
            EntitySnapshot snapshot = animation.getSnapshot();
            if (snapshot == null) continue;

            var camera = event.getCamera();
            double camX = camera.getPosition().x;
            double camY = camera.getPosition().y;
            double camZ = camera.getPosition().z;

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            poseStack.translate(
                    snapshot.getX() - camX,
                    snapshot.getY() - camY,
                    snapshot.getZ() - camZ
            );

            // Apply body yaw rotation
            float yawRad = (float) Math.toRadians(180 - snapshot.getBodyYaw());
            poseStack.mulPose(new org.joml.Quaternionf().rotateY(yawRad));

            MultiBufferSource consumers = Minecraft.getInstance().renderBuffers().bufferSource();
            float partialTick = event.getPartialTick();
            animation.render(poseStack, consumers, LightTexture.FULL_BRIGHT, partialTick);

            poseStack.popPose();
        }
    }
}
