package com.kill_line.animation.client.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

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

    /** Get interpolated progress using tickDelta for smooth rendering */
    public float getInterpolatedProgress(float tickDelta) {
        return Mth.clamp((tick + tickDelta) / (float) duration, 0.0f, 1.0f);
    }

    /** Map current progress to a sub-range [start, end] and clamp to [0, 1] */
    protected float progressInRange(float progress, float start, float end) {
        if (end == start) return 0;
        return Mth.clamp((progress - start) / (end - start), 0.0f, 1.0f);
    }

    /** Map current integer tick to a sub-range [startTick, endTick] and clamp to [0, 1] */
    protected float progressInTickRange(int startTick, int endTick) {
        if (endTick == startTick) return 0;
        return Mth.clamp((float)(tick - startTick) / (endTick - startTick), 0.0f, 1.0f);
    }

    /** Map current tick + tickDelta to a sub-range [startTick, endTick] and clamp to [0, 1] */
    protected float progressInTickRange(int startTick, int endTick, float tickDelta) {
        if (endTick == startTick) return 0;
        return Mth.clamp((tick - startTick + tickDelta) / (float)(endTick - startTick), 0.0f, 1.0f);
    }

    public int getTick() { return tick; }
    public int getDuration() { return duration; }
}
