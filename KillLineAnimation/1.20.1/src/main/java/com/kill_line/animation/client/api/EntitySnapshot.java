package com.kill_line.animation.client.api;

import com.kill_line.animation.network.AnimationPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
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

    // Cached part identification (computed once at construction)
    private final boolean standardBipedParts;
    private final String headChildName;
    private final java.util.List<String> allChildNames;
    private final java.util.List<String> leftChildNames;
    private final java.util.List<String> rightChildNames;
    private final java.util.List<String> centerChildNames;

    public EntitySnapshot(int entityId, double x, double y, double z, float bodyYaw,
                          int animationTypeId, float directionX, float directionZ,
                          ResourceLocation texture, EntityType<?> entityType,
                          LivingEntityRenderer<?, ?> renderer, EntityModel<?> model,
                          ModelPart rootPart, float scale, float height,
                          boolean standardBipedParts, String headChildName,
                          java.util.List<String> allChildNames,
                          java.util.List<String> leftChildNames,
                          java.util.List<String> rightChildNames,
                          java.util.List<String> centerChildNames) {
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
        this.standardBipedParts = standardBipedParts;
        this.headChildName = headChildName;
        this.allChildNames = allChildNames;
        this.leftChildNames = leftChildNames;
        this.rightChildNames = rightChildNames;
        this.centerChildNames = centerChildNames;
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("KillLineAnimation");

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

        if (entity == null) {
            LOG.info("[Snapshot] Entity {} not found in world (already removed?)", payload.entityId());
        } else if (entity instanceof LivingEntity living) {
            entityType = living.getType();
            height = living.getBbHeight();
            scale = 1.0f;

            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            EntityRenderer<?> rawRenderer = dispatcher.getRenderer(living);

            LOG.info("[Snapshot] {} ({}), renderer={}", entity.getName().getString(),
                    living.getClass().getSimpleName(), rawRenderer.getClass().getSimpleName());

            if (rawRenderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
                renderer = livingRenderer;
                model = livingRenderer.getModel();

                LOG.info("[Snapshot] model={}, modelClass={}", model, model.getClass().getName());

                // Step 1: Try HierarchicalModel.root() directly
                boolean isHierarchical = model instanceof HierarchicalModel<?>;
                if (isHierarchical) {
                    rootPart = ((HierarchicalModel<?>) model).root();
                    LOG.info("[Snapshot] Got root via instanceof HierarchicalModel: {}", rootPart != null);
                }

                // Step 2: If instanceof failed, try root() via reflection
                // (handles Forge classloader issues where instanceof returns false for HumanoidModel subclasses)
                if (rootPart == null) {
                    try {
                        java.lang.reflect.Method rootMethod = null;
                        for (Class<?> c = model.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                            try {
                                rootMethod = c.getDeclaredMethod("root");
                                rootMethod.setAccessible(true);
                                break;
                            } catch (NoSuchMethodException ignored) {}
                        }
                        if (rootMethod != null) {
                            rootPart = (ModelPart) rootMethod.invoke(model);
                            LOG.info("[Snapshot] Got root via reflection root(): {}", rootPart != null);
                        } else {
                            LOG.info("[Snapshot] No root() method found in class hierarchy");
                        }
                    } catch (Exception e) {
                        LOG.warn("[Snapshot] Reflection root() failed", e);
                    }
                }

                // Step 3: Enumerate ALL actual child names from the root ModelPart
                if (rootPart != null) {
                    java.util.Set<String> allChildren = discoverChildNames(rootPart);
                    LOG.info("[Snapshot] Root ALL children: {}", allChildren);

                    // Check biped compatibility
                    java.util.Set<String> bipedMatch = new java.util.LinkedHashSet<>();
                    for (String biped : new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
                        if (allChildren.contains(biped)) bipedMatch.add(biped);
                    }
                    LOG.info("[Snapshot] Biped-compatible children: {}", bipedMatch);
                }

                // Step 4: Last resort - generic field scan for models without root()
                if (rootPart == null) {
                    rootPart = buildSyntheticRoot(model);
                }

                // 1.20.1: getTextureLocation takes entity directly
                try {
                    texture = ((EntityRenderer<LivingEntity>) rawRenderer).getTextureLocation(living);
                } catch (Exception e) {
                    LOG.warn("[Snapshot] Failed to get texture for {}", living.getClass().getSimpleName(), e);
                }
            } else {
                LOG.warn("[Snapshot] Renderer {} is NOT a LivingEntityRenderer", rawRenderer.getClass().getName());
            }

            LOG.info("[Snapshot] result: rootPart={}, texture={}, hasModelData={}",
                    rootPart != null, texture != null,
                    renderer != null && model != null && rootPart != null);
        }

        // Compute part identification once during construction (not per-frame)
        boolean stdBiped = rootPart != null && rootPart.hasChild("head") && rootPart.hasChild("body");
        String headName = "root";
        java.util.List<String> allNames = java.util.Collections.emptyList();
        java.util.List<String> leftNames = java.util.Collections.emptyList();
        java.util.List<String> rightNames = java.util.Collections.emptyList();
        java.util.List<String> centerNames = java.util.Collections.emptyList();

        if (stdBiped) {
            headName = "head";
            // For standard biped, enumerate known part names
            allNames = java.util.Arrays.asList("head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg");
        } else if (rootPart != null) {
            java.util.Set<String> allChildren = discoverChildNames(rootPart);
            if (!allChildren.isEmpty()) {
                // HumanoidModel field order: head(0), hat(1), body(2), rightArm(3), leftArm(4), rightLeg(5), leftLeg(6)
                java.util.List<String> childList = new java.util.ArrayList<>(allChildren);
                allNames = childList;
                headName = childList.get(0); // First child = head
                java.util.List<String> left = new java.util.ArrayList<>();
                java.util.List<String> right = new java.util.ArrayList<>();
                java.util.List<String> center = new java.util.ArrayList<>();
                for (int i = 0; i < childList.size(); i++) {
                    if (i <= 2) center.add(childList.get(i));
                    else if (i == 3 || i == 5) right.add(childList.get(i));
                    else if (i == 4 || i == 6) left.add(childList.get(i));
                }
                leftNames = left;
                rightNames = right;
                centerNames = center;
                LOG.info("[Snapshot] Cached parts: head={}, left={}, right={}, center={}", headName, leftNames, rightNames, centerNames);
            }
        }

        return new EntitySnapshot(
                payload.entityId(), payload.x(), payload.y(), payload.z(), payload.bodyYaw(),
                payload.animationTypeId(), payload.directionX(), payload.directionZ(),
                texture, entityType, renderer, model, rootPart, scale, height,
                stdBiped, headName, allNames, leftNames, rightNames, centerNames
        );
    }

    /**
     * Discover all child names of a ModelPart by finding the Map field by type (not name).
     * This works in production where field names are SRG-remapped.
     */
    public static java.util.Set<String> discoverChildNames(ModelPart part) {
        try {
            for (java.lang.reflect.Field f : ModelPart.class.getDeclaredFields()) {
                if (java.util.Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, ModelPart> children = (java.util.Map<String, ModelPart>) f.get(part);
                    if (children != null) {
                        return new java.util.LinkedHashSet<>(children.keySet());
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("[Snapshot] Failed to discover children", e);
        }
        return java.util.Collections.emptySet();
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

    /** Whether the root ModelPart has standard biped-named children (head, body). Cached at construction. */
    public boolean hasStandardBipedParts() {
        return standardBipedParts;
    }

    /** Get the cached head child name. Computed once at construction. */
    public String findHeadChildName() {
        return headChildName;
    }

    /** Get all cached child names. Computed once at construction. */
    public java.util.List<String> getAllChildNames() {
        return allChildNames;
    }

    /** Get the cached left-side child names. Computed once at construction. */
    public java.util.List<String> findLeftChildNames() {
        return leftChildNames;
    }

    /** Get the cached right-side child names. Computed once at construction. */
    public java.util.List<String> findRightChildNames() {
        return rightChildNames;
    }

    /** Get the cached center child names. Computed once at construction. */
    public java.util.List<String> findCenterChildNames() {
        return centerChildNames;
    }

    /**
     * Last-resort fallback: scan all ModelPart fields and build a synthetic root.
     * Only used for models that don't extend HierarchicalModel.
     * In production, field names are SRG (e.g. f_102936_) so ShatterAnimation
     * falls back to whole-model "root" rendering.
     */
    private static ModelPart buildSyntheticRoot(EntityModel<?> model) {
        java.util.Map<String, ModelPart> children = new java.util.LinkedHashMap<>();

        for (Class<?> clazz = model.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (var field : clazz.getDeclaredFields()) {
                if (field.getType() == ModelPart.class) {
                    try {
                        field.setAccessible(true);
                        ModelPart part = (ModelPart) field.get(model);
                        if (part != null && !children.containsKey(field.getName())) {
                            children.put(field.getName(), part);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        LOG.info("[Snapshot] Generic scan on {}: found {} fields", model.getClass().getSimpleName(), children.size());
        if (children.isEmpty()) return null;
        return new ModelPart(java.util.Collections.emptyList(), children);
    }
}
