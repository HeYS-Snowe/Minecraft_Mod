package com.kill_line.animation.client;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.EntitySnapshot;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Renders all active death animations each frame.
 * Tick via ClientTickEvents (20 TPS), render via WorldRenderEvents.
 */
public class DeathAnimationRenderer {

    public static void register() {
        // Tick animations at proper tick rate (20 TPS), NOT per frame
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DeathAnimationManager.getInstance().tick();
        });

        // Render animations after entities are drawn
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            for (AbstractDeathAnimation animation : DeathAnimationManager.getInstance().getActiveAnimations()) {
                EntitySnapshot snapshot = animation.getSnapshot();

                // Skip if snapshot has no position data
                if (snapshot == null) continue;

                // Calculate camera-relative position
                var camera = context.camera();
                double camX = camera.getPos().x;
                double camY = camera.getPos().y;
                double camZ = camera.getPos().z;

                MatrixStack matrices = context.matrixStack();
                matrices.push();

                // Translate to entity death position (relative to camera)
                matrices.translate(
                        snapshot.getX() - camX,
                        snapshot.getY() - camY,
                        snapshot.getZ() - camZ
                );

                // Apply entity body yaw rotation
                matrices.multiply(
                        new org.joml.Quaternionf().rotateY(
                                (float) Math.toRadians(180 - snapshot.getBodyYaw())
                        )
                );

                // Let the animation render itself
                VertexConsumerProvider consumers = context.consumers();
                int light = net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE;
                animation.render(matrices, consumers, light, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));

                matrices.pop();
            }
        });
    }
}
