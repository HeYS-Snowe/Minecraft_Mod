package com.kill_line.kill_line.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class ModPayloads {

    public record MarkApplyPayload(int targetEntityId, int attackerEntityId, int level) implements CustomPayload {
        public static final CustomPayload.Id<MarkApplyPayload> ID =
                new CustomPayload.Id<>(Identifier.of("kill_line", "mark_apply"));
        public static final PacketCodec<PacketByteBuf, MarkApplyPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, MarkApplyPayload::targetEntityId,
                        PacketCodecs.VAR_INT, MarkApplyPayload::attackerEntityId,
                        PacketCodecs.VAR_INT, MarkApplyPayload::level,
                        MarkApplyPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ThresholdReachedPayload(int entityId, int attackerEntityId) implements CustomPayload {
        public static final CustomPayload.Id<ThresholdReachedPayload> ID =
                new CustomPayload.Id<>(Identifier.of("kill_line", "threshold_reached"));
        public static final PacketCodec<PacketByteBuf, ThresholdReachedPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, ThresholdReachedPayload::entityId,
                        PacketCodecs.VAR_INT, ThresholdReachedPayload::attackerEntityId,
                        ThresholdReachedPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record KillEffectPayload(double x, double y, double z, int entityId) implements CustomPayload {
        public static final CustomPayload.Id<KillEffectPayload> ID =
                new CustomPayload.Id<>(Identifier.of("kill_line", "kill_effect"));
        public static final PacketCodec<PacketByteBuf, KillEffectPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.DOUBLE, KillEffectPayload::x,
                        PacketCodecs.DOUBLE, KillEffectPayload::y,
                        PacketCodecs.DOUBLE, KillEffectPayload::z,
                        PacketCodecs.VAR_INT, KillEffectPayload::entityId,
                        KillEffectPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record MarkRemovePayload(int entityId) implements CustomPayload {
        public static final CustomPayload.Id<MarkRemovePayload> ID =
                new CustomPayload.Id<>(Identifier.of("kill_line", "mark_remove"));
        public static final PacketCodec<PacketByteBuf, MarkRemovePayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, MarkRemovePayload::entityId,
                        MarkRemovePayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record SlashKillPayload(int targetEntityId) implements CustomPayload {
        public static final CustomPayload.Id<SlashKillPayload> ID =
                new CustomPayload.Id<>(Identifier.of("kill_line", "slash_kill"));
        public static final PacketCodec<PacketByteBuf, SlashKillPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.VAR_INT, SlashKillPayload::targetEntityId,
                        SlashKillPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }
}