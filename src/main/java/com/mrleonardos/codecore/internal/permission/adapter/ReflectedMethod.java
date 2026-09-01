package com.mrleonardos.codecore.internal.permission.adapter;

import java.lang.reflect.Method;

/**
 * Метод чужого класса, найденный один раз и запомненный.
 *
 * <p>
 * {@code Class.getMethod} копирует весь массив методов класса, а права спрашивают на каждого получателя
 * каждого сообщения чата. Класс получателя в рантайме всегда один и тот же, поэтому хватает памяти на одну
 * запись: смена класса просто перерешает поиск.
 */
final class ReflectedMethod {

    private final String name;
    private final Class<?>[] parameters;

    private volatile Class<?> owner;
    private volatile Method resolved;

    ReflectedMethod(String name, Class<?>... parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    /** Метод у этого класса или {@code null}, если такого метода нет. */
    Method of(Class<?> candidate) {
        if (candidate == owner) {
            return resolved;
        }
        Method found = Reflected.method(candidate, name, parameters);
        resolved = found;
        owner = candidate;
        return found;
    }
}
