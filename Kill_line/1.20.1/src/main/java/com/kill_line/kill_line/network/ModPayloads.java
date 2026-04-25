package com.kill_line.kill_line.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * All network packet payloads for Kill_line mod.
 * In 1.20.1 Forge, these are plain records with static encode/decode methods.
 */
public class ModPayloads {

    // S2C: Mark applied to a target
    public record MarkApplyPayload(int targetEntityId, int attackerEntityId, int level) {
        public static void encode(MarkApplyPayload msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.targetEntityId());
            buf.writeVarInt(msg.attackerEntityId());
            buf.writeVarInt(msg.level());
        }

        public static MarkApplyPayload decode(FriendlyByteBuf buf) {
            return new MarkApplyPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }
    }

    // S2C: Target health threshold reached
    public record ThresholdReachedPayload(int entityId, int attackerEntityId) {
        public static void encode(ThresholdReachedPayload msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId());
            buf.writeVarInt(msg.attackerEntityId());
        }

        public static ThresholdReachedPayload decode(FriendlyByteBuf buf) {
            return new ThresholdReachedPayload(buf.readVarInt(), buf.readVarInt());
        }
    }

    // S2C: Kill visual effect at position
    public record KillEffectPayload(double x, double y, double z, int entityId) {
        public static void encode(KillEffectPayload msg, FriendlyByteBuf buf) {
            buf.writeDouble(msg.x());
            buf.writeDouble(msg.y());
            buf.writeDouble(msg.z());
            buf.writeVarInt(msg.entityId());
        }

        public static KillEffectPayload decode(FriendlyByteBuf buf) {
            return new KillEffectPayload(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt());
        }
    }

    // S2C: Mark removed from target
    public record MarkRemovePayload(int entityId) {
        public static void encode(MarkRemovePayload msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId());
        }

        public static MarkRemovePayload decode(FriendlyByteBuf buf) {
            return new MarkRemovePayload(buf.readVarInt());
        }
    }

    // C2S: Slash kill request
    public record SlashKillPayload(int targetEntityId) {
        public static void encode(SlashKillPayload msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.targetEntityId());
        }

        public static SlashKillPayload decode(FriendlyByteBuf buf) {
            return new SlashKillPayload(buf.readVarInt());
        }
    }
}
