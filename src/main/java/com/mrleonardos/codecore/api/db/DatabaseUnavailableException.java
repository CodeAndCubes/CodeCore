package com.mrleonardos.codecore.api.db;

/**
 * База не отвечает.
 *
 * <p>
 * Пометки навсегда нет: следующий запрос снова пытается соединиться, и поднятая администратором СУБД
 * подхватывается без перезапуска сервера.
 */
public final class DatabaseUnavailableException extends DatabaseException {

    private static final long serialVersionUID = 1L;

    public DatabaseUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseUnavailableException(String message) {
        super(message);
    }
}
