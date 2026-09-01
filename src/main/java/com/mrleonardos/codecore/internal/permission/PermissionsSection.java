package com.mrleonardos.codecore.internal.permission;

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

    @Comment("Группа игрока, которому ничего не выдали.")
    public String defaultGroup = PermissionKeys.DEFAULT_GROUP;

    @Comment("Группа, в которую попадает оператор сервера из ops.json.")
    public String opGroup = PermissionKeys.OPERATOR_GROUP;

    /** Пустое имя группы возвращается к заводскому: без имени права не считаются вовсе. */
    public void normalize() {
        if (defaultGroup == null || defaultGroup.isEmpty()) {
            defaultGroup = PermissionKeys.DEFAULT_GROUP;
        }
        if (opGroup == null || opGroup.isEmpty()) {
            opGroup = PermissionKeys.OPERATOR_GROUP;
        }
    }
}
