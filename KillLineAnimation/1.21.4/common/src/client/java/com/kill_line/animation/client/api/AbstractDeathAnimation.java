package com.kill_line.animation.client.api;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Abstract base class for all death animations.
 * Consumer mods extend this to implement custom animations.
 */
public abstract class AbstractDeathAnimation {

    protected final EntitySnapshot snapshot;
    protected final int duration;
    protected int tick = 0;
    protected boolean finished = false;

    protected AbstractDeathAnimation(EntitySnapshot snapshot, int durationTicks) {
        this.snapshot = snapshot;
        this.duration = durationTicks;
    }

    /**
     * Called each client tick. Advances time and calls {@link #onTick()}.
     */
    public final void tick() {
        if (finished) return;
        tick++;
        onTick();
        if (tick >= duration) {
            finished = true;
        }
    }

    /**
     * Override to update animation state each tick.
     */
    protected abstract void onTick();

    /**
     * Render the animation. Called each frame.
     *
     * @param matrices  the matrix stack (already translated to entity position relative to camera)
     * @param consumers vertex consumer provider
     * @param light     light level
     * @param tickDelta partial tick delta for interpolation
     */
    public abstract void render(MatrixStack matrices, VertexConsumerProvider consumers, int light, float tickDelta);

    public boolean isFinished() {
        return finished;
    }

    public EntitySnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Animation progress from 0.0 to 1.0.
     */
    public float getProgress() {
        return (float) tick / duration;
    }

    public int getTick() {
        return tick;
    }

    public int getDuration() {
        return duration;
    }
}
