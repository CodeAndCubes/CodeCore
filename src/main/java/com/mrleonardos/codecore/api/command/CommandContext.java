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

    /**
     * Кто выполняет команду: игрок, консоль, RCON или командный блок.
     *
     * <p>
     * Единственная дорога к отправителю после сноса старых подписей. Ссылка на игрока берётся у него же:
     * {@code caller().player()} отдаёт {@code Optional<PlayerRef>}, пустой у всех, кроме игрока.
     * Отдельного метода на контексте под неё нет намеренно, второй способ спросить то же самое только
     * плодит расхождения.
     *
     * <p>
     * Метод по умолчанию, а не обязательный, только на время переезда: у каждого мода линейки есть
     * подставной контекст в тестах, и обязательный метод уронил бы их все разом. Со сносом старых
     * подписей становится обязательным. Подставному контексту его стоит реализовать сразу, иначе он
     * бросит на первом же вызове из перееехавшего кода.
     */
    default CommandSender caller() {
        throw new UnsupportedOperationException(
            getClass().getName() + " must override caller(): the command sender is asked for without game types");
    }

    /** Кто выполняет команду; уходит со сносом старых подписей, вместо него {@link #caller()}. */
    ICommandSender sender();

    /**
     * Отправитель как игрок или {@code null}, если команду выполняет не игрок.
     *
     * <p>
     * Уходит со сносом старых подписей, вместо него {@code caller().player()}.
     */
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
