package com.mrleonardos.codecore.api.db;

/**
 * Чем кончилась последняя проверка базы.
 *
 * <p>
 * Нужен моду, который показывает состояние своей базы в команде: спрашивать базу ради этого второй раз
 * не надо, ядро помнит ответ предыдущей попытки.
 */
public final class DbStatus {

    private static final DbStatus UNKNOWN = new DbStatus(false, -1L, "проверки ещё не было");

    private final boolean reachable;
    private final long pingMs;
    private final String detail;

    private DbStatus(boolean reachable, long pingMs, String detail) {
        this.reachable = reachable;
        this.pingMs = pingMs;
        this.detail = detail;
    }

    /** База ответила за столько миллисекунд. */
    public static DbStatus ok(long pingMs) {
        return new DbStatus(true, pingMs, "");
    }

    /** База не ответила, причина словами. */
    public static DbStatus failed(String detail) {
        return new DbStatus(false, -1L, detail == null ? "" : detail);
    }

    /** Базу ещё ни разу не спрашивали. */
    public static DbStatus unknown() {
        return UNKNOWN;
    }

    public boolean reachable() {
        return reachable;
    }

    /** Время ответа или отрицательное число, когда ответа не было. */
    public long pingMs() {
        return pingMs;
    }

    /** Причина отказа или пустая строка. */
    public String detail() {
        return detail;
    }
}
