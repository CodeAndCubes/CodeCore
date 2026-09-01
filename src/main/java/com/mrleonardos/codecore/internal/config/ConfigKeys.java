package com.mrleonardos.codecore.internal.config;

/** Имена и суффиксы, из которых складываются файлы настроек. */
public final class ConfigKeys {

    /** Поле с версией схемы. Добавляется при записи, в классах настроек его нет. */
    public static final String SCHEMA_VERSION = "schemaVersion";

    public static final String FILE_EXTENSION = ".json";

    /** Временный файл, который затем атомарно занимает место основного. */
    public static final String TEMPORARY_SUFFIX = ".tmp";

    /** Куда откладывается файл, который не удалось разобрать. */
    public static final String BROKEN_SUFFIX = ".broken";

    /** Подпапка клиентских предпочтений внутри папки мода. */
    public static final String CLIENT_DIRECTORY = "client";

    private ConfigKeys() {}
}
