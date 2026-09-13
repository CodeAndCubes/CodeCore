package com.mrleonardos.codecore.api.service;

/**
 * Одна строка над хотбаром: что показать, под каким ключом и насколько.
 *
 * <p>
 * Срок показа и окно повтора это разные числа, и сводить их в одно нельзя. Отказ привата висит пару
 * секунд, а повторять его хватит раза в пять: иначе игрок, который долбит чужую стену, получит сплошную
 * надпись во весь бой. Когда окно не задали, оно равно сроку показа: строка держится, пока причина не
 * исчезла, и не мигает.
 *
 * <p>
 * Ключ не показывается игроку, по нему сервис узнаёт повтор. Одна причина это один ключ: {@code deny} у
 * отказа, {@code limit} у потолка. Ключ на каждое сообщение убил бы антиспам, общий ключ на весь мод
 * прятал бы вторую причину за первой.
 */
public final class ActionBarLine {

    /** Сколько строка висит, когда срок не задали. */
    public static final int DEFAULT_SECONDS = 3;

    private final String key;
    private final String text;
    private final int seconds;

    /** Ноль значит «как срок показа»: наружу это число не выходит, {@link #repeatSeconds()} его решает. */
    private final int repeat;

    private ActionBarLine(String key, String text, int seconds, int repeat) {
        this.key = key;
        this.text = text;
        this.seconds = seconds;
        this.repeat = repeat;
    }

    /**
     * Строка с заводским сроком показа.
     *
     * @param key  ключ повтора, не пустой
     * @param text готовый текст, а не ключ перевода: языка клиента сервер не знает
     */
    public static ActionBarLine of(String key, String text) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Action bar line needs a key to tell repeats apart");
        }
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Action bar line has nothing to show");
        }
        return new ActionBarLine(key, text, DEFAULT_SECONDS, 0);
    }

    /** Сколько секунд строка висит на экране. */
    public ActionBarLine forSeconds(int value) {
        return new ActionBarLine(key, text, atLeastOne(value), repeat);
    }

    /** Через сколько секунд эту же строку можно показать снова. */
    public ActionBarLine repeatAfter(int value) {
        return new ActionBarLine(key, text, seconds, atLeastOne(value));
    }

    public String key() {
        return key;
    }

    public String text() {
        return text;
    }

    /** Срок показа в секундах. */
    public int seconds() {
        return seconds;
    }

    /** Окно повтора в секундах; без своего значения равно сроку показа. */
    public int repeatSeconds() {
        return repeat > 0 ? repeat : seconds;
    }

    private static int atLeastOne(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Action bar seconds must be positive, got " + value);
        }
        return value;
    }
}
