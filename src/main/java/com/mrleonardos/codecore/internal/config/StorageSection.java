package com.mrleonardos.codecore.internal.config;

import com.mrleonardos.codecore.api.config.Comment;

/** Секция {@code [storage]}: как роли хранят свои данные. */
@Comment("Хранилище: значение для всех ролей сразу.")
public final class StorageSection {

    /** Встроенное хранилище: файлы рядом с конфигами. */
    public static final String DEFAULT_PROVIDER = "json";

    /** Через сколько секунд несохранённое уходит на диск. */
    public static final int DEFAULT_AUTOSAVE_SECONDS = 30;

    @Comment("Имя провайдера. Встроен \"json\", остальные приносят моды.")
    public String provider = DEFAULT_PROVIDER;

    @Comment("Через сколько секунд несохранённое уходит на диск.")
    public int autosaveSeconds = DEFAULT_AUTOSAVE_SECONDS;

    void normalize() {
        if (provider == null || provider.isEmpty()) {
            provider = DEFAULT_PROVIDER;
        }
        if (autosaveSeconds < 1) {
            autosaveSeconds = DEFAULT_AUTOSAVE_SECONDS;
        }
    }
}
