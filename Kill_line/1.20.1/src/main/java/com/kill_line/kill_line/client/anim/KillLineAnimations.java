package com.kill_line.kill_line.client.anim;

import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.animation.client.api.EntitySnapshot;
import com.kill_line.kill_line.Constants;
import net.minecraft.resources.ResourceLocation;

/**
 * Registers all Kill_line death animation types with the animation library.
 */
public class KillLineAnimations {

    public static DeathAnimationType HORIZONTAL_SLASH;
    public static DeathAnimationType VERTICAL_SPLIT;
    public static DeathAnimationType DECAPITATION;
    public static DeathAnimationType IMPALE;
    public static DeathAnimationType ASCENSION;
    public static DeathAnimationType ICE_SHATTER;
    public static DeathAnimationType ASH;

    public static void register() {
        HORIZONTAL_SLASH = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "horizontal_slash"), 40,
                KillLineAnimations::createHorizontalSlash);

        VERTICAL_SPLIT = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "vertical_split"), 45,
                KillLineAnimations::createVerticalSplit);

        DECAPITATION = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "decapitation"), 50,
                KillLineAnimations::createDecapitation);

        IMPALE = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "impale"), 60,
                KillLineAnimations::createImpale);

        ASCENSION = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "ascension"), 50,
                KillLineAnimations::createAscension);

        ICE_SHATTER = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "ice_shatter"), 45,
                KillLineAnimations::createIceShatter);

        ASH = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "ash"), 40,
                KillLineAnimations::createAsh);
    }

    private static Object createHorizontalSlash(Object snapshot, DeathAnimationType type) {
        return new HorizontalSlashAnimation((EntitySnapshot) snapshot, type);
    }

    private static Object createVerticalSplit(Object snapshot, DeathAnimationType type) {
        return new VerticalSplitAnimation((EntitySnapshot) snapshot, type);
    }

    private static Object createDecapitation(Object snapshot, DeathAnimationType type) {
        return new DecapitationAnimation((EntitySnapshot) snapshot, type);
    }

    private static Object createImpale(Object snapshot, DeathAnimationType type) {
        return new ImpaleAnimation((EntitySnapshot) snapshot, type);
    }

    private static Object createAscension(Object snapshot, DeathAnimationType type) {
        return new AscensionAnimation((EntitySnapshot) snapshot, type);
    }

    private static Object createIceShatter(Object snapshot, DeathAnimationType type) {
        return new IceShatterAnimation((EntitySnapshot) snapshot, type);
    }

    private static Object createAsh(Object snapshot, DeathAnimationType type) {
        return new AshAnimation((EntitySnapshot) snapshot, type);
    }
}
