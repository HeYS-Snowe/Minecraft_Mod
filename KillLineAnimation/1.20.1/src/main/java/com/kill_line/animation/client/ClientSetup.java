package com.kill_line.animation.client;

import com.kill_line.animation.client.anim.BuiltinAnimations;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void onClientSetup(FMLClientSetupEvent event) {
        // BuiltinAnimations now registered in commonSetup (both sides)
        // Client setup only needed for future client-only initialization
    }
}
