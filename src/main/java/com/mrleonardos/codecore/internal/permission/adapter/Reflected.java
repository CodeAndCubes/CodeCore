package com.mrleonardos.codecore.internal.permission.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.CoreConstants;

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
 * никому ничего не даёт». Пишется только первая неудача каждого члена: следующие пойдут на каждое сообщение
 * чата.
 */
final class Reflected {

    private static final Logger LOG = LogManager.getLogger(CoreConstants.MOD_NAME);
    private static final Set<String> REPORTED = Collections.newSetFromMap(new ConcurrentHashMap<>());

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

    private static void report(Member member, Throwable failure) {
        String where = where(member);
        if (REPORTED.add(where)) {
            LOG.warn("Permission adapter call {} failed, everything it answers counts as denied", where, failure);
        }
    }

    private static String where(Member member) {
        StringBuilder text = new StringBuilder(
            member.getDeclaringClass()
                .getName()).append('#')
                    .append(member.getName());
        if (member instanceof Method) {
            for (Class<?> parameter : ((Method) member).getParameterTypes()) {
                text.append(':')
                    .append(parameter.getName());
            }
        }
        return text.toString();
    }
}
