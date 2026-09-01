package com.mrleonardos.codecore.api.util;

/**
 * Промежутки времени в человеческом виде: {@code 30s}, {@code 10m}, {@code 2h30m}, {@code 7d}.
 *
 * <p>
 * Нужны везде, где срок вводит человек: муты, временные выдачи прав, задержки. Число без суффикса
 * считается секундами.
 */
public final class Durations {

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int SECONDS_IN_HOUR = 60 * SECONDS_IN_MINUTE;
    private static final int SECONDS_IN_DAY = 24 * SECONDS_IN_HOUR;

    private Durations() {}

    /**
     * Разобрать запись в секунды.
     *
     * <p>
     * Счёт идёт в {@code long}, а результат не превышает {@link Integer#MAX_VALUE}: {@code 49712d} это
     * больше двух миллиардов секунд, и в {@code int} такое число превратилось бы в мут на сутки вместо мута
     * на век. Слишком большой срок отвергается так же, как непонятная запись.
     *
     * @return число секунд или {@code -1}, если запись непонятна или срок больше {@link Integer#MAX_VALUE}
     *         секунд
     */
    public static int toSeconds(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        }

        long total = 0;
        long number = -1;
        for (int index = 0; index < value.length(); index++) {
            char symbol = value.charAt(index);
            if (symbol >= '0' && symbol <= '9') {
                number = (number < 0 ? 0 : number) * 10 + (symbol - '0');
                if (number > Integer.MAX_VALUE) {
                    return -1;
                }
                continue;
            }
            if (number < 0) {
                return -1;
            }
            int multiplier = multiplierOf(symbol);
            if (multiplier < 0) {
                return -1;
            }
            total += number * multiplier;
            if (total > Integer.MAX_VALUE) {
                return -1;
            }
            number = -1;
        }

        if (number >= 0) {
            total += number;
        }
        return total > Integer.MAX_VALUE ? -1 : (int) total;
    }

    /** Записать секунды в тот же человеческий вид: {@code 2h 30m}. */
    public static String format(int seconds) {
        if (seconds <= 0) {
            return "0s";
        }

        StringBuilder text = new StringBuilder();
        int rest = seconds;
        rest = append(text, rest, SECONDS_IN_DAY, 'd');
        rest = append(text, rest, SECONDS_IN_HOUR, 'h');
        rest = append(text, rest, SECONDS_IN_MINUTE, 'm');
        append(text, rest, 1, 's');
        return text.toString()
            .trim();
    }

    private static int append(StringBuilder text, int seconds, int unit, char suffix) {
        int count = seconds / unit;
        if (count > 0) {
            text.append(count)
                .append(suffix)
                .append(' ');
        }
        return seconds % unit;
    }

    private static int multiplierOf(char suffix) {
        switch (suffix) {
            case 'd':
                return SECONDS_IN_DAY;
            case 'h':
                return SECONDS_IN_HOUR;
            case 'm':
                return SECONDS_IN_MINUTE;
            case 's':
                return 1;
            default:
                return -1;
        }
    }
}
