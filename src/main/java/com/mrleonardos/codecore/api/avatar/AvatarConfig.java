package com.mrleonardos.codecore.api.avatar;

import java.util.Locale;

/**
 * Откуда брать аватары игроков, решает сервер.
 *
 * <p>
 * Настройка общая для обеих сторон: сервер читает её из файла и рассылает клиентам, клиент по ней
 * собирает провайдера. Иначе каждый игрок настраивал бы адрес сам, и на одном сервере половина людей
 * ходила бы без аватаров, а вторая — с чужими.
 *
 * <p>
 * Значения только описывают источник; клиент сам ходит по адресу и обрабатывает картинку.
 */
public final class AvatarConfig {

    /** Аватары не используются. */
    public static final String PROVIDER_NONE = "none";

    /** Адрес собирается из шаблона: {@code {name}}, {@code {uuid}}, {@code {uuid_nodash}}. */
    public static final String PROVIDER_URL = "urlTemplate";

    /** По шаблону запрашивается json, адрес картинки берётся из него по {@code jsonPath}. */
    public static final String PROVIDER_JSON = "jsonEndpoint";

    /** Картинки лежат у игрока в папке рядом с конфигом. */
    public static final String PROVIDER_FOLDER = "localFolder";

    /** Наименьший разумный размер готового аватара. */
    public static final int MIN_SIZE = 16;

    /** Наибольший размер, который сервер вправе запросить у клиента. */
    public static final int MAX_SIZE = 256;

    private final String provider;
    private final String url;
    private final String jsonPath;
    private final String folder;
    private final int size;

    private AvatarConfig(String provider, String url, String jsonPath, String folder, int size) {
        this.provider = provider;
        this.url = url;
        this.jsonPath = jsonPath;
        this.folder = folder;
        this.size = size;
    }

    /**
     * Собрать настройку, приведя её к допустимым значениям.
     *
     * <p>
     * Значения приходят из файла на сервере и по сети, поэтому проверяются здесь, а не у того, кто ими
     * пользуется: незнакомый провайдер превращается в {@link #PROVIDER_NONE}, размер зажимается в
     * границы, папка остаётся только относительной и без выхода наверх.
     */
    public static AvatarConfig of(String provider, String url, String jsonPath, String folder, int size) {
        return new AvatarConfig(
            known(provider),
            url == null ? "" : url,
            jsonPath == null ? "" : jsonPath,
            inside(folder),
            Math.max(MIN_SIZE, Math.min(MAX_SIZE, size)));
    }

    /** Настройка «аватаров нет». */
    public static AvatarConfig disabled() {
        return new AvatarConfig(PROVIDER_NONE, "", "", "", MIN_SIZE);
    }

    public String provider() {
        return provider;
    }

    public String url() {
        return url;
    }

    public String jsonPath() {
        return jsonPath;
    }

    public String folder() {
        return folder;
    }

    public int size() {
        return size;
    }

    /** Есть ли вообще источник аватаров. */
    public boolean enabled() {
        return !PROVIDER_NONE.equals(provider);
    }

    /**
     * Папка, из которой клиент не выйдет.
     *
     * <p>
     * Значение приходит от сервера, а {@code Path.resolve} на абсолютном пути отбрасывает основание
     * целиком: {@code C:/Users/Public} или {@code ../../..} увели бы чтение картинок куда угодно по диску.
     * Такая папка отбрасывается, и остаётся папка по умолчанию.
     */
    private static String inside(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String path = value.replace('\\', '/');
        if (path.startsWith("/") || path.contains(":") || path.contains("..")) {
            return "";
        }
        return path;
    }

    private static String known(String value) {
        if (value == null) {
            return PROVIDER_NONE;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (PROVIDER_URL.toLowerCase(Locale.ROOT)
            .equals(lower)) {
            return PROVIDER_URL;
        }
        if (PROVIDER_JSON.toLowerCase(Locale.ROOT)
            .equals(lower)) {
            return PROVIDER_JSON;
        }
        if (PROVIDER_FOLDER.toLowerCase(Locale.ROOT)
            .equals(lower)) {
            return PROVIDER_FOLDER;
        }
        return PROVIDER_NONE;
    }

    @Override
    public String toString() {
        return provider + (url.isEmpty() ? "" : " " + url);
    }
}
