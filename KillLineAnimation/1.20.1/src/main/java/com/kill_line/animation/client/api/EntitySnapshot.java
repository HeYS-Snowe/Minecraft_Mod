package com.kill_line.animation.client.api;

import com.kill_line.animation.network.AnimationPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;

public class EntitySnapshot {

    private final int entityId;
    private final double x, y, z;
    private final float bodyYaw;
    private final int animationTypeId;
    private final float directionX, directionZ;
    private final ResourceLocation texture;
    private final EntityType<?> entityType;
    private final LivingEntityRenderer<?, ?> renderer;
    private final EntityModel<?> model;
    private final ModelPart rootPart;
    private final float scale;
    private final float height;

    public EntitySnapshot(int entityId, double x, double y, double z, float bodyYaw,
                          int animationTypeId, float directionX, float directionZ,
                          ResourceLocation texture, EntityType<?> entityType,
                          LivingEntityRenderer<?, ?> renderer, EntityModel<?> model,
                          ModelPart rootPart, float scale, float height) {
        this.entityId = entityId;
        this.x = x; this.y = y; this.z = z;
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

    @SuppressWarnings("unchecked")
    public static EntitySnapshot fromPacket(AnimationPayloads.DeathAnimationTriggerPacket payload, Minecraft client) {
        Entity entity = client.level != null ? client.level.getEntity(payload.entityId()) : null;

        ResourceLocation texture = null;
        EntityType<?> entityType = null;
        LivingEntityRenderer<?, ?> renderer = null;
        EntityModel<?> model = null;
        ModelPart rootPart = null;
        float scale = 1.0f;
        float height = 1.8f;

        if (entity instanceof LivingEntity living) {
            entityType = living.getType();
            height = living.getBbHeight();
            scale = 1.0f;

            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            // 1.20.1: EntityRenderer has one type parameter <T extends Entity>
            EntityRenderer<?> rawRenderer = dispatcher.getRenderer(living);

            if (rawRenderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                renderer = livingRenderer;
                model = livingRenderer.getModel();

                // 1.20.1: Get root ModelPart from the model
                // Different MC versions expose model parts differently
                // 1.20.1 HumanoidModel: has head/body/rightArm/etc fields, no root() method
                // 1.21+: has root() method

                // Approach 1: try root() method (1.21+ style)
                try {
                    rootPart = (ModelPart) model.getClass().getMethod("root").invoke(model);
                } catch (Exception e1) {
                    // Approach 2: try mesh() method
                    try {
                        var mesh = model.getClass().getMethod("mesh").invoke(model);
                        if (mesh instanceof ModelPart mp) rootPart = mp;
                    } catch (Exception e2) {
                        // Approach 3: try "root" field
                        try {
                            var field = model.getClass().getDeclaredField("root");
                            field.setAccessible(true);
                            rootPart = (ModelPart) field.get(model);
                        } catch (Exception e3) {
                            // Approach 4: 1.20.1 HumanoidModel - get head's parent as root
                            try {
                                var headField = model.getClass().getField("head");
                                ModelPart head = (ModelPart) headField.get(model);
                                if (head != null) {
                                    // head is a child of the root part, get parent via reflection
                                    // In 1.20.1, ModelPart stores children in parent
                                    // The root is the parent of "head"
                                    // We can get the root by iterating fields for a ModelPart that has children
                                    for (var f : model.getClass().getFields()) {
                                        if (ModelPart.class.isAssignableFrom(f.getType())) {
                                            ModelPart candidate = (ModelPart) f.get(model);
                                            if (candidate != null && candidate.hasChild("head")) {
                                                rootPart = candidate;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e4) {
                                // All approaches failed
                            }
                        }
                    }
                }

                if (rootPart == null) {
                    System.err.println("[KillLineAnimation] Failed to get rootPart from model: " + model.getClass().getName());
                }

                // 1.20.1: getTextureLocation takes entity directly
                try {
                    texture = ((EntityRenderer<LivingEntity>) rawRenderer).getTextureLocation(living);
                } catch (ClassCastException e) {
                    // Fallback for wildcard type issues
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

    public int getEntityId() { return entityId; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getBodyYaw() { return bodyYaw; }
    public int getAnimationTypeId() { return animationTypeId; }
    public float getDirectionX() { return directionX; }
    public float getDirectionZ() { return directionZ; }
    public ResourceLocation getTexture() { return texture; }
    public EntityType<?> getEntityType() { return entityType; }
    public LivingEntityRenderer<?, ?> getRenderer() { return renderer; }
    public EntityModel<?> getModel() { return model; }
    public ModelPart getRootPart() { return rootPart; }
    public float getScale() { return scale; }
    public float getHeight() { return height; }
    public boolean hasModelData() { return renderer != null && model != null && rootPart != null; }
}
