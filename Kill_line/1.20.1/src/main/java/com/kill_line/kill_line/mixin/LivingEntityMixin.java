package com.kill_line.kill_line.mixin;

import com.kill_line.animation.api.AnimationDispatcher;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.mark.MarkManager;
import com.kill_line.kill_line.network.ModNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Unique
    private boolean kill_line$processingKill = false;

    @Unique
    private static final org.slf4j.Logger KILL_LINE_LOGGER = LoggerFactory.getLogger("kill_line");

    @Inject(method = "hurt", at = @At("RETURN"))
    private void kill_line$onDamage(DamageSource source, float amount,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (this.kill_line$processingKill) return;
        if (this.level().isClientSide) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isAlive()) return;

        Entity attacker = source.getEntity();
        if (attacker == null) return;

        // Get Kill Line level from attacker's weapon
        ItemStack weapon = null;
        if (attacker instanceof Player player) {
            weapon = player.getMainHandItem();
        } else if (attacker instanceof LivingEntity livingAttacker) {
            weapon = livingAttacker.getMainHandItem();
        }

        if (weapon == null || weapon.isEmpty()) return;
        int level = ModEnchantments.getKillLineLevel(weapon);
        if (level <= 0) return;

        if (!cir.getReturnValueZ()) return; // damage was not applied

        ServerLevel serverLevel = (ServerLevel) this.level();

        KILL_LINE_LOGGER.info("Kill Line level {} detected on {} hitting {} (HP: {}/{})",
                level, attacker.getName().getString(), self.getName().getString(),
                String.format("%.1f", self.getHealth()), String.format("%.1f", self.getMaxHealth()));

        MarkManager markManager = MarkManager.getInstance();
        UUID targetUuid = self.getUUID();
        UUID attackerUuid = attacker.getUUID();

        float healthPercent = self.getHealth() / self.getMaxHealth();
        float threshold = ModEnchantments.getThreshold(level);

        // Check if target is already marked by this attacker
        MarkManager.MarkEntry existingMark = markManager.getMark(targetUuid);

        if (existingMark == null || !existingMark.attackerId().equals(attackerUuid)) {
            // First attack by this attacker -- apply mark
            markManager.mark(targetUuid, attackerUuid, level);
            ModNetworking.sendMarkApply(serverLevel, self, attacker, level);
            KILL_LINE_LOGGER.info("Mark applied to {} (level {})", self.getName().getString(), level);

            // Also check threshold on first mark
            if (healthPercent <= threshold) {
                ModNetworking.sendThresholdReached(serverLevel, self, attacker);
                KILL_LINE_LOGGER.info("Threshold reached on first mark! HP%: {} <= threshold: {}",
                        String.format("%.2f", healthPercent), String.format("%.2f", threshold));
            }
        } else {
            // Target is already marked by this attacker
            if (healthPercent <= threshold) {
                // Send threshold visual
                ModNetworking.sendThresholdReached(serverLevel, self, attacker);
                KILL_LINE_LOGGER.info("Threshold reached! HP%: {} <= threshold: {}",
                        String.format("%.2f", healthPercent), String.format("%.2f", threshold));

                // Roll for instant kill
                float killChance = ModEnchantments.getKillChance(level);
                float roll = this.random.nextFloat();
                KILL_LINE_LOGGER.info("Kill roll: {} vs chance: {} => {}",
                        String.format("%.3f", roll), String.format("%.3f", killChance), roll < killChance ? "KILL!" : "miss");
                if (roll < killChance) {
                    // Instant kill!
                    this.kill_line$processingKill = true;
                    try {
                        // Calculate attack direction for animation
                        Vec3 direction = attacker.position().subtract(self.position()).normalize();
                        if (direction.lengthSqr() < 0.001) {
                            direction = new Vec3(1, 0, 0);
                        }

                        // Pick random animation from registry
                        List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
                        DeathAnimationType animType = types.isEmpty() ? null : types.get(serverLevel.random.nextInt(types.size()));

                        if (animType != null) {
                            AnimationDispatcher.triggerDeathAnimation(serverLevel, self, animType, direction);
                        }

                        // Send legacy kill effect for backwards compatibility
                        ModNetworking.sendKillEffect(serverLevel, self);

                        self.kill();
                        markManager.removeMark(targetUuid);
                        KILL_LINE_LOGGER.info("Instant kill executed on {}", self.getName().getString());
                    } finally {
                        this.kill_line$processingKill = false;
                    }
                }
            }
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void kill_line$onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (this.level().isClientSide) return;

        MarkManager.getInstance().removeMark(this.getUUID());

        // If already processing an instant kill, animation was already triggered
        if (this.kill_line$processingKill) return;

        // Trigger death animation if killed by a Kill Line weapon (normal damage kill)
        LivingEntity self = (LivingEntity) (Object) this;
        Entity attacker = damageSource.getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        ItemStack weapon = livingAttacker.getMainHandItem();
        ServerLevel serverLevel = (ServerLevel) this.level();
        int level = ModEnchantments.getKillLineLevel(weapon);
        if (level <= 0) return;

        // Calculate attack direction
        Vec3 direction = attacker.position().subtract(self.position()).normalize();
        if (direction.lengthSqr() < 0.001) {
            direction = new Vec3(1, 0, 0);
        }

        // Pick random animation
        List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
        if (!types.isEmpty()) {
            DeathAnimationType animType = types.get(serverLevel.random.nextInt(types.size()));
            AnimationDispatcher.triggerDeathAnimation(serverLevel, self, animType, direction);
        }

        ModNetworking.sendKillEffect(serverLevel, self);
    }
}
