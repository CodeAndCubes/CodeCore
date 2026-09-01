package com.mrleonardos.codecore.internal.config;

/** Имена и суффиксы, из которых складываются файлы настроек. */
public final class ConfigKeys {

    /** Поле с версией схемы. Добавляется при записи, в классах настроек его нет. */
    public static final String SCHEMA_VERSION = "schemaVersion";

    /** Общая папка линейки внутри config и внутри папки мира. */
    public static final String LINEUP_DIRECTORY = "code";

    /** Главный файл линейки внутри папки линейки. */
    public static final String MAIN_FILE = "config.toml";

    /** Приставка modid, которую ядро отрезает, получая имя владельца. */
    public static final String OWNER_PREFIX = "code";

    /** Разделитель владельца и имени файла. */
    public static final String NAME_SEPARATOR = "-";

    /** Временный файл, который затем атомарно занимает место основного. */
    public static final String TEMPORARY_SUFFIX = ".tmp";

    /** Куда откладывается файл, который не удалось разобрать. */
    public static final String BROKEN_SUFFIX = ".broken";

    /** Подпапка клиентских предпочтений внутри папки роли. */
    public static final String CLIENT_DIRECTORY = "client";

    private ConfigKeys() {}
}
