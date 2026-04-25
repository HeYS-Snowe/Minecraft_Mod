package com.kill_line.kill_line.mark;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MarkManager {

    private static final MarkManager INSTANCE = new MarkManager();

    public record MarkEntry(UUID attackerId, int level, long timestamp) {}

    private final Map<UUID, MarkEntry> marks = new ConcurrentHashMap<>();

    public static MarkManager getInstance() {
        return INSTANCE;
    }

    public void mark(UUID targetId, UUID attackerId, int level) {
        marks.put(targetId, new MarkEntry(attackerId, level, System.currentTimeMillis()));
    }

    public MarkEntry getMark(UUID targetId) {
        return marks.get(targetId);
    }

    public void removeMark(UUID targetId) {
        marks.remove(targetId);
    }

    public boolean isMarked(UUID targetId) {
        return marks.containsKey(targetId);
    }

    public void clear() {
        marks.clear();
    }
}
