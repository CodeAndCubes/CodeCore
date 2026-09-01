package com.mrleonardos.codecore.api.adapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Запасная реализация роли: то, что отвечает, когда роль не занята никем.
 *
 * <p>
 * Роль без владельца оставляет {@code require(тип)} без ответа, а это исключение посреди чужой команды.
 * Проще всего было бы объявить, что каждый мод сам разбирается с пустым ответом, но одинаковая заглушка
 * в трёх модах это тот самый дубль, которого линейка избегает. Поэтому объявитель роли говорит здесь,
 * чем закрыть дыру, и делает это один раз за всех.
 *
 * <p>
 * Запасная реализация не становится владельцем: {@link AdapterRegistry#owner(String)} остаётся пустым,
 * роль по-прежнему ничья. Она закрывает подмножество умений, а разница с перечнем роли уходит в
 * недоступное и печатается стартовой строкой.
 */
public final class RoleFallback {

    private final String description;
    private final Supplier<RoleServices> services;
    private final Set<RoleCapability> capabilities;

    private RoleFallback(String description, Supplier<RoleServices> services, Set<RoleCapability> capabilities) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Role fallback needs a description for the startup line");
        }
        if (services == null) {
            throw new IllegalArgumentException("Role fallback needs implementations");
        }
        this.description = description;
        this.services = services;
        this.capabilities = Collections.unmodifiableSet(new LinkedHashSet<>(capabilities));
    }

    /**
     * @param description  чем закрыта дыра, словами и по-английски: строка уходит в лог рядом с ролью
     * @param services     сборка реализаций, зовётся только когда роль осталась ничьей
     * @param capabilities умения, которые запасная реализация закрывает
     */
    public static RoleFallback of(String description, Supplier<RoleServices> services, RoleCapability... capabilities) {
        return new RoleFallback(description, services, new LinkedHashSet<>(Arrays.asList(capabilities)));
    }

    /** Чем закрыта дыра: эта строка стоит в стартовой сводке и в {@code /codecore adapters}. */
    public String description() {
        return description;
    }

    /** Умения, которые запасная реализация закрывает. Остальные умения роли не работают. */
    public Set<RoleCapability> capabilities() {
        return capabilities;
    }

    /** Собрать реализации. Зовётся один раз и только когда роль осталась без владельца. */
    public RoleServices create() {
        return services.get();
    }
}
