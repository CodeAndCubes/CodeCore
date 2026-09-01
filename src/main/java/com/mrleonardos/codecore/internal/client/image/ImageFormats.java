package com.mrleonardos.codecore.internal.client.image;

import javax.imageio.spi.IIORegistry;

import org.apache.logging.log4j.LogManager;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

/**
 * Подключает декодеры, которых нет в самой Java.
 *
 * <p>
 * Ридер регистрируется вызовом, а не файлом сервисов: в собранном jar пути классов переезжают, и запись в
 * {@code META-INF/services} после этого указывает в пустоту. Прямая ссылка заодно удерживает декодер при
 * очистке неиспользуемых классов на сборке.
 *
 * <p>
 * Отсутствие декодера не останавливает запуск: без него просто не будет webp, и это ровно то, что обещает
 * описание форматов. Иначе сборка без TwelveMonkeys не давала бы игре стартовать вовсе.
 */
final class ImageFormats {

    private ImageFormats() {}

    static void register() {
        try {
            IIORegistry.getDefaultInstance()
                .registerServiceProvider(new WebPImageReaderSpi());
        } catch (Throwable unavailable) {
            LogManager.getLogger("CodeCore")
                .warn(
                    "WebP decoder is missing from this build, webp avatars will not load: {}",
                    unavailable.toString());
        }
    }
}
