package com.mrleonardos.codecore.internal.db;

/**
 * Заводские значения записи и границы, в которые зажимаются числа.
 *
 * <p>
 * Кривое число трактуется как отсутствие: ноль, отрицательное и вышедшее за границу заменяются заводским
 * со строкой в лог. Поле, из-за которого сервер молча встал бы в тике на полминуты, лучше вернуть к
 * разумному, чем послушно исполнить опечатку.
 */
public final class DbLimits {

    public static final int POOL_SIZE_DEFAULT = 8;
    public static final int POOL_SIZE_MIN = 1;
    public static final int POOL_SIZE_MAX = 32;

    public static final int CONNECTION_TIMEOUT_DEFAULT = 5000;
    public static final int CONNECTION_TIMEOUT_MIN = 100;
    public static final int CONNECTION_TIMEOUT_MAX = 60000;

    public static final int QUERY_TIMEOUT_DEFAULT = 10000;
    public static final int QUERY_TIMEOUT_MIN = 100;
    public static final int QUERY_TIMEOUT_MAX = 600000;

    public static final int IDLE_TIMEOUT_DEFAULT = 60;
    public static final int IDLE_TIMEOUT_MIN = 5;
    public static final int IDLE_TIMEOUT_MAX = 3600;

    /** Наибольшая длина метки роли. */
    public static final int ROLE_MAX_LENGTH = 32;

    private DbLimits() {}

    /** Значение в границах или заводское, когда оно за ними. */
    public static int clamp(int value, int min, int max, int fallback) {
        if (value < min || value > max) {
            return fallback;
        }
        return value;
    }
}
