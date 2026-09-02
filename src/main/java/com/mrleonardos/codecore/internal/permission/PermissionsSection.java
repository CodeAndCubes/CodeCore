package com.mrleonardos.codecore.internal.permission;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Секция {@code [permissions]} главного файла.
 *
 * <p>
 * Объявляет её ядро, а не CodePerms: встроенная реализация прав живёт в ядре и без имени группы по
 * умолчанию не работает даже там, где CodePerms не стоит.
 */
@Comment("Права. Файлы лежат в config/code/permissions/.")
public final class PermissionsSection {

    private static final String DEFAULT_GROUP_KEY = "defaultGroup";
    private static final String OPERATOR_GROUP_KEY = "opGroup";

    @Comment("Группа игрока, которому ничего не выдали.")
    public String defaultGroup = PermissionKeys.DEFAULT_GROUP;

    @Comment("Группа, в которую попадает оператор сервера из ops.json.")
    public String opGroup = PermissionKeys.OPERATOR_GROUP;

    /**
     * Пустое имя группы возвращается к заводскому: без имени права не считаются вовсе.
     *
     * <p>
     * Подмена называется в логе. Молча она выглядела бы так: админ стёр значение, перезапустил сервер и
     * увидел в файле прежнее имя, не понимая, кто его вернул.
     *
     * <p>
     * Третьего смысла у пустой строки нет: она не выключает группу. Сопоставление операторов
     * выключается тем, что в {@code opGroup} стоит та же группа, что в {@code defaultGroup}.
     */
    public void normalize(Logger log) {
        defaultGroup = filled(log, DEFAULT_GROUP_KEY, defaultGroup, PermissionKeys.DEFAULT_GROUP);
        opGroup = filled(log, OPERATOR_GROUP_KEY, opGroup, PermissionKeys.OPERATOR_GROUP);
    }

    private static String filled(Logger log, String key, String value, String factory) {
        if (value != null && !value.isEmpty()) {
            return value;
        }
        log.warn("Config permissions.{} is empty, using \"{}\" instead", key, factory);
        return factory;
    }
}
