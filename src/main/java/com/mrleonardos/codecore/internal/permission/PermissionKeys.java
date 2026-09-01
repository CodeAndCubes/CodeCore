package com.mrleonardos.codecore.internal.permission;

/** Имена и обозначения, из которых собран файл прав. */
public final class PermissionKeys {

    /** Имя файла настроек прав. */
    public static final String FILE_NAME = "permissions";

    /** Группа, в которую попадает игрок без явного назначения. */
    public static final String DEFAULT_GROUP = "player";

    /** Группа, которую получают операторы сервера. */
    public static final String OPERATOR_GROUP = "admin";

    /** Правило, покрывающее любую ноду. */
    public static final String WILDCARD = "*";

    /** Разделитель сегментов ноды. */
    public static final char NODE_SEPARATOR = '.';

    /** Префикс правила-запрета: перебивает разрешение той же точности. */
    public static final char DENY_PREFIX = '-';

    private PermissionKeys() {}
}
