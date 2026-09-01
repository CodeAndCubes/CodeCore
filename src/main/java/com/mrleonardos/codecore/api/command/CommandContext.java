package com.mrleonardos.codecore.api.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Разобранная команда: кто вызвал и с какими аргументами.
 *
 * <p>
 * Ответы уходят ключами перевода, а не готовым текстом: игрок увидит их на своём языке, потому что
 * перевод делает его клиент.
 */
public interface CommandContext {

    /** Кто выполняет команду: игрок, консоль, командный блок. */
    ICommandSender sender();

    /** Отправитель как игрок или {@code null}, если команду выполняет не игрок. */
    EntityPlayerMP player();

    /**
     * Значение аргумента по имени.
     *
     * @throws IllegalArgumentException если такого аргумента у команды нет
     */
    <T> T get(String name);

    /** Значение аргумента или запасное, если аргумент не был указан. */
    <T> T getOrDefault(String name, T fallback);

    /** Был ли аргумент указан. */
    boolean has(String name);

    /** Ответить отправителю. */
    void reply(String translationKey, Object... arguments);

    /** Ответить об ошибке: то же самое, но заметным цветом. */
    void replyError(String translationKey, Object... arguments);
}
