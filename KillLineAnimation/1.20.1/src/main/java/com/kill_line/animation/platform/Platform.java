package com.kill_line.animation.platform;

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
