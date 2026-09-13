package com.mrleonardos.codecore.api.db;

/**
 * Беда с базой.
 *
 * <p>
 * Непроверяемое нарочно: база это ресурс, который может не ответить в любой момент, и обвешивать
 * каждый вызов проверкой мод не обязан. Кто хочет разобрать причину, ловит наследника.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
