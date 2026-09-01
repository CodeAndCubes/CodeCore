package com.mrleonardos.codecore.api.client.image;

/**
 * Границы, за которые не выходит загрузка картинок.
 *
 * <p>
 * Адрес картинки задаёт конфиг, а по адресу может лежать что угодно: гигабайтный файл, изображение
 * 30000×30000 или бесконечный поток. Поэтому ограничен и объём, и размер в пикселях, и время ожидания.
 *
 * <p>
 * Константы это заводские потолки безопасности. Секция {@code [images]} главного файла умеет только
 * опустить их: настройка, поднимающая потолок, зажимается обратно с записью в лог. Действующие значения
 * берутся из {@link #current()}.
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

    private static volatile ImageLimits current = factory();

    private final int maxBytes;
    private final int maxSourcePixels;
    private final int maxSize;
    private final int maxHandles;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public ImageLimits(int maxBytes, int maxSourcePixels, int maxSize, int maxHandles, int connectTimeoutMs,
        int readTimeoutMs) {
        this.maxBytes = maxBytes;
        this.maxSourcePixels = maxSourcePixels;
        this.maxSize = maxSize;
        this.maxHandles = maxHandles;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /** Заводские потолки: то, что действует, пока конфиг не прочитан. */
    public static ImageLimits factory() {
        return new ImageLimits(
            MAX_BYTES,
            MAX_SOURCE_PIXELS,
            MAX_SIZE,
            MAX_HANDLES,
            CONNECT_TIMEOUT_MS,
            READ_TIMEOUT_MS);
    }

    /** Действующие пределы: заводские потолки, опущенные настройками. */
    public static ImageLimits current() {
        return current;
    }

    /**
     * Подставить пределы из конфига. Зовётся ядром один раз при готовности клиентской стороны.
     *
     * <p>
     * Читают их фоновые потоки загрузки, поэтому ссылка {@code volatile}: запись видна им сразу, а
     * читаются шесть чисел одного объекта, а не шесть полей вразнобой.
     */
    public static void install(ImageLimits limits) {
        current = limits;
    }

    /** Максимум байт по одному адресу. */
    public int maxBytes() {
        return maxBytes;
    }

    /** Максимум пикселей в исходной картинке. */
    public int maxSourcePixels() {
        return maxSourcePixels;
    }

    /** Наибольшая сторона готовой картинки. */
    public int maxSize() {
        return maxSize;
    }

    /** Сколько картинок держится в памяти одновременно. */
    public int maxHandles() {
        return maxHandles;
    }

    /** Сколько ждать соединения. */
    public int connectTimeoutMs() {
        return connectTimeoutMs;
    }

    /** Сколько ждать данных. */
    public int readTimeoutMs() {
        return readTimeoutMs;
    }
}
