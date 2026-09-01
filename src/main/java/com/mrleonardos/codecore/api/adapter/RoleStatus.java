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
    private final String fallback;

    public RoleStatus(String role, String owner, RoleChoice choice, List<String> candidates,
        Set<RoleCapability> missing) {
        this(role, owner, choice, candidates, missing, null);
    }

    public RoleStatus(String role, String owner, RoleChoice choice, List<String> candidates,
        Set<RoleCapability> missing, String fallback) {
        this.role = role;
        this.owner = owner;
        this.choice = choice;
        this.candidates = Collections.unmodifiableList(candidates);
        this.missing = Collections.unmodifiableSet(new LinkedHashSet<>(missing));
        this.fallback = fallback;
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

    /**
     * Чем закрыта роль, оставшаяся ничьей, или {@code null}, если она пуста.
     *
     * <p>
     * Владельцем запасная реализация не считается, поэтому {@link #owner()} рядом с непустым значением
     * этого поля всё равно {@code null}.
     */
    public String fallback() {
        return fallback;
    }
}
