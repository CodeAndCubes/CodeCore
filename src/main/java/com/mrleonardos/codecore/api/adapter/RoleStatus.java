package com.mrleonardos.codecore.api.adapter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Чем кончился выбор владельца роли: для стартовой сводки и для {@code /codecore adapters}. */
public final class RoleStatus {

    private final String role;
    private final String owner;
    private final RoleChoice choice;
    private final List<String> candidates;
    private final Set<RoleCapability> missing;

    public RoleStatus(String role, String owner, RoleChoice choice, List<String> candidates,
        Set<RoleCapability> missing) {
        this.role = role;
        this.owner = owner;
        this.choice = choice;
        this.candidates = Collections.unmodifiableList(candidates);
        this.missing = Collections.unmodifiableSet(new LinkedHashSet<>(missing));
    }

    public String role() {
        return role;
    }

    /** Имя владельца или {@code null}, если роль не занята никем. */
    public String owner() {
        return owner;
    }

    /** Откуда взялся владелец. */
    public RoleChoice choice() {
        return choice;
    }

    /** Имена всех заявок роли, недоступные помечены звёздочкой. */
    public List<String> candidates() {
        return candidates;
    }

    /** Умения роли, которых у владельца нет. */
    public Set<RoleCapability> missing() {
        return missing;
    }
}
