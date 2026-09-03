package com.mrleonardos.codecore.api.net;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mrleonardos.codecore.api.actor.PlayerRef;

/**
 * Обстановка, в которой обрабатывается пакет.
 *
 * <p>
 * Обработчик уже выполняется в главном потоке своей стороны, так что обращаться к миру и игрокам отсюда
 * безопасно.
 */
public interface PacketContext {

    /**
     * Ссылка на игрока, приславшего пакет; на клиенте пустая.
     *
     * <p>
     * Единственный источник правды об отправителе: имя или идентификатор внутри самого пакета доверия не
     * заслуживают. Со сносом старых подписей остаётся только этот метод.
     */
    Optional<PlayerRef> player();

    /**
     * Игрок, приславший пакет; на клиенте {@code null}.
     *
     * <p>
     * Уходит со сносом старых подписей, вместо него {@link #player()}.
     */
    EntityPlayerMP sender();

    /** Обрабатывается ли пакет на сервере. */
    boolean onServer();
}
