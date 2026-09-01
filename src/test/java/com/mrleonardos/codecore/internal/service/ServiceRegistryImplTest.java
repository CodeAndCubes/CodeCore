package com.mrleonardos.codecore.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mrleonardos.codecore.api.service.ServiceNotFoundException;
import com.mrleonardos.codecore.api.service.ServicePriority;
import com.mrleonardos.codecore.api.service.ServiceRegistry;

class ServiceRegistryImplTest {

    private static final Logger LOG = LogManager.getLogger(ServiceRegistryImplTest.class);

    private final ServiceRegistryImpl registry = new ServiceRegistryImpl(LOG);

    @Test
    @DisplayName("аддон вытесняет встроенную реализацию")
    void addonReplacesBuiltin() {
        Greeting builtin = () -> "builtin";
        Greeting addon = () -> "addon";

        registry.register(Greeting.class, builtin, ServicePriority.BUILTIN);
        registry.register(Greeting.class, addon, ServicePriority.ADDON);

        assertSame(addon, registry.require(Greeting.class));
    }

    @Test
    @DisplayName("равный вес не меняет владельца")
    void equalWeightKeepsTheFirst() {
        Greeting first = () -> "first";
        Greeting second = () -> "second";

        registry.register(Greeting.class, first, ServicePriority.BUILTIN);
        registry.register(Greeting.class, second, ServicePriority.BUILTIN);

        assertSame(first, registry.require(Greeting.class));
    }

    @Test
    @DisplayName("переопределение садится поверх аддона")
    void overrideBeatsAddon() {
        Greeting addon = () -> "addon";
        Greeting override = () -> "override";

        registry.register(Greeting.class, addon, ServicePriority.ADDON);
        registry.register(Greeting.class, override, ServicePriority.OVERRIDE);

        assertSame(override, registry.require(Greeting.class));
    }

    @Test
    @DisplayName("после заморозки регистрация отклонена, а чтение работает")
    void frozenRegistryRefusesRegistration() {
        Greeting builtin = () -> "builtin";
        registry.register(Greeting.class, builtin, ServicePriority.BUILTIN);
        assertFalse(registry.frozen());

        registry.freeze();

        assertTrue(registry.frozen());
        assertSame(builtin, registry.require(Greeting.class));
        assertThrows(
            IllegalStateException.class,
            () -> registry.register(Greeting.class, () -> "late", ServicePriority.OVERRIDE));
    }

    @Test
    @DisplayName("проверка аргументов идёт раньше проверки заморозки")
    void argumentsAreCheckedBeforeTheFreeze() {
        registry.freeze();

        assertThrows(
            IllegalArgumentException.class,
            () -> registry.register(null, "impl", ServicePriority.OVERRIDE),
            "нулевой тип не должен превращаться в NPE из сообщения о заморозке");
    }

    @Test
    @DisplayName("предложение с нулевым весом отклонено")
    void nullPriorityIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(Greeting.class, () -> "x", null));
        assertFalse(
            registry.find(Greeting.class)
                .isPresent());
    }

    @Test
    @DisplayName("реализация не того типа отклонена")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void wrongImplementationTypeIsRefused() {
        ServiceRegistry raw = registry;

        assertThrows(
            IllegalArgumentException.class,
            () -> raw.register((Class) Greeting.class, "не приветствие", ServicePriority.BUILTIN));
    }

    @Test
    @DisplayName("обязательный запрос без реализации бросает, необязательный отдаёт пустоту")
    void missingServiceIsToldApart() {
        ServiceNotFoundException failure = assertThrows(
            ServiceNotFoundException.class,
            () -> registry.require(Greeting.class));

        assertTrue(
            failure.getMessage()
                .contains(Greeting.class.getName()),
            failure.getMessage());
        assertEquals(Optional.empty(), registry.find(Greeting.class));
    }

    @FunctionalInterface
    private interface Greeting {

        String text();
    }
}
