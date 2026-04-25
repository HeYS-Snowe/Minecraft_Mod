package com.kill_line.kill_line.client.mark;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientMarkManager {

    private static final ClientMarkManager INSTANCE = new ClientMarkManager();

    public record MarkInfo(int attackerEntityId, int level, boolean thresholdReached) {}

    private final Map<Integer, MarkInfo> marks = new ConcurrentHashMap<>();

    public static ClientMarkManager getInstance() {
        return INSTANCE;
    }

    public void applyMark(int targetEntityId, int attackerEntityId, int level) {
        marks.put(targetEntityId, new MarkInfo(attackerEntityId, level, false));
    }

    public void setThresholdReached(int targetEntityId, int attackerEntityId) {
        MarkInfo existing = marks.get(targetEntityId);
        if (existing != null) {
            marks.put(targetEntityId, new MarkInfo(existing.attackerEntityId, existing.level, true));
        } else {
            marks.put(targetEntityId, new MarkInfo(attackerEntityId, 1, true));
        }
    }

    public void removeMark(int targetEntityId) {
        marks.remove(targetEntityId);
    }

    public MarkInfo getMark(int targetEntityId) {
        return marks.get(targetEntityId);
    }

    public boolean isThresholdReached(int targetEntityId) {
        MarkInfo info = marks.get(targetEntityId);
        return info != null && info.thresholdReached;
    }

    public void clear() {
        marks.clear();
    }
}