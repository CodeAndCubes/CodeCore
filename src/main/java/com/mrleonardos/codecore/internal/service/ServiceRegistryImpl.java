package com.mrleonardos.codecore.internal.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.service.ServiceNotFoundException;
import com.mrleonardos.codecore.api.service.ServicePriority;
import com.mrleonardos.codecore.api.service.ServiceRegistry;

/**
 * Реестр сервисов.
 *
 * <p>
 * Регистрируют из потока загрузки модов, а спрашивают откуда угодно, в том числе из фоновых потоков.
 * Поэтому карта заменяется на неизменяемую копию при заморозке: запись {@code volatile}-поля и есть
 * безопасная публикация всего, что в этой карте лежит.
 */
public final class ServiceRegistryImpl implements ServiceRegistry {

    private final Logger log;

    private volatile Map<Class<?>, Entry<?>> entries = new ConcurrentHashMap<>();
    private volatile boolean frozen;

    public ServiceRegistryImpl(Logger log) {
        this.log = log;
    }

    @Override
    public <T> void register(Class<T> type, T implementation, ServicePriority priority) {
        if (type == null || implementation == null || priority == null) {
            throw new IllegalArgumentException("Service type, implementation and priority must not be null");
        }
        if (frozen) {
            throw new IllegalStateException(
                "Service registry is frozen, register " + type.getName() + " during your mod's init phase");
        }
        if (!type.isInstance(implementation)) {
            throw new IllegalArgumentException(
                implementation.getClass()
                    .getName() + " does not implement "
                    + type.getName());
        }

        Entry<?> current = entries.get(type);
        if (current != null && current.priority.weight() >= priority.weight()) {
            log.warn(
                "Ignored {} for {}: {} is already registered with priority {}",
                implementation.getClass()
                    .getName(),
                type.getName(),
                current.implementation.getClass()
                    .getName(),
                current.priority);
            return;
        }

        entries.put(type, new Entry<>(implementation, priority));
        log.info(
            "Service {} provided by {} ({})",
            type.getName(),
            implementation.getClass()
                .getName(),
            priority);
    }

    @Override
    public <T> T require(Class<T> type) {
        return find(type).orElseThrow(() -> new ServiceNotFoundException(type));
    }

    @Override
    public <T> Optional<T> find(Class<T> type) {
        Entry<?> entry = entries.get(type);
        return entry == null ? Optional.empty() : Optional.of(type.cast(entry.implementation));
    }

    @Override
    public boolean frozen() {
        return frozen;
    }

    /** Закрывает реестр для регистрации: дальше набор реализаций уже не меняется. */
    public void freeze() {
        Map<Class<?>, Entry<?>> published = Collections.unmodifiableMap(new HashMap<>(entries));
        entries = published;
        frozen = true;
        log.info("Service registry frozen with {} service(s)", published.size());
    }

    private static final class Entry<T> {

        private final T implementation;
        private final ServicePriority priority;

        private Entry(T implementation, ServicePriority priority) {
            this.implementation = implementation;
            this.priority = priority;
        }
    }
}
