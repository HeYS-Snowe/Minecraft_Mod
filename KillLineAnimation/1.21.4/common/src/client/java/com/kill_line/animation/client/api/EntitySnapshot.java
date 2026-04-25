package com.kill_line.animation.client.api;

import com.kill_line.animation.network.AnimationPayloads;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Immutable snapshot of entity visual state at death time.
 * All rendering uses this data — no reference to the entity is held.
 */
public class EntitySnapshot {

    private final int entityId;
    private final double x, y, z;
    private final float bodyYaw;
    private final int animationTypeId;
    private final float directionX, directionZ;
    private final Identifier texture;
    private final EntityType<?> entityType;
    private final LivingEntityRenderer<?, ?, ?> renderer;
    private final EntityModel<?> model;
    private final ModelPart rootPart;
    private final float scale;
    private final float height;

    public EntitySnapshot(int entityId, double x, double y, double z, float bodyYaw,
                          int animationTypeId, float directionX, float directionZ,
                          Identifier texture, EntityType<?> entityType,
                          LivingEntityRenderer<?, ?, ?> renderer, EntityModel<?> model,
                          ModelPart rootPart, float scale, float height) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bodyYaw = bodyYaw;
        this.animationTypeId = animationTypeId;
        this.directionX = directionX;
        this.directionZ = directionZ;
        this.texture = texture;
        this.entityType = entityType;
        this.renderer = renderer;
        this.model = model;
        this.rootPart = rootPart;
        this.scale = scale;
        this.height = height;
    }

    /**
     * Create snapshot from a network packet. Captures entity rendering data while it still exists.
     */
    @SuppressWarnings("unchecked")
    public static EntitySnapshot fromPacket(AnimationPayloads.DeathAnimationTriggerPacket payload, MinecraftClient client) {
        // Find the entity in the world
        Entity entity = client.world != null ? client.world.getEntityById(payload.entityId()) : null;

        Identifier texture = null;
        EntityType<?> entityType = null;
        LivingEntityRenderer<?, ?, ?> renderer = null;
        EntityModel<?> model = null;
        ModelPart rootPart = null;
        float scale = 1.0f;
        float height = 1.8f;

        if (entity instanceof LivingEntity living) {
            entityType = living.getType();
            height = living.getHeight();
            scale = living.getScaleFactor();

            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            EntityRenderer<?, ?> rawRenderer = dispatcher.getRenderer(living);

            if (rawRenderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer) {
                renderer = livingRenderer;
                model = livingRenderer.getModel();
                rootPart = model.getRootPart();

                // Get texture from the renderer via raw type cast to bypass wildcard generics
                try {
                    @SuppressWarnings("rawtypes")
                    LivingEntityRenderer raw = livingRenderer;
                    var state = raw.createRenderState();
                    raw.updateRenderState(living, state, 0.0f);
                    texture = (Identifier) raw.getTexture((net.minecraft.client.render.entity.state.LivingEntityRenderState) state);
                } catch (Exception e) {
                    // Fallback: no texture available
                }
            }
        }

        return new EntitySnapshot(
                payload.entityId(),
                payload.x(), payload.y(), payload.z(),
                payload.bodyYaw(),
                payload.animationTypeId(),
                payload.directionX(), payload.directionZ(),
                texture, entityType, renderer, model, rootPart, scale, height
        );
    }

    // Getters
    public int getEntityId() { return entityId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getBodyYaw() { return bodyYaw; }
    public int getAnimationTypeId() { return animationTypeId; }
    public float getDirectionX() { return directionX; }
    public float getDirectionZ() { return directionZ; }
    public Identifier getTexture() { return texture; }
    public EntityType<?> getEntityType() { return entityType; }
    public LivingEntityRenderer<?, ?, ?> getRenderer() { return renderer; }
    public EntityModel<?> getModel() { return model; }
    public ModelPart getRootPart() { return rootPart; }
    public float getScale() { return scale; }
    public float getHeight() { return height; }
    public boolean hasModelData() { return renderer != null && model != null && rootPart != null; }
}
