package com.mrleonardos.codecore.api.client.image;

/**
 * Границы, за которые не выходит загрузка картинок.
 *
 * <p>
 * Адрес картинки задаёт конфиг, а по адресу может лежать что угодно: гигабайтный файл, изображение
 * 30000×30000 или бесконечный поток. Поэтому ограничен и объём, и размер в пикселях, и время ожидания.
 */
public final class ImageLimits {

    /** Максимум байт, которые скачиваются по одному адресу. */
    public static final int MAX_BYTES = 2 * 1024 * 1024;

    /** Максимум пикселей в исходной картинке: защита от «архивных бомб» в декодере. */
    public static final int MAX_SOURCE_PIXELS = 4096 * 4096;

    /** Сколько ждать соединения. */
    public static final int CONNECT_TIMEOUT_MS = 5000;

    /** Сколько ждать данных. */
    public static final int READ_TIMEOUT_MS = 10000;

    /** Наименьшая сторона готовой картинки. */
    public static final int MIN_SIZE = 8;

    /** Наибольшая сторона готовой картинки. */
    public static final int MAX_SIZE = 512;

    /** Сколько не трогать адрес после неудачи. */
    public static final long RETRY_AFTER_FAILURE_MS = 5 * 60 * 1000L;

    /**
     * Сколько картинок держится в памяти клиента одновременно.
     *
     * <p>
     * Свыше этого числа самая давняя выселяется вместе с текстурой. На {@link #MAX_SIZE} это порядка
     * мегабайта на картинку, и без потолка сессия с двумя сотнями авторов в чате выедала бы видеопамять.
     */
    public static final int MAX_HANDLES = 256;

    private ImageLimits() {}
}
