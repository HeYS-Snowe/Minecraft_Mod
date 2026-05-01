package com.kill_line.kill_line.enchantment;

import com.kill_line.kill_line.network.ModNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Detects attacks on invulnerable entities BEFORE the damage check.
 * Player.hurt() overrides LivingEntity.hurt() and returns false for creative mode
 * without calling super, so the LivingEntityMixin never fires for creative players.
 * This event fires before any invulnerability check.
 */
@Mod.EventBusSubscriber(modid = "kill_line")
public class InvulnerableAttackHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity target = event.getEntity();
        if (!InvulnerabilityDetector.isInvulnerable(target)) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;

        ItemStack weapon = null;
        if (attacker instanceof Player player) {
            weapon = player.getMainHandItem();
        } else if (attacker instanceof LivingEntity livingAttacker) {
            weapon = livingAttacker.getMainHandItem();
        }

        if (weapon == null || weapon.isEmpty()) return;
        int level = ModEnchantments.getKillLineLevel(weapon);
        if (level <= 0) return;

        ServerLevel serverLevel = (ServerLevel) target.level();
        ModNetworking.sendMarkApply(serverLevel, target, attacker, level);
    }
}
