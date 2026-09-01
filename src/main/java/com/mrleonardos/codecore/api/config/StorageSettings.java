package com.mrleonardos.codecore.api.config;

/**
 * Как роль хранит свои данные. Собирается ядром из секции {@code [storage]} главного файла и, если она
 * есть, из секции {@code [storage.<роль>]} с теми ключами, которые для этой роли отличаются.
 */
public final class StorageSettings {

    private final String provider;
    private final int autosaveSeconds;

    public StorageSettings(String provider, int autosaveSeconds) {
        this.provider = provider;
        this.autosaveSeconds = autosaveSeconds;
    }

    /** Имя провайдера хранилища. Встроен {@code json}, остальные приносят моды. */
    public String provider() {
        return provider;
    }

    /** Через сколько секунд несохранённое уходит на диск. */
    public int autosaveSeconds() {
        return autosaveSeconds;
    }

    @Override
    public String toString() {
        return "storage[provider=" + provider + ", autosaveSeconds=" + autosaveSeconds + "]";
    }
}
