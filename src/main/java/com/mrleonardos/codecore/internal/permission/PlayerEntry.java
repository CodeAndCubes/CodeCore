package com.mrleonardos.codecore.internal.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Запись об игроке: его группа и личные правила, которые сильнее групповых. */
public final class PlayerEntry {

    public String group;
    public List<String> nodes = new ArrayList<>();
    public Map<String, String> meta = new LinkedHashMap<>();

    void normalize() {
        nodes = PermissionEntries.texts(nodes);
        meta = PermissionEntries.pairs(meta);
    }
}
