package com.kill_line.kill_line.network;

import com.kill_line.animation.api.AnimationDispatcher;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.kill_line.enchantment.InvulnerabilityDetector;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.mark.MarkManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ModNetworking {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line");

    public static void register() {
        // Register S2C payloads
        PayloadTypeRegistry.playS2C().register(
                ModPayloads.MarkApplyPayload.ID,
                ModPayloads.MarkApplyPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ModPayloads.ThresholdReachedPayload.ID,
                ModPayloads.ThresholdReachedPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ModPayloads.KillEffectPayload.ID,
                ModPayloads.KillEffectPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ModPayloads.MarkRemovePayload.ID,
                ModPayloads.MarkRemovePayload.CODEC
        );

        // Register C2S payload
        PayloadTypeRegistry.playC2S().register(
                ModPayloads.SlashKillPayload.ID,
                ModPayloads.SlashKillPayload.CODEC
        );

        // Server-side slash kill handler
        ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SlashKillPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                ServerWorld world = player.getServerWorld();

                // 1. Validate entity exists and is alive
                Entity target = world.getEntityById(payload.targetEntityId());
                if (target == null || !target.isAlive()) return;
                if (!(target instanceof LivingEntity livingTarget)) return;

                // 2. Validate player has Kill Line weapon
                ItemStack weapon = player.getMainHandStack();
                int level = ModEnchantments.getKillLineLevel(world, weapon);
                if (level <= 0) return;

                // 3. Validate range
                double distSq = player.squaredDistanceTo(target);
                double maxRange = ModEnchantments.SLASH_LINE_RANGE;
                if (distSq > maxRange * maxRange) return;

                // 4. Validate target is invulnerable (server authority)
                if (!InvulnerabilityDetector.isInvulnerable(target)) return;

                // 5. Execute kill
                Vec3d direction = player.getPos().subtract(target.getPos()).normalize();
                if (direction.lengthSquared() < 0.001) {
                    direction = new Vec3d(1, 0, 0);
                }

                List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
                DeathAnimationType animType = types.isEmpty() ? null
                        : types.get(world.getRandom().nextInt(types.size()));

                if (animType != null) {
                    AnimationDispatcher.triggerDeathAnimation(world, livingTarget, animType, direction);
                }

                sendKillEffect(world, livingTarget);
                livingTarget.kill(world);
                MarkManager.getInstance().removeMark(target.getUuid());

                LOGGER.info("Slash kill: {} executed on {} by {}",
                        target.getName().getString(), livingTarget.getName().getString(),
                        player.getName().getString());
            });
        });
    }

    public static void sendMarkApply(ServerWorld world, Entity target, Entity attacker, int level) {
        if (target instanceof ServerPlayerEntity && attacker instanceof ServerPlayerEntity) {
            ServerPlayNetworking.send((ServerPlayerEntity) attacker,
                    new ModPayloads.MarkApplyPayload(target.getId(), attacker.getId(), level));
        } else {
            for (ServerPlayerEntity player : PlayerLookup.tracking(world, target.getBlockPos())) {
                ServerPlayNetworking.send(player,
                        new ModPayloads.MarkApplyPayload(target.getId(), attacker.getId(), level));
            }
        }
    }

    public static void sendThresholdReached(ServerWorld world, Entity target, Entity attacker) {
        if (target instanceof ServerPlayerEntity && attacker instanceof ServerPlayerEntity) {
            ServerPlayNetworking.send((ServerPlayerEntity) attacker,
                    new ModPayloads.ThresholdReachedPayload(target.getId(), attacker.getId()));
        } else {
            for (ServerPlayerEntity player : PlayerLookup.tracking(world, target.getBlockPos())) {
                ServerPlayNetworking.send(player,
                        new ModPayloads.ThresholdReachedPayload(target.getId(), attacker.getId()));
            }
        }
    }

    public static void sendKillEffect(ServerWorld world, Entity target) {
        ModPayloads.KillEffectPayload payload = new ModPayloads.KillEffectPayload(
                target.getX(), target.getY(), target.getZ(), target.getId()
        );
        for (ServerPlayerEntity player : PlayerLookup.tracking(world, target.getBlockPos())) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendMarkRemove(ServerWorld world, Entity target, Entity attacker) {
        ModPayloads.MarkRemovePayload payload = new ModPayloads.MarkRemovePayload(target.getId());
        if (target instanceof ServerPlayerEntity && attacker instanceof ServerPlayerEntity) {
            ServerPlayNetworking.send((ServerPlayerEntity) attacker, payload);
        } else {
            for (ServerPlayerEntity player : PlayerLookup.tracking(world, target.getBlockPos())) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
