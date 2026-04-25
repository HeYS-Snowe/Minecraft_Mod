package com.kill_line.kill_line;

import com.kill_line.kill_line.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kill_line implements ModInitializer {

    public static final String MOD_ID = Constants.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModNetworking.register();
        LOGGER.info("Kill Line mod initialized");
    }
}
