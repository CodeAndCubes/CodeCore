package com.mrleonardos.codecore.internal.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Группа в файле прав: что наследует, что разрешает и какие значения даёт своим участникам. */
public final class GroupEntry {

    public List<String> inherits = new ArrayList<>();
    public List<String> nodes = new ArrayList<>();
    public Map<String, String> meta = new LinkedHashMap<>();

    void normalize() {
        inherits = PermissionEntries.texts(inherits);
        nodes = PermissionEntries.texts(nodes);
        meta = PermissionEntries.pairs(meta);
    }
}
