package com.mrleonardos.codecore.api.command;

/** Что делает команда, когда её разобрали. */
@FunctionalInterface
public interface CommandAction {

    /**
     * Выполнить команду.
     *
     * @throws net.minecraft.command.CommandException с ключом перевода, если выполнить нельзя
     */
    void run(CommandContext context);
}
