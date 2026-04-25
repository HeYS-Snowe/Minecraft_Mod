package com.kill_line.kill_line.mixin;

import com.kill_line.kill_line.enchantment.ModEnchantments;
import com.kill_line.kill_line.mark.MarkManager;
import com.kill_line.kill_line.network.ModNetworking;
import com.kill_line.animation.api.AnimationDispatcher;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private boolean kill_line$processingKill = false;

    @Unique
    private static final org.slf4j.Logger KILL_LINE_LOGGER = LoggerFactory.getLogger("kill_line");

    @Inject(method = "damage", at = @At("RETURN"))
    private void kill_line$onDamage(ServerWorld world, DamageSource source, float amount,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (this.kill_line$processingKill) return;
        if (world.isClient()) return;
        if (!cir.getReturnValueZ()) return; // damage was not applied

        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isAlive()) return; // already dead from this hit

        Entity attacker = source.getAttacker();
        if (attacker == null) return;

        // Get Kill Line level from attacker's weapon
        ItemStack weapon = null;
        if (attacker instanceof PlayerEntity player) {
            weapon = player.getMainHandStack();
        } else if (attacker instanceof LivingEntity livingAttacker) {
            weapon = livingAttacker.getMainHandStack();
        }

        if (weapon == null || weapon.isEmpty()) return;
        int level = ModEnchantments.getKillLineLevel(world, weapon);
        if (level <= 0) return;

        KILL_LINE_LOGGER.info("Kill Line level {} detected on {} hitting {} (HP: {:.1f}/{:.1f})",
                level, attacker.getName().getString(), self.getName().getString(),
                self.getHealth(), self.getMaxHealth());

        MarkManager markManager = MarkManager.getInstance();
        UUID targetUuid = self.getUuid();
        UUID attackerUuid = attacker.getUuid();

        float healthPercent = self.getHealth() / self.getMaxHealth();
        float threshold = ModEnchantments.getThreshold(level);

        // Check if target is already marked by this attacker
        MarkManager.MarkEntry existingMark = markManager.getMark(targetUuid);

        if (existingMark == null || !existingMark.attackerId().equals(attackerUuid)) {
            // First attack by this attacker — apply mark
            markManager.mark(targetUuid, attackerUuid, level);
            ModNetworking.sendMarkApply(world, self, attacker, level);
            KILL_LINE_LOGGER.info("Mark applied to {} (level {})", self.getName().getString(), level);

            // Also check threshold on first mark
            if (healthPercent <= threshold) {
                ModNetworking.sendThresholdReached(world, self, attacker);
                KILL_LINE_LOGGER.info("Threshold reached on first mark! HP%: {:.2f} <= threshold: {:.2f}",
                        healthPercent, threshold);
            }
        } else {
            // Target is already marked by this attacker
            if (healthPercent <= threshold) {
                // Send threshold visual
                ModNetworking.sendThresholdReached(world, self, attacker);
                KILL_LINE_LOGGER.info("Threshold reached! HP%: {:.2f} <= threshold: {:.2f}",
                        healthPercent, threshold);

                // Roll for instant kill
                float killChance = ModEnchantments.getKillChance(level);
                float roll = world.getRandom().nextFloat();
                KILL_LINE_LOGGER.info("Kill roll: {:.3f} vs chance: {:.3f} => {}",
                        roll, killChance, roll < killChance ? "KILL!" : "miss");
                if (roll < killChance) {
                    // Instant kill!
                    this.kill_line$processingKill = true;
                    try {
                        // Calculate attack direction for animation
                        Vec3d direction = attacker.getPos().subtract(self.getPos()).normalize();
                        if (direction.lengthSquared() < 0.001) {
                            direction = new Vec3d(1, 0, 0);
                        }

                        // Pick random animation from registry
                        List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
                        DeathAnimationType animType = types.isEmpty() ? null : types.get(world.getRandom().nextInt(types.size()));

                        if (animType != null) {
                            AnimationDispatcher.triggerDeathAnimation(world, self, animType, direction);
                        }

                        // Also send legacy kill effect for backwards compatibility
                        ModNetworking.sendKillEffect(world, self);

                        self.kill(world);
                        markManager.removeMark(targetUuid);
                        KILL_LINE_LOGGER.info("Instant kill executed on {}", self.getName().getString());
                    } finally {
                        this.kill_line$processingKill = false;
                    }
                }
            }
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void kill_line$onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (this.getWorld().isClient()) return;

        MarkManager.getInstance().removeMark(this.getUuid());

        // If already processing an instant kill, animation was already triggered
        if (this.kill_line$processingKill) return;

        // Trigger death animation if killed by a Kill Line weapon (normal damage kill)
        LivingEntity self = (LivingEntity) (Object) this;
        Entity attacker = damageSource.getAttacker();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        ItemStack weapon = livingAttacker.getMainHandStack();
        ServerWorld world = (ServerWorld) this.getWorld();
        int level = ModEnchantments.getKillLineLevel(world, weapon);
        if (level <= 0) return;

        // Calculate attack direction
        Vec3d direction = attacker.getPos().subtract(self.getPos()).normalize();
        if (direction.lengthSquared() < 0.001) {
            direction = new Vec3d(1, 0, 0);
        }

        // Pick random animation
        List<DeathAnimationType> types = DeathAnimationRegistry.getAll();
        if (!types.isEmpty()) {
            DeathAnimationType animType = types.get(world.getRandom().nextInt(types.size()));
            AnimationDispatcher.triggerDeathAnimation(world, self, animType, direction);
        }

        ModNetworking.sendKillEffect(world, self);
    }
}