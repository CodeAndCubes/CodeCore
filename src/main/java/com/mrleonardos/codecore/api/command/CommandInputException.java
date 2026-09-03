package com.mrleonardos.codecore.api.command;

/**
 * Введённое не подходит: не число, число вне границ, игрока нет на сервере.
 *
 * <p>
 * Заменяет {@code net.minecraft.command.CommandException} на границе api. Мост команд ядра ловит это
 * исключение и бросает дальше игровое с тем же ключом и теми же аргументами, так что игрок видит прежнее
 * сообщение, а разбор аргумента проверяется тестом без запуска игры.
 *
 * <p>
 * Сообщение уходит ключом перевода: подставлять готовый текст нельзя, у каждого игрока свой язык.
 */
public final class CommandInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String translationKey;
    private final Object[] arguments;

    public CommandInputException(String translationKey, Object... arguments) {
        super(describe(translationKey, arguments));
        this.translationKey = translationKey;
        this.arguments = arguments == null ? new Object[0] : arguments.clone();
    }

    /** Ключ перевода сообщения об ошибке. */
    public String translationKey() {
        return translationKey;
    }

    /** Что подставляется в перевод. */
    public Object[] arguments() {
        return arguments.clone();
    }

    private static String describe(String translationKey, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return translationKey;
        }
        StringBuilder text = new StringBuilder(translationKey).append(" [");
        for (int index = 0; index < arguments.length; index++) {
            if (index > 0) {
                text.append(", ");
            }
            text.append(arguments[index]);
        }
        return text.append(']')
            .toString();
    }
}
