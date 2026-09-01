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
     * @throws net.minecraft.command.CommandException с ключом перевода, если ввод не подходит
     */
    T parse(String raw);

    /** Варианты для автодополнения по началу слова. */
    default List<String> suggestions(ICommandSender sender, String partial) {
        return Collections.emptyList();
    }

    /** Забирает ли аргумент весь остаток строки, как делают сообщения и причины. */
    default boolean greedy() {
        return false;
    }
}
