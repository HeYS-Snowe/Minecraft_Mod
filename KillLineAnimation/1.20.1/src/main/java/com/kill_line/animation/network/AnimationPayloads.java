package com.kill_line.animation.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class AnimationPayloads {

    public static final ResourceLocation DEATH_ANIMATION_TRIGGER_ID =
            new ResourceLocation("kill_line_animation", "death_animation_trigger");

    public record DeathAnimationTriggerPacket(
            int entityId,
            double x, double y, double z,
            float bodyYaw,
            int animationTypeId,
            float directionX, float directionZ
    ) {
        public static DeathAnimationTriggerPacket decode(FriendlyByteBuf buf) {
            return new DeathAnimationTriggerPacket(
                    buf.readVarInt(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readFloat(), buf.readFloat()
            );
        }

        public static void encode(DeathAnimationTriggerPacket msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId);
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
            buf.writeFloat(msg.bodyYaw);
            buf.writeVarInt(msg.animationTypeId);
            buf.writeFloat(msg.directionX);
            buf.writeFloat(msg.directionZ);
        }
    }
}
