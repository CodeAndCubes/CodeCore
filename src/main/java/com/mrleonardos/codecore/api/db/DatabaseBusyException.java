package com.mrleonardos.codecore.api.db;

/**
 * Пул не отдал соединение за отведённое время.
 *
 * <p>
 * Отличается от недоступности: база жива, но все соединения заняты. Лечится размером пула или тем, что
 * мод перестаёт держать соединение дольше запроса.
 */
public final class DatabaseBusyException extends DatabaseException {

    private static final long serialVersionUID = 1L;

    public DatabaseBusyException(String message) {
        super(message);
    }
}
