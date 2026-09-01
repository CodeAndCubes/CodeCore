package com.mrleonardos.codecore.api.command;

/**
 * Ключи перевода для общих ответов команд.
 *
 * <p>
 * Сервер отправляет ключ, а не текст: перевод подставляет клиент, поэтому игроки с разными языками видят
 * сообщение каждый на своём.
 */
public final class CommandMessages {

    public static final String NOT_A_NUMBER = "codecore.command.error.not_a_number";
    public static final String OUT_OF_RANGE = "codecore.command.error.out_of_range";
    public static final String PLAYER_NOT_FOUND = "codecore.command.error.player_not_found";
    public static final String INVALID_DURATION = "codecore.command.error.invalid_duration";
    public static final String UNKNOWN_VALUE = "codecore.command.error.unknown_value";
    public static final String NO_PERMISSION = "codecore.command.error.no_permission";
    public static final String UNKNOWN_SUBCOMMAND = "codecore.command.error.unknown_subcommand";
    public static final String MISSING_ARGUMENT = "codecore.command.error.missing_argument";
    public static final String PLAYERS_ONLY = "codecore.command.error.players_only";
    public static final String USAGE = "codecore.command.usage";

    private CommandMessages() {}
}
