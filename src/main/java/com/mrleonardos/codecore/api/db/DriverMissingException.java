package com.mrleonardos.codecore.api.db;

/**
 * Класса драйвера нет в classpath сервера.
 *
 * <p>
 * Ядро драйверы не поставляет: их кладёт администратор. Поэтому в сообщении стоит имя класса и куда
 * положить jar, а не общая фраза о том, что база не поднялась.
 */
public final class DriverMissingException extends DatabaseException {

    private static final long serialVersionUID = 1L;

    public DriverMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
