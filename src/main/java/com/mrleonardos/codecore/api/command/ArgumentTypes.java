package com.mrleonardos.codecore.api.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.util.Durations;
import com.mrleonardos.codecore.api.util.Players;

/** Готовые типы аргументов. */
public final class ArgumentTypes {

    private ArgumentTypes() {}

    /** Одно слово как есть. */
    public static ArgumentType<String> word() {
        return raw -> raw;
    }

    /** Весь остаток строки. Подходит для сообщений и причин. */
    public static ArgumentType<String> text() {
        return new ArgumentType<String>() {

            @Override
            public String parse(String raw) {
                return raw;
            }

            @Override
            public boolean greedy() {
                return true;
            }
        };
    }

    /** Целое число. */
    public static ArgumentType<Integer> integer() {
        return integer(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /** Целое число в заданных границах. */
    public static ArgumentType<Integer> integer(int min, int max) {
        return raw -> {
            int value;
            try {
                value = Integer.parseInt(raw);
            } catch (NumberFormatException notANumber) {
                throw new CommandException(CommandMessages.NOT_A_NUMBER, raw);
            }
            if (value < min || value > max) {
                throw new CommandException(CommandMessages.OUT_OF_RANGE, raw, min, max);
            }
            return value;
        };
    }

    /** Игрок, который сейчас на сервере. */
    public static ArgumentType<EntityPlayerMP> player() {
        return new ArgumentType<EntityPlayerMP>() {

            @Override
            public EntityPlayerMP parse(String raw) {
                EntityPlayerMP player = Players.online(raw);
                if (player == null) {
                    throw new CommandException(CommandMessages.PLAYER_NOT_FOUND, raw);
                }
                return player;
            }

            @Override
            public List<String> suggestions(ICommandSender sender, String partial) {
                return startingWith(Players.onlineNames(), partial);
            }
        };
    }

    /** Срок вида {@code 10m} или {@code 2h30m}, разобранный в секунды. */
    public static ArgumentType<Integer> duration() {
        return raw -> {
            int seconds = Durations.toSeconds(raw);
            if (seconds < 0) {
                throw new CommandException(CommandMessages.INVALID_DURATION, raw);
            }
            return seconds;
        };
    }

    /** Одно из значений перечисления, без учёта регистра. */
    public static <E extends Enum<E>> ArgumentType<E> enumOf(Class<E> type) {
        return new ArgumentType<E>() {

            @Override
            public E parse(String raw) {
                for (E value : type.getEnumConstants()) {
                    if (value.name()
                        .equalsIgnoreCase(raw)) {
                        return value;
                    }
                }
                throw new CommandException(CommandMessages.UNKNOWN_VALUE, raw);
            }

            @Override
            public List<String> suggestions(ICommandSender sender, String partial) {
                List<String> names = new ArrayList<>();
                for (E value : type.getEnumConstants()) {
                    names.add(
                        value.name()
                            .toLowerCase(Locale.ROOT));
                }
                return startingWith(names, partial);
            }
        };
    }

    private static List<String> startingWith(List<String> candidates, String partial) {
        String prefix = partial.toLowerCase(Locale.ROOT);
        List<String> matching = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT)
                .startsWith(prefix)) {
                matching.add(candidate);
            }
        }
        return matching;
    }
}
