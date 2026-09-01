package com.mrleonardos.codecore.api.adapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Объявление роли: её имя, полный перечень умений и типы сервисов, которыми она закрывается.
 *
 * <p>
 * Объявляет тот, у кого есть реализация роли по умолчанию: права объявляет ядро, экономику и
 * перемещения объявляют сами моды. Перечень умений здесь полный, а каждая заявка называет своё
 * подмножество.
 */
public final class RoleSpec {

    private final String role;
    private final Set<RoleCapability> capabilities;
    private final Set<Class<?>> services;

    private RoleSpec(Builder builder) {
        this.role = builder.role;
        this.capabilities = Collections.unmodifiableSet(new LinkedHashSet<>(builder.capabilities));
        this.services = Collections.unmodifiableSet(new LinkedHashSet<>(builder.services));
    }

    public static Builder of(String role) {
        return new Builder(role);
    }

    public String role() {
        return role;
    }

    /** Полный перечень умений роли. */
    public Set<RoleCapability> capabilities() {
        return capabilities;
    }

    /** Типы сервисов, которые ядро регистрирует за победителем роли. */
    public Set<Class<?>> services() {
        return services;
    }

    public static final class Builder {

        private final String role;
        private final Set<RoleCapability> capabilities = new LinkedHashSet<>();
        private final Set<Class<?>> services = new LinkedHashSet<>();

        private Builder(String role) {
            if (role == null || role.isEmpty()) {
                throw new IllegalArgumentException("Role name must not be empty");
            }
            this.role = role;
        }

        public Builder capabilities(RoleCapability... values) {
            capabilities.addAll(Arrays.asList(values));
            return this;
        }

        public Builder services(Class<?>... values) {
            services.addAll(Arrays.asList(values));
            return this;
        }

        public RoleSpec build() {
            return new RoleSpec(this);
        }
    }
}
