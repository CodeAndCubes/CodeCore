package com.mrleonardos.codecore.internal.config;

import com.mrleonardos.codecore.api.client.image.ImageLimits;
import com.mrleonardos.codecore.api.config.Comment;

/**
 * Секция {@code [images]}: пределы загрузки картинок.
 *
 * <p>
 * Заводские значения совпадают с потолками безопасности из {@link ImageLimits}, и правка файла может
 * только опустить их. Числа читаются как константы времени компиляции, поэтому серверная сборка эту
 * секцию пишет так же, как клиентская, а сам клиентский класс в неё не попадает.
 */
@Comment("Картинки: аватары и превью ссылок. Значения только опускают заводские потолки, поднять их нельзя.")
public final class ImagesSection {

    @Comment("Больше этого объёма картинка не читается.")
    public int maxBytes = ImageLimits.MAX_BYTES;

    @Comment("Больше этого числа пикселей исходник не декодируется.")
    public int maxSourcePixels = ImageLimits.MAX_SOURCE_PIXELS;

    @Comment("Сторона готовой картинки в пикселях.")
    public int maxSize = ImageLimits.MAX_SIZE;

    @Comment("Сколько картинок держать в памяти клиента.")
    public int maxHandles = ImageLimits.MAX_HANDLES;

    public int connectTimeoutMs = ImageLimits.CONNECT_TIMEOUT_MS;

    public int readTimeoutMs = ImageLimits.READ_TIMEOUT_MS;
}
