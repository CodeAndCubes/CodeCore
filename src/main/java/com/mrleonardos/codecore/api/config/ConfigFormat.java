package com.mrleonardos.codecore.api.config;

/**
 * Формат файла настроек.
 *
 * <p>
 * Настройки пишутся в toml: там переживают перезапись и описания полей из мода, и строки, дописанные
 * человеком. Данные, которые правит только мод, остаются в json: комментарии там некому читать, а десять
 * тысяч строк журнала разбираются быстрее.
 */
public enum ConfigFormat {

    /** Настройки: секции с заголовками и комментарии к полям. */
    TOML(".toml"),

    /** Машинные данные: без комментариев, зато быстро. */
    JSON(".json");

    private final String extension;

    ConfigFormat(String extension) {
        this.extension = extension;
    }

    /** Расширение файла вместе с точкой. */
    public String extension() {
        return extension;
    }
}
