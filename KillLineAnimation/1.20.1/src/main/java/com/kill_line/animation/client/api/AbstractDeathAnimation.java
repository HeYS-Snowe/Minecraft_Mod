package com.kill_line.animation.client.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class AbstractDeathAnimation {

    protected final EntitySnapshot snapshot;
    protected final int duration;
    protected int tick = 0;
    protected boolean finished = false;

    protected AbstractDeathAnimation(EntitySnapshot snapshot, int durationTicks) {
        this.snapshot = snapshot;
        this.duration = durationTicks;
    }

    public final void tick() {
        if (finished) return;
        tick++;
        onTick();
        if (tick >= duration) {
            finished = true;
        }
    }

    protected abstract void onTick();

    public abstract void render(PoseStack poseStack, MultiBufferSource consumers, int light, float tickDelta);

    public boolean isFinished() { return finished; }
    public EntitySnapshot getSnapshot() { return snapshot; }
    public float getProgress() { return (float) tick / duration; }
    public int getTick() { return tick; }
    public int getDuration() { return duration; }
}
