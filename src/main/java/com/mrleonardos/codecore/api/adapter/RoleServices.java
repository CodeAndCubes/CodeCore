package com.mrleonardos.codecore.api.adapter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Реализации, которые победитель роли отдаёт ядру: тип сервиса в реализацию.
 *
 * <p>
 * Собирается в {@link RoleAdapter#create()}, то есть только у выигравшей заявки. Проигравшие ничего не
 * создают, поэтому мост к чужому моду не трогает его api, пока не назван владельцем.
 */
public final class RoleServices {

    private final Map<Class<?>, Object> implementations;

    private RoleServices(Map<Class<?>, Object> implementations) {
        this.implementations = Collections.unmodifiableMap(implementations);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Типы сервисов, которые эта заявка закрывает. */
    public Set<Class<?>> types() {
        return implementations.keySet();
    }

    /** Реализация сервиса или {@code null}, если эта заявка его не закрывает. */
    public <T> T find(Class<T> type) {
        return type.cast(implementations.get(type));
    }

    public static final class Builder {

        private final Map<Class<?>, Object> implementations = new LinkedHashMap<>();

        private Builder() {}

        public <T> Builder add(Class<T> type, T implementation) {
            if (type == null || implementation == null) {
                throw new IllegalArgumentException("Role service type and implementation must not be null");
            }
            if (!type.isInstance(implementation)) {
                throw new IllegalArgumentException(
                    implementation.getClass()
                        .getName() + " does not implement "
                        + type.getName());
            }
            implementations.put(type, implementation);
            return this;
        }

        public RoleServices build() {
            return new RoleServices(new LinkedHashMap<>(implementations));
        }
    }
}
