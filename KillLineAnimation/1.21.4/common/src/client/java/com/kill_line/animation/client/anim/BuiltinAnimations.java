package com.kill_line.animation.client.anim;

import com.kill_line.animation.Constants;
import com.kill_line.animation.api.DeathAnimationFactory;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.animation.client.api.AbstractDeathAnimation;
import com.kill_line.animation.client.api.EntitySnapshot;
import net.minecraft.util.Identifier;

import java.util.Random;

/**
 * Built-in animation registrations for the library.
 */
public class BuiltinAnimations {

    public static DeathAnimationType SHATTER;

    public static void register() {
        SHATTER = DeathAnimationRegistry.register(
                Identifier.of(Constants.MOD_ID, "shatter"),
                35,
                (snapshot, type) -> new ShatterAnimation((EntitySnapshot) snapshot, type)
        );
    }

    /**
     * Pick a random animation type from all registered types.
     */
    public static DeathAnimationType randomType(Random random) {
        var all = DeathAnimationRegistry.getAll();
        return all.get(random.nextInt(all.size()));
    }
}
