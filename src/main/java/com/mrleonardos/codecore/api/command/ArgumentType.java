package com.mrleonardos.codecore.api.command;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.ICommandSender;

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
     * Переопределять надо этот метод: со сносом старых подписей перегрузка на {@code ICommandSender}
     * уходит. Пока живы обе, мост команд спрашивает их обе и складывает ответы, поэтому переопределённой
     * может быть любая, и подсказки не пропадут посреди переезда.
     *
     * <p>
     * Реализация, написанная без {@code @Override}, после смены подписи станет перегрузкой, а не
     * переопределением: сборка останется зелёной, а подсказки замолчат. Аннотацию ставить обязательно.
     */
    default List<String> suggestions(CommandSender sender, String partial) {
        return Collections.emptyList();
    }

    /** Варианты для автодополнения по началу слова; уходит со сносом старых подписей. */
    default List<String> suggestions(ICommandSender sender, String partial) {
        return Collections.emptyList();
    }

    /** Забирает ли аргумент весь остаток строки, как делают сообщения и причины. */
    default boolean greedy() {
        return false;
    }
}
