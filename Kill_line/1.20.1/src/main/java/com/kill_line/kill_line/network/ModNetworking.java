package com.kill_line.kill_line.network;

import com.kill_line.animation.api.AnimationDispatcher;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.kill_line.enchantment.InvulnerabilityDetector;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.mark.MarkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class ModNetworking {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line");
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("kill_line", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // S2C: MarkApplyPayload
        CHANNEL.registerMessage(packetId++,
                ModPayloads.MarkApplyPayload.class,
                ModPayloads.MarkApplyPayload::encode,
                ModPayloads.MarkApplyPayload::decode,
                ModNetworking::handleMarkApply);

        // S2C: ThresholdReachedPayload
        CHANNEL.registerMessage(packetId++,
                ModPayloads.ThresholdReachedPayload.class,
                ModPayloads.ThresholdReachedPayload::encode,
                ModPayloads.ThresholdReachedPayload::decode,
                ModNetworking::handleThresholdReached);

        // S2C: KillEffectPayload
        CHANNEL.registerMessage(packetId++,
                ModPayloads.KillEffectPayload.class,
                ModPayloads.KillEffectPayload::encode,
                ModPayloads.KillEffectPayload::decode,
                ModNetworking::handleKillEffect);

        // S2C: MarkRemovePayload
        CHANNEL.registerMessage(packetId++,
                ModPayloads.MarkRemovePayload.class,
                ModPayloads.MarkRemovePayload::encode,
                ModPayloads.MarkRemovePayload::decode,
                ModNetworking::handleMarkRemove);

        // C2S: SlashKillPayload
        CHANNEL.registerMessage(packetId++,
                ModPayloads.SlashKillPayload.class,
                ModPayloads.SlashKillPayload::encode,
                ModPayloads.SlashKillPayload::decode,
                ModNetworking::handleSlashKill);

        LOGGER.info("Kill Line networking registered");
    }

    // ===== S2C Handlers =====

    private static void handleMarkApply(ModPayloads.MarkApplyPayload msg,
                                         Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            com.kill_line.kill_line.client.mark.ClientMarkManager.getInstance()
                    .applyMark(msg.targetEntityId(), msg.attackerEntityId(), msg.level());
            LOGGER.debug("Client received mark_apply: target={}, attacker={}, level={}",
                    msg.targetEntityId(), msg.attackerEntityId(), msg.level());
        });
        context.setPacketHandled(true);
    }

    private static void handleThresholdReached(ModPayloads.ThresholdReachedPayload msg,
                                                Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            com.kill_line.kill_line.client.mark.ClientMarkManager.getInstance()
                    .setThresholdReached(msg.entityId(), msg.attackerEntityId());
            LOGGER.debug("Client received threshold_reached: entity={}, attacker={}",
                    msg.entityId(), msg.attackerEntityId());
        });
        context.setPacketHandled(true);
    }

    private static void handleKillEffect(ModPayloads.KillEffectPayload msg,
                                          Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            com.kill_line.kill_line.client.render.KillEffectRenderer.spawnKillEffect(
                    msg.x(), msg.y(), msg.z());
            LOGGER.debug("Client received kill_effect: pos=({}, {}, {})",
                    msg.x(), msg.y(), msg.z());
        });
        context.setPacketHandled(true);
    }

    private static void handleMarkRemove(ModPayloads.MarkRemovePayload msg,
                                          Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            com.kill_line.kill_line.client.mark.ClientMarkManager.getInstance()
                    .removeMark(msg.entityId());
            LOGGER.debug("Client received mark_remove: entity={}", msg.entityId());
        });
        context.setPacketHandled(true);
    }

    // ===== C2S Handler: Slash Kill =====

    private static void handleSlashKill(ModPayloads.SlashKillPayload msg,
                                         Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ServerLevel world = player.serverLevel();

            // 1. Validate entity exists and is alive
            Entity target = world.getEntity(msg.targetEntityId());
            if (target == null || !target.isAlive()) return;
            if (!(target instanceof LivingEntity livingTarget)) return;

            // 2. Validate player has Kill Line weapon
            ItemStack weapon = player.getMainHandItem();
            int level = ModEnchantments.getKillLineLevel(weapon);
            if (level <= 0) return;

            // 3. Validate range
            double distSq = player.distanceToSqr(target);
            double maxRange = ModEnchantments.SLASH_LINE_RANGE;
            if (distSq > maxRange * maxRange) return;

            // 4. Validate target is invulnerable (server authority)
            if (!InvulnerabilityDetector.isInvulnerable(target)) return;

            // 5. Execute kill
            Vec3 direction = player.position().subtract(target.position()).normalize();
            if (direction.lengthSqr() < 0.001) {
                direction = new Vec3(1, 0, 0);
            }

            List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
            DeathAnimationType animType = types.isEmpty() ? null
                    : types.get(world.random.nextInt(types.size()));

            if (animType != null) {
                AnimationDispatcher.triggerDeathAnimation(world, livingTarget, animType, direction);
            }

            sendKillEffect(world, livingTarget);
            livingTarget.kill();
            MarkManager.getInstance().removeMark(target.getUUID());

            LOGGER.info("Slash kill: {} executed on {} by {}",
                    target.getName().getString(), livingTarget.getName().getString(),
                    player.getName().getString());
        });
        context.setPacketHandled(true);
    }

    // ===== Send Methods =====

    public static void sendMarkApply(ServerLevel world, Entity target, Entity attacker, int level) {
        ModPayloads.MarkApplyPayload payload = new ModPayloads.MarkApplyPayload(
                target.getId(), attacker.getId(), level);
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), payload);
    }

    public static void sendThresholdReached(ServerLevel world, Entity target, Entity attacker) {
        ModPayloads.ThresholdReachedPayload payload = new ModPayloads.ThresholdReachedPayload(
                target.getId(), attacker.getId());
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), payload);
    }

    public static void sendKillEffect(ServerLevel world, Entity target) {
        ModPayloads.KillEffectPayload payload = new ModPayloads.KillEffectPayload(
                target.getX(), target.getY(), target.getZ(), target.getId());
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), payload);
    }

    public static void sendMarkRemove(ServerLevel world, Entity target, Entity attacker) {
        ModPayloads.MarkRemovePayload payload = new ModPayloads.MarkRemovePayload(target.getId());
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), payload);
    }
}
