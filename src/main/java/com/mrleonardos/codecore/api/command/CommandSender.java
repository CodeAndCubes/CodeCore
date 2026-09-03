package com.mrleonardos.codecore.api.command;

import java.util.Optional;

import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Тот, кто выполняет команду: игрок, консоль, RCON или командный блок.
 *
 * <p>
 * Заменяет {@code net.minecraft.command.ICommandSender} на границе api. Мод, которому нужно спросить вид
 * отправителя, взять его ссылку или ответить ему, обходится этим интерфейсом и не ссылается ни на один
 * класс игры.
 *
 * <p>
 * Отвечать умеет сам отправитель, а не только контекст команды: фильтру или провайдеру, у которого на руках
 * один отправитель, иначе ответить нечем.
 */
public interface CommandSender {

    /** Кто это. */
    SenderKind kind();

    /** Ссылка на игрока; пустая у всех, кроме {@link SenderKind#PLAYER}. */
    Optional<PlayerRef> player();

    /** Отображаемое имя: ник игрока, имя консоли или имя командного блока. */
    String name();

    /** Точка, из которой пришла команда; пустая у консоли и RCON. */
    Optional<SenderPosition> position();

    /**
     * Ответить отправителю.
     *
     * <p>
     * Ответ уходит ключом перевода, а не готовым текстом: игрок увидит его на своём языке, потому что
     * перевод делает его клиент.
     */
    void reply(String translationKey, Object... arguments);

    /** Ответить об ошибке: то же самое, но заметным цветом. */
    void replyError(String translationKey, Object... arguments);
}
