package com.mrleonardos.codecore.internal.permission.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Обращение к чужому моду без зависимости на него.
 *
 * <p>
 * Адаптеру нельзя ссылаться на классы UltraMine, LuckPerms или ForgeEssentials напрямую: их может не быть на
 * сервере, а отсутствующий класс в сигнатуре роняет загрузку всего мода. Поэтому вызовы идут отражением, а
 * недоступный адаптер просто не регистрируется.
 *
 * <p>
 * Сорвавшийся вызов превращается в отказ в праве, и без записи в лог это выглядело бы как «мод прав молча
 * никому ничего не даёт». Пишется только первая неудача: следующие пойдут на каждое сообщение чата.
 */
final class Reflected {

    private static final Logger LOG = LogManager.getLogger("CodeCore");
    private static final AtomicBoolean REPORTED = new AtomicBoolean();

    private Reflected() {}

    static Class<?> type(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException absent) {
            return null;
        }
    }

    static Method method(Class<?> owner, String name, Class<?>... parameters) {
        try {
            return owner.getMethod(name, parameters);
        } catch (NoSuchMethodException absent) {
            return null;
        }
    }

    static Field field(Class<?> owner, String name) {
        try {
            return owner.getField(name);
        } catch (NoSuchFieldException absent) {
            return null;
        }
    }

    static Object call(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            report(method, failure);
            return null;
        }
    }

    static Object value(Field field) {
        try {
            return field.get(null);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            report(field, failure);
            return null;
        }
    }

    static boolean flag(Object value) {
        return value instanceof Boolean && (Boolean) value;
    }

    static String text(Object value, String fallback) {
        return value instanceof String && !((String) value).isEmpty() ? (String) value : fallback;
    }

    private static void report(Object member, Throwable failure) {
        if (REPORTED.compareAndSet(false, true)) {
            LOG.warn("Permission adapter call {} failed, everything it answers counts as denied", member, failure);
        }
    }
}
