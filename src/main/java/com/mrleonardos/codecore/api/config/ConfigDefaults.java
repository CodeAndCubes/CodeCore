package com.mrleonardos.codecore.api.config;

import java.util.function.Supplier;

/** Значения по умолчанию из конструктора без аргументов: общее для описаний файлов и секций. */
final class ConfigDefaults {

    private ConfigDefaults() {}

    static <T> Supplier<T> reflective(Class<T> type) {
        return () -> {
            try {
                return type.getDeclaredConstructor()
                    .newInstance();
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException(
                    type.getName() + " needs a no-argument constructor or an explicit defaults supplier",
                    failure);
            }
        };
    }
}
