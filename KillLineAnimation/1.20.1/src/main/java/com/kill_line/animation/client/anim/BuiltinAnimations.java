package com.kill_line.animation.client.anim;

import com.kill_line.animation.Constants;
import com.kill_line.animation.api.DeathAnimationFactory;
import com.kill_line.animation.api.DeathAnimationRegistry;
import com.kill_line.animation.api.DeathAnimationType;
import com.kill_line.animation.client.api.EntitySnapshot;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class BuiltinAnimations {

    public static DeathAnimationType SHATTER;

    public static void register() {
        SHATTER = DeathAnimationRegistry.register(
                new ResourceLocation(Constants.MOD_ID, "shatter"),
                35,
                (snapshot, type) -> new ShatterAnimation((EntitySnapshot) snapshot, type)
        );
    }

    public static DeathAnimationType randomType(Random random) {
        var all = DeathAnimationRegistry.getAll();
        return all.get(random.nextInt(all.size()));
    }
}
