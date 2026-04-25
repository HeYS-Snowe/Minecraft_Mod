package com.kill_line.kill_line.client.slash;

import com.kill_line.kill_line.enchantment.InvulnerabilityDetector;
import com.kill_line.kill_line.enchantment.ModEnchantments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for slash lines on invulnerable entities.
 * Tracks which entities have visible slash lines and their trace progress.
 *
 * Tracing uses proximity-based detection (is crosshair near entity?)
 * combined with sweep detection (is mouse moving in the slash direction?).
 */
public class SlashLineManager {

    private static final SlashLineManager INSTANCE = new SlashLineManager();

    public record SlashLineEntry(int entityId, float progress, long lastTraceTick) {}

    private final Map<Integer, SlashLineEntry> activeLines = new ConcurrentHashMap<>();
    private Vec3d prevLookDir = null;

    public static SlashLineManager getInstance() {
        return INSTANCE;
    }

    private SlashLineManager() {}

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            activeLines.clear();
            prevLookDir = null;
            return;
        }

        PlayerEntity player = client.player;
        ItemStack weapon = player.getMainHandStack();
        int level = ModEnchantments.getKillLineLevel(client.world.getRegistryManager(), weapon);

        // Remove entries for entities that are no longer valid
        activeLines.entrySet().removeIf(entry -> {
            Entity entity = client.world.getEntityById(entry.getKey());
            if (entity == null || !entity.isAlive()) return true;
            if (player.squaredDistanceTo(entity) > ModEnchantments.SLASH_LINE_RANGE * ModEnchantments.SLASH_LINE_RANGE)
                return true;
            return level <= 0 || !InvulnerabilityDetector.isInvulnerable(entity);
        });

        if (level <= 0) {
            prevLookDir = player.getRotationVec(1.0f);
            return;
        }

        // Scan nearby entities for slash-eligible targets
        Vec3d playerPos = player.getPos();
        double range = ModEnchantments.SLASH_LINE_RANGE;
        Box searchBox = Box.from(playerPos).expand(range);

        List<LivingEntity> nearby = client.world.getEntitiesByClass(
                LivingEntity.class, searchBox,
                entity -> entity != player
                        && entity.isAlive()
                        && InvulnerabilityDetector.isInvulnerable(entity)
                        && player.squaredDistanceTo(entity) <= range * range
        );

        long currentTick = client.world.getTime();
        Vec3d currentLookDir = player.getRotationVec(1.0f);

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
     * Diagonal from upper-left to lower-right, extended beyond bounding box
     * for a clear visible slash mark (~50 degree angle).
     */
    public static Vec3d[] computeLineEndpoints(LivingEntity entity) {
        Box box = entity.getBoundingBox();
        double centerX = (box.minX + box.maxX) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;
        double halfW = (box.maxX - box.minX) / 2;
        double halfD = (box.maxZ - box.minZ) / 2;

        // Extend 2x beyond bounding box corners for a wider diagonal angle
        double ext = 2.0;
        Vec3d start = new Vec3d(centerX - halfW * ext, box.maxY + 0.15, centerZ - halfD * ext);
        Vec3d end = new Vec3d(centerX + halfW * ext, box.minY - 0.15, centerZ + halfD * ext);
        return new Vec3d[]{start, end};
    }

    /**
     * Forgiving trace progress calculation.
     * Uses proximity (is crosshair near entity?) + sweep detection (is mouse moving in slash direction?).
     */
    private float calculateTraceProgress(PlayerEntity player, LivingEntity entity,
                                          float currentProgress, Vec3d currentLookDir) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = currentLookDir.normalize();

        // Step 1: Check if looking at/near the entity
        Vec3d entityCenter = entity.getBoundingBox().getCenter();
        double entityDist = eyePos.distanceTo(entityCenter);

        // Project look direction to entity distance
        Vec3d lookAt = eyePos.add(lookDir.multiply(entityDist));
        double proximity = lookAt.distanceTo(entityCenter);

        // Generous tolerance: at least 1.5 blocks, or 2x entity width
        double tolerance = Math.max(1.5, entity.getWidth() * 2.0);
        if (proximity > tolerance) return currentProgress;

        // Step 2: Get slash line direction
        Vec3d[] line = computeLineEndpoints(entity);
        Vec3d slashDir = line[1].subtract(line[0]).normalize();

        // Step 3: Sweep detection — is the mouse moving along the slash direction?
        // Accept both directions (top-to-bottom AND bottom-to-top)
        boolean sweeping = false;
        if (prevLookDir != null) {
            Vec3d lookDelta = lookDir.subtract(prevLookDir.normalize());
            double sweepComponent = Math.abs(lookDelta.dotProduct(slashDir));
            sweeping = sweepComponent > 0.0005;
        }

        // Step 4: Directional alignment — accept both directions
        double alignment = Math.abs(lookDir.dotProduct(slashDir));

        // Step 5: Calculate progress advancement
        double speed = ModEnchantments.SLASH_LINE_TRACE_SPEED;
        double proximityFactor = Math.max(0.3, 1.0 - proximity / tolerance);

        if (sweeping) {
            // Actively sweeping in slash direction — fast progress
            return (float) Math.min(1.0, currentProgress + speed * 2.0 * proximityFactor);
        } else if (alignment > 0.2) {
            // Looking in roughly the right direction — medium progress
            return (float) Math.min(1.0, currentProgress + speed * 0.8 * proximityFactor);
        } else if (proximity < tolerance * 0.4) {
            // Very close to entity center — slow progress
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
