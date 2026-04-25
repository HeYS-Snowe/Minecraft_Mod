package com.kill_line.animation.client.mixin;

import com.kill_line.animation.client.DeathAnimationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void kill_line_animation$suppressDeathRender(
            Entity entity, double x, double y, double z, float rotationYaw, float tickDelta,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            CallbackInfo ci) {
        if (entity instanceof LivingEntity && DeathAnimationManager.getInstance().isAnimating(DeathAnimationManager.getInstance().getEntityId(entity))) {
            ci.cancel();
        }
    }
}
