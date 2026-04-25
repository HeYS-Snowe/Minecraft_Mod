package com.kill_line.animation.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class AnimationPayloads {

    /**
     * S2C packet: trigger a death animation on the client.
     */
    public record DeathAnimationTriggerPacket(
            int entityId,
            double x, double y, double z,
            float bodyYaw,
            int animationTypeId,
            float directionX, float directionZ
    ) implements CustomPayload {

        public static final CustomPayload.Id<DeathAnimationTriggerPacket> ID =
                new CustomPayload.Id<>(Identifier.of("kill_line_animation", "death_animation_trigger"));

        public static final PacketCodec<PacketByteBuf, DeathAnimationTriggerPacket> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, DeathAnimationTriggerPacket::entityId,
                        PacketCodecs.DOUBLE, DeathAnimationTriggerPacket::x,
                        PacketCodecs.DOUBLE, DeathAnimationTriggerPacket::y,
                        PacketCodecs.DOUBLE, DeathAnimationTriggerPacket::z,
                        PacketCodecs.FLOAT, DeathAnimationTriggerPacket::bodyYaw,
                        PacketCodecs.VAR_INT, DeathAnimationTriggerPacket::animationTypeId,
                        PacketCodecs.FLOAT, DeathAnimationTriggerPacket::directionX,
                        PacketCodecs.FLOAT, DeathAnimationTriggerPacket::directionZ,
                        DeathAnimationTriggerPacket::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }
}
