package com.mrleonardos.codecore.api.command;

import java.util.Collections;
import java.util.List;

/**
 * Тип аргумента команды: как разобрать введённое и что подсказать по Tab.
 *
 * @param <T> во что превращается текст
 */
public interface ArgumentType<T> {

    /**
     * Разобрать один токен.
     *
     * @throws CommandInputException с ключом перевода, если ввод не подходит
     */
    T parse(String raw);

    /**
     * Варианты для автодополнения по началу слова.
     *
     * <p>
     * Реализация, написанная без {@code @Override}, при следующей смене подписи станет перегрузкой, а не
     * переопределением: сборка останется зелёной, а подсказки замолчат. Аннотацию ставить обязательно.
     */
    default List<String> suggestions(CommandSender sender, String partial) {
        return Collections.emptyList();
    }

    /** Забирает ли аргумент весь остаток строки, как делают сообщения и причины. */
    default boolean greedy() {
        return false;
    }
}
