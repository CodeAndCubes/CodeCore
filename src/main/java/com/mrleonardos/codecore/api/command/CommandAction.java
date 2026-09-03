package com.mrleonardos.codecore.api.command;

/** Что делает команда, когда её разобрали. */
@FunctionalInterface
public interface CommandAction {

    /**
     * Выполнить команду.
     *
     * @throws CommandInputException с ключом перевода, если выполнить нельзя: мост команд ядра переведёт
     *                               его в игровое исключение на границе, и игрок увидит своё сообщение
     */
    void run(CommandContext context);
}
