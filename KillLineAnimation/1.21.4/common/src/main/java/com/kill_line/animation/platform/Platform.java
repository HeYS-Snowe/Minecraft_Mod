package com.kill_line.animation.platform;

/**
 * Platform service registry.
 * Initialized by each loader's entry point with platform-specific implementations.
 */
public final class Platform {

    private static PacketSender packetSender;

    private Platform() {}

    public static void setPacketSender(PacketSender sender) {
        packetSender = sender;
    }

    public static PacketSender getPacketSender() {
        if (packetSender == null) {
            throw new IllegalStateException("PacketSender not initialized. Platform entry point must call Platform.setPacketSender() first.");
        }
        return packetSender;
    }
}
