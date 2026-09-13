package com.mrleonardos.codecore.internal.config;

import org.apache.logging.log4j.Logger;

import com.mrleonardos.codecore.api.config.Comment;

/**
 * Секция {@code [hud]}: что моды линейки рисуют поверх экрана.
 *
 * <p>
 * Пока здесь одно значение, и объявляет его ядро: сервис строки над хотбаром живёт в ядре, а мод, который
 * её показывает, о потолке повторов знать не обязан. Иначе каждый мод завёл бы свой ключ, и админ
 * настраивал бы одно и то же в четырёх файлах.
 */
@Comment("Надписи поверх экрана. Строку над хотбаром рисует клиент с CodeCore, чужой клиент её не увидит.")
public final class HudSection {

    /** Чаще раза в секунду строку повторять нельзя: она начнёт мигать на каждом такте. */
    public static final int MIN_REPEAT_SECONDS = 1;

    /** Заводское окно: столько же, сколько отказ висит на экране. */
    public static final int DEFAULT_REPEAT_SECONDS = 3;

    private static final String REPEAT_KEY = "actionBarRepeatSeconds";

    @Comment({ "Не чаще раза в столько секунд повторяется строка с одним ключом у одного игрока.",
        "Мод просит своё окно, но меньше этого числа оно не станет." })
    public int actionBarRepeatSeconds = DEFAULT_REPEAT_SECONDS;

    /**
     * Ноль и отрицательное возвращаются к секунде с записью в лог.
     *
     * <p>
     * Молча это выглядело бы так: админ поставил ноль, чтобы выключить антиспам, и получил мигающую
     * надпись, которую нечем объяснить.
     */
    public void normalize(Logger log) {
        if (actionBarRepeatSeconds < MIN_REPEAT_SECONDS) {
            log.warn(
                "Config hud.{} is {}, using {} instead",
                REPEAT_KEY,
                Integer.valueOf(actionBarRepeatSeconds),
                Integer.valueOf(MIN_REPEAT_SECONDS));
            actionBarRepeatSeconds = MIN_REPEAT_SECONDS;
        }
    }
}
