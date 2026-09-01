package com.mrleonardos.codecore.api.command;

/**
 * Регистрация команд.
 *
 * <p>
 * Объявлять команды можно на инициализации мода: ядро само отдаст их серверу в нужный момент и сделает то
 * же самое при перезапуске мира.
 */
public interface CommandService {

    /** Добавить команду с её деревом подкоманд. */
    void register(CommandNode root);
}
