package com.mrleonardos.codecore;

/** Ноды прав, которыми распоряжается само ядро. */
public final class CorePermissions {

    /** Доступ к обслуживающим командам ядра. */
    public static final String ADMIN = "codecore.admin";

    /** Перезагрузка файлов настроек всех модов. */
    public static final String RELOAD = "codecore.admin.reload";

    /** Состояние ролей: кто их держит и чего не умеет. */
    public static final String ADAPTERS = "codecore.admin.adapters";

    private CorePermissions() {}
}
