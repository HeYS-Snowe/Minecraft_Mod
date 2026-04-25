package com.kill_line.animation.api;

@FunctionalInterface
public interface DeathAnimationFactory {
    Object create(Object snapshot, DeathAnimationType type);
}
