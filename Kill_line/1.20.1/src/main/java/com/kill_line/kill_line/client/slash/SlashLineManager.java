package com.kill_line.kill_line.client.slash;

import com.kill_line.kill_line.enchantment.InvulnerabilityDetector;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for slash lines on invulnerable entities.
 * Tracks which entities have visible slash lines and their trace progress.
 */
public class SlashLineManager {

    private static final SlashLineManager INSTANCE = new SlashLineManager();

    public record SlashLineEntry(int entityId, float progress, long lastTraceTick) {}

    private final Map<Integer, SlashLineEntry> activeLines = new ConcurrentHashMap<>();
    private Vec3 prevLookDir = null;

    public static SlashLineManager getInstance() {
        return INSTANCE;
    }

    private SlashLineManager() {}

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            activeLines.clear();
            prevLookDir = null;
            return;
        }

        Player player = client.player;
        ItemStack weapon = player.getMainHandItem();
        int level = ModEnchantments.getKillLineLevel(weapon);

        // Remove entries for entities that are no longer valid
        activeLines.entrySet().removeIf(entry -> {
            Entity entity = client.level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) return true;
            if (player.distanceToSqr(entity) > ModEnchantments.SLASH_LINE_RANGE * ModEnchantments.SLASH_LINE_RANGE)
                return true;
            return level <= 0 || !InvulnerabilityDetector.isInvulnerable(entity);
        });

        if (level <= 0) {
            prevLookDir = player.getViewVector(1.0f);
            return;
        }

        // Scan nearby entities for slash-eligible targets
        Vec3 playerPos = player.position();
        double range = ModEnchantments.SLASH_LINE_RANGE;
        AABB searchBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        List<LivingEntity> nearby = client.level.getEntitiesOfClass(
                LivingEntity.class, searchBox,
                entity -> entity != player
                        && entity.isAlive()
                        && InvulnerabilityDetector.isInvulnerable(entity)
                        && player.distanceToSqr(entity) <= range * range
        );

        long currentTick = client.level.getGameTime();
        Vec3 currentLookDir = player.getViewVector(1.0f);

        for (LivingEntity entity : nearby) {
            int entityId = entity.getId();

            SlashLineEntry existing = activeLines.get(entityId);
            float currentProgress = existing != null ? existing.progress : 0.0f;

            float newProgress = calculateTraceProgress(player, entity, currentProgress, currentLookDir);

            activeLines.put(entityId, new SlashLineEntry(entityId, newProgress, currentTick));
        }

        prevLookDir = currentLookDir;
    }

    /**
     * Compute slash line endpoints for an entity.
     * Diagonal from upper-left to lower-right, extended beyond bounding box.
     */
    public static Vec3[] computeLineEndpoints(LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        double centerX = (box.minX + box.maxX) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;
        double halfW = (box.maxX - box.minX) / 2;
        double halfD = (box.maxZ - box.minZ) / 2;

        double ext = 2.0;
        Vec3 start = new Vec3(centerX - halfW * ext, box.maxY + 0.15, centerZ - halfD * ext);
        Vec3 end = new Vec3(centerX + halfW * ext, box.minY - 0.15, centerZ + halfD * ext);
        return new Vec3[]{start, end};
    }

    private float calculateTraceProgress(Player player, LivingEntity entity,
                                          float currentProgress, Vec3 currentLookDir) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = currentLookDir.normalize();

        // Step 1: Check if looking at/near the entity
        Vec3 entityCenter = entity.getBoundingBox().getCenter();
        double entityDist = eyePos.distanceTo(entityCenter);

        // Project look direction to entity distance
        Vec3 lookAt = eyePos.add(lookDir.scale(entityDist));
        double proximity = lookAt.distanceTo(entityCenter);

        double tolerance = Math.max(1.5, entity.getBbWidth() * 2.0);
        if (proximity > tolerance) return currentProgress;

        // Step 2: Get slash line direction
        Vec3[] line = computeLineEndpoints(entity);
        Vec3 slashDir = line[1].subtract(line[0]).normalize();

        // Step 3: Sweep detection
        boolean sweeping = false;
        if (prevLookDir != null) {
            Vec3 lookDelta = lookDir.subtract(prevLookDir.normalize());
            double sweepComponent = Math.abs(lookDelta.dot(slashDir));
            sweeping = sweepComponent > 0.0005;
        }

        // Step 4: Directional alignment
        double alignment = Math.abs(lookDir.dot(slashDir));

        // Step 5: Calculate progress advancement
        double speed = ModEnchantments.SLASH_LINE_TRACE_SPEED;
        double proximityFactor = Math.max(0.3, 1.0 - proximity / tolerance);

        if (sweeping) {
            return (float) Math.min(1.0, currentProgress + speed * 2.0 * proximityFactor);
        } else if (alignment > 0.2) {
            return (float) Math.min(1.0, currentProgress + speed * 0.8 * proximityFactor);
        } else if (proximity < tolerance * 0.4) {
            return (float) Math.min(1.0, currentProgress + speed * 0.3);
        }

        return currentProgress;
    }

    public SlashLineEntry getEntry(int entityId) {
        return activeLines.get(entityId);
    }

    public boolean isTraceComplete(int entityId) {
        SlashLineEntry entry = activeLines.get(entityId);
        return entry != null && entry.progress >= 1.0f;
    }

    public void removeLine(int entityId) {
        activeLines.remove(entityId);
    }

    public void clear() {
        activeLines.clear();
    }

    public Map<Integer, SlashLineEntry> getActiveLines() {
        return activeLines;
    }
}
