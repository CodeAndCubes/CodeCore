package com.mrleonardos.codecore;

/** Ноды прав, которыми распоряжается само ядро. */
public final class CorePermissions {

    /** Доступ к обслуживающим командам ядра. */
    public static final String ADMIN = "codecore.admin";

    /** Перезагрузка файлов настроек всех модов. */
    public static final String RELOAD = "codecore.admin.reload";

    private CorePermissions() {}
}
