package com.kill_line.animation.network;

import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.animation.client.DeathAnimationManager;
import com.kill_line.animation.platform.PacketSender;
import com.kill_line.animation.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class AnimationNetworking {

    private static final Logger LOGGER = LoggerFactory.getLogger("kill_line_animation");
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("kill_line_animation", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Register S2C death animation trigger packet
        CHANNEL.registerMessage(packetId++,
                AnimationPayloads.DeathAnimationTriggerPacket.class,
                AnimationPayloads.DeathAnimationTriggerPacket::encode,
                AnimationPayloads.DeathAnimationTriggerPacket::decode,
                AnimationNetworking::handleDeathAnimationTrigger
        );

        // Set up platform packet sender
        Platform.setPacketSender(new PacketSender() {
            @Override
            public void sendDeathAnimation(ServerLevel world, Entity entity,
                                           int animationTypeId, float directionX, float directionZ) {
                float bodyYaw = entity instanceof net.minecraft.world.entity.LivingEntity living
                        ? living.yBodyRot : 0f;

                AnimationPayloads.DeathAnimationTriggerPacket packet =
                        new AnimationPayloads.DeathAnimationTriggerPacket(
                                entity.getId(),
                                entity.getX(), entity.getY(), entity.getZ(),
                                bodyYaw, animationTypeId, directionX, directionZ
                        );

                CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
            }
        });
    }

    private static void handleDeathAnimationTrigger(AnimationPayloads.DeathAnimationTriggerPacket msg,
                                                     Supplier<net.minecraftforge.network.NetworkEvent.Context> contextSupplier) {
        net.minecraftforge.network.NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling
            DeathAnimationType type = DeathAnimationRegistry.fromNetworkId(msg.animationTypeId());
            if (type == null) {
                LOGGER.warn("[AnimationNet] Unknown animation type id: {}", msg.animationTypeId());
                return;
            }

            Minecraft client = Minecraft.getInstance();
            LOGGER.info("[AnimationNet] Received death animation packet: entityId={}, pos=({},{},{}), yaw={}, type={}, hasLevel={}",
                    msg.entityId(),
                    String.format("%.1f", msg.x()), String.format("%.1f", msg.y()), String.format("%.1f", msg.z()),
                    msg.bodyYaw(), type.getId(), client.level != null);

            EntitySnapshot snapshot = EntitySnapshot.fromPacket(msg, client);

            LOGGER.info("[AnimationNet] Snapshot result: hasModelData={}, hasTexture={}, entityId={}",
                    snapshot.hasModelData(), snapshot.getTexture() != null, snapshot.getEntityId());

            Object anim = type.getFactory().create(snapshot, type);
            if (anim instanceof AbstractDeathAnimation deathAnim) {
                DeathAnimationManager.getInstance().startAnimation(snapshot.getEntityId(), deathAnim);
                LOGGER.info("[AnimationNet] Started death animation {} for entity {}", type.getId(), snapshot.getEntityId());
            }
        });
        context.setPacketHandled(true);
    }
}
